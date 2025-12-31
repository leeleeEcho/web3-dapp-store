package com.di.dappstore.model.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * 应用评论实体
 */
@Table("reviews")
data class Review(
    @Column("app_id")
    val appId: Long,

    @Column("user_id")
    val userId: Long,

    @Column("rating")
    var rating: Int,  // 1-5 星

    @Column("title")
    var title: String? = null,

    @Column("content")
    var content: String? = null,

    @Column("is_helpful_count")
    var helpfulCount: Int = 0,

    @Column("developer_reply")
    var developerReply: String? = null,

    @Column("developer_reply_at")
    var developerReplyAt: java.time.LocalDateTime? = null,

    @Column("is_deleted")
    var isDeleted: Boolean = false
) : BaseEntity()
