package com.di.dappstore.model.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * DApp 应用实体
 */
@Table("apps")
data class App(
    @Column("package_name")
    val packageName: String,

    @Column("name")
    var name: String,

    @Column("description")
    var description: String? = null,

    @Column("short_description")
    var shortDescription: String? = null,

    @Column("version_name")
    var versionName: String,

    @Column("version_code")
    var versionCode: Long,

    @Column("min_sdk_version")
    var minSdkVersion: Int = 21,

    @Column("target_sdk_version")
    var targetSdkVersion: Int = 34,

    @Column("icon_url")
    var iconUrl: String? = null,

    @Column("apk_url")
    var apkUrl: String,

    @Column("apk_size")
    var apkSize: Long = 0,

    @Column("apk_hash")
    var apkHash: String,

    @Column("developer_id")
    val developerId: Long,

    @Column("category_id")
    var categoryId: Long? = null,

    @Column("is_web3")
    var isWeb3: Boolean = true,

    @Column("blockchain")
    var blockchain: String? = null,  // ethereum, solana, polygon, etc.

    @Column("contract_address")
    var contractAddress: String? = null,

    @Column("website_url")
    var websiteUrl: String? = null,

    @Column("source_code_url")
    var sourceCodeUrl: String? = null,

    @Column("download_count")
    var downloadCount: Long = 0,

    @Column("rating_average")
    var ratingAverage: Double = 0.0,

    @Column("rating_count")
    var ratingCount: Long = 0,

    @Column("status")
    var status: AppStatus = AppStatus.PENDING,

    @Column("is_featured")
    var isFeatured: Boolean = false,

    @Column("is_deleted")
    var isDeleted: Boolean = false,

    @Column("rejection_reason")
    var rejectionReason: String? = null,  // 拒绝原因

    @Column("submitted_at")
    var submittedAt: java.time.LocalDateTime? = null,  // 提交审核时间

    @Column("reviewed_at")
    var reviewedAt: java.time.LocalDateTime? = null,  // 审核时间

    @Column("reviewer_id")
    var reviewerId: Long? = null  // 审核人 ID
) : BaseEntity()

/**
 * App 状态枚举
 */
enum class AppStatus {
    DRAFT,      // 草稿
    PENDING,    // 待审核
    APPROVED,   // 已通过
    REJECTED,   // 已拒绝
    SUSPENDED   // 已暂停
}
