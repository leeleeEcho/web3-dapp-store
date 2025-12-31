package com.di.dappstore.model.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 开发者注册请求 DTO
 */
data class DeveloperRegistrationRequest(
    @field:Size(max = 100, message = "公司名称不能超过100个字符")
    val companyName: String? = null,

    val websiteUrl: String? = null,

    @field:NotBlank(message = "联系邮箱不能为空")
    @field:Email(message = "请输入有效的邮箱地址")
    val contactEmail: String,

    @field:Size(max = 1000, message = "描述不能超过1000个字符")
    val description: String? = null
)

/**
 * 更新开发者信息请求
 */
data class UpdateDeveloperRequest(
    val companyName: String? = null,
    val websiteUrl: String? = null,
    val contactEmail: String? = null,
    val description: String? = null
)
