package com.di.dappstore

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.*

/**
 * API 测试脚本 - 生成测试用 JWT Token
 * 运行: kotlinc -script ApiTestScript.kt
 */
fun main() {
    val secret = "dev-only-secret-key-for-testing-purpose-must-be-at-least-256-bits"
    val key = Keys.hmacShaKeyFor(secret.toByteArray())

    // 生成普通用户 Token (userId=1)
    val userToken = Jwts.builder()
        .setSubject("1")
        .claim("role", "USER")
        .setIssuedAt(Date())
        .setExpiration(Date(System.currentTimeMillis() + 86400000))
        .signWith(key)
        .compact()

    // 生成开发者 Token (userId=2)
    val developerToken = Jwts.builder()
        .setSubject("2")
        .claim("role", "DEVELOPER")
        .setIssuedAt(Date())
        .setExpiration(Date(System.currentTimeMillis() + 86400000))
        .signWith(key)
        .compact()

    // 生成管理员 Token (userId=3)
    val adminToken = Jwts.builder()
        .setSubject("3")
        .claim("role", "ADMIN")
        .setIssuedAt(Date())
        .setExpiration(Date(System.currentTimeMillis() + 86400000))
        .signWith(key)
        .compact()

    println("=== Test JWT Tokens ===")
    println()
    println("USER Token (userId=1):")
    println(userToken)
    println()
    println("DEVELOPER Token (userId=2):")
    println(developerToken)
    println()
    println("ADMIN Token (userId=3):")
    println(adminToken)
}
