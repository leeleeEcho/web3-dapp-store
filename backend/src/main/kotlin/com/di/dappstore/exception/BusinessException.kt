package com.di.dappstore.exception

/**
 * 业务异常基类
 */
open class BusinessException(
    override val message: String,
    val code: Int = -1
) : RuntimeException(message)

/**
 * 资源不存在异常
 */
class ResourceNotFoundException(
    message: String = "资源不存在"
) : BusinessException(message, 404)

/**
 * 未授权异常
 */
class UnauthorizedException(
    message: String = "未授权"
) : BusinessException(message, 401)

/**
 * 禁止访问异常
 */
class ForbiddenException(
    message: String = "禁止访问"
) : BusinessException(message, 403)

/**
 * 重复操作异常
 */
class DuplicateException(
    message: String = "重复操作"
) : BusinessException(message, 409)

/**
 * 验证失败异常
 */
class ValidationException(
    message: String = "验证失败"
) : BusinessException(message, 400)
