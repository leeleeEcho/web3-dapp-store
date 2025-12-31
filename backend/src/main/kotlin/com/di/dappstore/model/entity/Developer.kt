package com.di.dappstore.model.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * 开发者实体
 */
@Table("developers")
data class Developer(
    @Column("user_id")
    val userId: Long,

    @Column("company_name")
    var companyName: String? = null,

    @Column("website_url")
    var websiteUrl: String? = null,

    @Column("contact_email")
    var contactEmail: String,

    @Column("description")
    var description: String? = null,

    @Column("logo_url")
    var logoUrl: String? = null,

    @Column("is_verified")
    var isVerified: Boolean = false,

    @Column("verification_status")
    var verificationStatus: VerificationStatus = VerificationStatus.PENDING,

    @Column("total_apps")
    var totalApps: Int = 0,

    @Column("total_downloads")
    var totalDownloads: Long = 0
) : BaseEntity()

/**
 * 开发者验证状态
 */
enum class VerificationStatus {
    PENDING,    // 待验证
    VERIFIED,   // 已验证
    REJECTED    // 验证被拒
}
