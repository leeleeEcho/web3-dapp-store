package com.di.dappstore.model.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * 应用分类实体
 */
@Table("categories")
data class Category(
    @Column("name")
    val name: String,

    @Column("display_name")
    var displayName: String,

    @Column("description")
    var description: String? = null,

    @Column("icon_name")
    var iconName: String? = null,

    @Column("sort_order")
    var sortOrder: Int = 0,

    @Column("is_active")
    var isActive: Boolean = true,

    @Column("app_count")
    var appCount: Int = 0
) : BaseEntity()
