package com.di.dappstore.service

import com.di.dappstore.model.dto.DeveloperRegistrationRequest
import com.di.dappstore.model.dto.UpdateDeveloperRequest
import com.di.dappstore.model.entity.Developer
import com.di.dappstore.model.entity.UserRole
import com.di.dappstore.repository.DeveloperRepository
import com.di.dappstore.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Service
class DeveloperService(
    private val developerRepository: DeveloperRepository,
    private val userRepository: UserRepository
) {

    /**
     * 注册为开发者
     */
    fun registerDeveloper(userId: Long, request: DeveloperRegistrationRequest): Mono<Developer> {
        // 检查是否已经是开发者
        return developerRepository.findByUserId(userId)
            .flatMap<Developer> {
                Mono.error(IllegalStateException("您已经是开发者"))
            }
            .switchIfEmpty(
                Mono.defer {
                    val developer = Developer(
                        userId = userId,
                        companyName = request.companyName,
                        websiteUrl = request.websiteUrl,
                        contactEmail = request.contactEmail,
                        description = request.description
                    )

                    developerRepository.save(developer)
                        .flatMap { savedDeveloper ->
                            // 更新用户角色
                            userRepository.findById(userId)
                                .flatMap { user ->
                                    user.role = UserRole.DEVELOPER
                                    userRepository.save(user)
                                }
                                .thenReturn(savedDeveloper)
                        }
                        .doOnSuccess { logger.info { "Developer registered: ${it.contactEmail}" } }
                }
            )
    }

    /**
     * 获取开发者信息
     */
    fun getDeveloperByUserId(userId: Long): Mono<Developer> {
        return developerRepository.findByUserId(userId)
    }

    /**
     * 获取开发者信息 (by ID)
     */
    fun getDeveloperById(id: Long): Mono<Developer> {
        return developerRepository.findById(id)
    }

    /**
     * 更新开发者信息
     */
    fun updateDeveloper(userId: Long, request: UpdateDeveloperRequest): Mono<Developer> {
        return developerRepository.findByUserId(userId)
            .flatMap { developer ->
                request.companyName?.let { developer.companyName = it }
                request.websiteUrl?.let { developer.websiteUrl = it }
                request.contactEmail?.let { developer.contactEmail = it }
                request.description?.let { developer.description = it }
                developerRepository.save(developer)
            }
    }

    /**
     * 更新开发者 Logo
     */
    fun updateLogo(userId: Long, logoUrl: String): Mono<Developer> {
        return developerRepository.findByUserId(userId)
            .flatMap { developer ->
                developer.logoUrl = logoUrl
                developerRepository.save(developer)
            }
    }
}
