package com.di.dappstore.security

import com.di.dappstore.model.dto.AuthResponse
import com.di.dappstore.model.dto.UserResponse
import com.di.dappstore.model.entity.AuthProvider
import com.di.dappstore.model.entity.User
import com.di.dappstore.repository.UserRepository
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.math.BigInteger
import java.time.LocalDateTime
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * 认证服务 - 支持 Google OAuth 和钱包签名双认证
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    @Value("\${app.google.client-id:}")
    private val googleClientId: String
) {

    private val googleVerifier: GoogleIdTokenVerifier by lazy {
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(googleClientId))
            .build()
    }

    // ============================================
    // Google OAuth 认证
    // ============================================

    /**
     * Google 登录
     */
    fun loginWithGoogle(idToken: String): Mono<AuthResponse> {
        return Mono.fromCallable { verifyGoogleToken(idToken) }
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { googlePayload ->
                if (googlePayload == null) {
                    Mono.error(AuthenticationException("无效的 Google ID Token"))
                } else {
                    getOrCreateGoogleUser(googlePayload)
                }
            }
            .flatMap { user -> createAuthResponse(user) }
    }

    /**
     * 验证 Google ID Token
     */
    private fun verifyGoogleToken(idToken: String): GoogleIdToken.Payload? {
        return try {
            val googleIdToken = googleVerifier.verify(idToken)
            googleIdToken?.payload
        } catch (e: Exception) {
            logger.error { "Google token verification failed: ${e.message}" }
            null
        }
    }

    /**
     * 获取或创建 Google 用户
     */
    private fun getOrCreateGoogleUser(payload: GoogleIdToken.Payload): Mono<User> {
        val googleId = payload.subject
        val email = payload.email
        val name = payload["name"] as? String
        val avatarUrl = payload["picture"] as? String

        return userRepository.findByGoogleId(googleId)
            .switchIfEmpty(
                Mono.defer {
                    val user = User(
                        googleId = googleId,
                        authProvider = AuthProvider.GOOGLE,
                        email = email,
                        username = name,
                        avatarUrl = avatarUrl
                    )
                    userRepository.save(user)
                        .doOnSuccess { logger.info { "New Google user created: $googleId" } }
                }
            )
            .flatMap { user ->
                // 更新最后登录时间
                user.lastLoginAt = LocalDateTime.now()
                userRepository.save(user)
            }
    }

    // ============================================
    // 钱包签名认证
    // ============================================

    /**
     * 钱包登录
     */
    fun loginWithWallet(walletAddress: String, signature: String, message: String): Mono<AuthResponse> {
        val normalizedAddress = walletAddress.lowercase()

        return userRepository.findByWalletAddress(normalizedAddress)
            .switchIfEmpty(Mono.error(AuthenticationException("用户不存在，请先获取 nonce")))
            .flatMap { user ->
                // 验证 nonce 是否在消息中
                if (user.nonce == null || !message.contains(user.nonce!!)) {
                    return@flatMap Mono.error<User>(AuthenticationException("Nonce 不匹配"))
                }

                // 验证签名
                Mono.fromCallable { verifyWalletSignature(normalizedAddress, signature, message) }
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap { isValid ->
                        if (!isValid) {
                            Mono.error(AuthenticationException("签名验证失败"))
                        } else {
                            // 更新最后登录时间并刷新 nonce
                            user.lastLoginAt = LocalDateTime.now()
                            user.nonce = generateNonce()
                            userRepository.save(user)
                        }
                    }
            }
            .flatMap { user -> createAuthResponse(user) }
    }

    /**
     * 验证钱包签名 (EIP-191 Personal Sign)
     */
    private fun verifyWalletSignature(walletAddress: String, signature: String, message: String): Boolean {
        return try {
            val messageHash = getEthereumMessageHash(message)
            val signatureBytes = Numeric.hexStringToByteArray(signature)

            if (signatureBytes.size != 65) {
                logger.warn { "Invalid signature length: ${signatureBytes.size}" }
                return false
            }

            val r = signatureBytes.copyOfRange(0, 32)
            val s = signatureBytes.copyOfRange(32, 64)
            var v = signatureBytes[64]

            // 处理 v 值 (可能是 27/28 或 0/1)
            if (v < 27) {
                v = (v + 27).toByte()
            }

            val signatureData = Sign.SignatureData(v, r, s)

            // 尝试恢复公钥
            for (recId in 0..3) {
                val adjustedV = (27 + recId).toByte()
                val adjustedSignatureData = Sign.SignatureData(adjustedV, r, s)

                try {
                    val recoveredKey = Sign.signedMessageHashToKey(messageHash, adjustedSignatureData)
                    val recoveredAddress = "0x${Keys.getAddress(recoveredKey)}"

                    if (recoveredAddress.equals(walletAddress, ignoreCase = true)) {
                        return true
                    }
                } catch (e: Exception) {
                    // 继续尝试下一个 recId
                }
            }

            false
        } catch (e: Exception) {
            logger.error { "Signature verification error: ${e.message}" }
            false
        }
    }

    /**
     * 计算 Ethereum 消息哈希 (EIP-191)
     */
    private fun getEthereumMessageHash(message: String): ByteArray {
        val prefix = "\u0019Ethereum Signed Message:\n${message.length}"
        val prefixedMessage = prefix + message
        return org.web3j.crypto.Hash.sha3(prefixedMessage.toByteArray())
    }

    /**
     * 生成 nonce
     */
    fun generateNonce(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    /**
     * 为钱包地址生成并保存 nonce
     */
    fun generateAndSaveNonce(walletAddress: String): Mono<String> {
        val normalizedAddress = walletAddress.lowercase()

        return userRepository.findByWalletAddress(normalizedAddress)
            .switchIfEmpty(
                Mono.defer {
                    val user = User(
                        walletAddress = normalizedAddress,
                        authProvider = AuthProvider.WALLET,
                        nonce = generateNonce()
                    )
                    userRepository.save(user)
                        .doOnSuccess { logger.info { "New wallet user created: $normalizedAddress" } }
                }
            )
            .flatMap { user ->
                user.nonce = generateNonce()
                userRepository.save(user)
            }
            .map { it.nonce!! }
    }

    // ============================================
    // 钱包绑定 (Google 用户绑定钱包)
    // ============================================

    /**
     * 绑定钱包到现有账户
     */
    fun bindWallet(userId: Long, walletAddress: String, signature: String, message: String): Mono<User> {
        val normalizedAddress = walletAddress.lowercase()

        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(AuthenticationException("用户不存在")))
            .flatMap { user ->
                // 检查钱包是否已被绑定
                userRepository.findByWalletAddress(normalizedAddress)
                    .flatMap<User> { Mono.error(AuthenticationException("该钱包已被其他账户绑定")) }
                    .switchIfEmpty(
                        Mono.fromCallable { verifyWalletSignature(normalizedAddress, signature, message) }
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap { isValid ->
                                if (!isValid) {
                                    Mono.error(AuthenticationException("签名验证失败"))
                                } else {
                                    user.walletAddress = normalizedAddress
                                    userRepository.save(user)
                                }
                            }
                    )
            }
    }

    // ============================================
    // 辅助方法
    // ============================================

    /**
     * 创建认证响应
     */
    private fun createAuthResponse(user: User): Mono<AuthResponse> {
        val token = jwtService.generateToken(user)
        val expiresIn = jwtService.getExpirationTime() / 1000 // 转换为秒

        return Mono.just(
            AuthResponse(
                token = token,
                expiresIn = expiresIn,
                user = UserResponse(
                    id = user.id!!,
                    walletAddress = user.walletAddress,
                    googleId = user.googleId,
                    authProvider = user.authProvider,
                    username = user.username,
                    email = user.email,
                    avatarUrl = user.avatarUrl,
                    role = user.role.name,
                    createdAt = user.createdAt!!
                )
            )
        )
    }
}

/**
 * 认证异常
 */
class AuthenticationException(message: String) : RuntimeException(message)
