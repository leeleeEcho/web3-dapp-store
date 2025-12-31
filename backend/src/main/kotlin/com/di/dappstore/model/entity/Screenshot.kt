package com.di.dappstore.model.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * 应用截图实体
 */
@Table("screenshots")
data class Screenshot(
    @Column("app_id")
    val appId: Long,

    @Column("image_url")
    val imageUrl: String,

    @Column("sort_order")
    var sortOrder: Int = 0,

    @Column("width")
    var width: Int? = null,

    @Column("height")
    var height: Int? = null
) : BaseEntity()
