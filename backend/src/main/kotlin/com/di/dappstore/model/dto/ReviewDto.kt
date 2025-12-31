package com.di.dappstore.model.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 创建评论请求 DTO
 */
data class CreateReviewRequest(
    @field:NotNull(message = "评分不能为空")
    @field:Min(value = 1, message = "评分最小为1")
    @field:Max(value = 5, message = "评分最大为5")
    val rating: Int,

    @field:Size(max = 100, message = "标题不能超过100个字符")
    val title: String? = null,

    @field:Size(max = 2000, message = "评论内容不能超过2000个字符")
    val content: String? = null
)

/**
 * 开发者回复请求
 */
data class DeveloperReplyRequest(
    @field:Size(max = 2000, message = "回复内容不能超过2000个字符")
    val reply: String
)
