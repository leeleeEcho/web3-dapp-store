package com.di.dappstore.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 创建/更新 App 请求 DTO
 */
data class CreateAppRequest(
    @field:NotBlank(message = "包名不能为空")
    val packageName: String,

    @field:NotBlank(message = "应用名称不能为空")
    @field:Size(max = 100, message = "应用名称不能超过100个字符")
    val name: String,

    @field:Size(max = 5000, message = "描述不能超过5000个字符")
    val description: String? = null,

    @field:Size(max = 200, message = "简短描述不能超过200个字符")
    val shortDescription: String? = null,

    @field:NotBlank(message = "版本号不能为空")
    val versionName: String,

    val versionCode: Long,

    val minSdkVersion: Int = 21,
    val targetSdkVersion: Int = 34,

    val categoryId: Long? = null,

    val isWeb3: Boolean = true,
    val blockchain: String? = null,
    val contractAddress: String? = null,

    val websiteUrl: String? = null,
    val sourceCodeUrl: String? = null
)

/**
 * 更新 App 请求 DTO
 */
data class UpdateAppRequest(
    val name: String? = null,
    val description: String? = null,
    val shortDescription: String? = null,
    val versionName: String? = null,
    val versionCode: Long? = null,
    val categoryId: Long? = null,
    val websiteUrl: String? = null,
    val sourceCodeUrl: String? = null
)
