package com.di.dappstore.service

import com.di.dappstore.model.dto.CreateReviewRequest
import com.di.dappstore.model.entity.Review
import com.di.dappstore.model.vo.PageResponse
import com.di.dappstore.model.vo.ReviewVo
import com.di.dappstore.repository.AppRepository
import com.di.dappstore.repository.ReviewRepository
import com.di.dappstore.repository.UserRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val appRepository: AppRepository
) {

    /**
     * 获取应用评论 (分页)
     */
    fun getReviewsByAppId(appId: Long, page: Int, size: Int): Mono<PageResponse<ReviewVo>> {
        val offset = page * size

        return reviewRepository.findByAppIdPaged(appId, size, offset)
            .flatMap { review -> toReviewVo(review) }
            .collectList()
            .zipWith(reviewRepository.countByAppId(appId)) { reviews, total ->
                PageResponse.of(reviews, page, size, total)
            }
    }

    /**
     * 创建评论
     */
    fun createReview(appId: Long, userId: Long, request: CreateReviewRequest): Mono<Review> {
        // 检查是否已评论过
        return reviewRepository.findByAppIdAndUserId(appId, userId)
            .flatMap<Review> {
                Mono.error(IllegalStateException("您已经评论过此应用"))
            }
            .switchIfEmpty(
                Mono.defer {
                    val review = Review(
                        appId = appId,
                        userId = userId,
                        rating = request.rating,
                        title = request.title,
                        content = request.content
                    )
                    reviewRepository.save(review)
                        .flatMap { savedReview ->
                            // 更新应用评分
                            updateAppRating(appId).thenReturn(savedReview)
                        }
                }
            )
    }

    /**
     * 开发者回复评论
     */
    fun replyToReview(reviewId: Long, developerId: Long, reply: String): Mono<Review> {
        return reviewRepository.findById(reviewId)
            .flatMap { review ->
                // 验证应用属于该开发者
                appRepository.findById(review.appId)
                    .filter { it.developerId == developerId }
                    .flatMap {
                        review.developerReply = reply
                        review.developerReplyAt = LocalDateTime.now()
                        reviewRepository.save(review)
                    }
            }
    }

    /**
     * 删除评论
     */
    fun deleteReview(reviewId: Long, userId: Long): Mono<Void> {
        return reviewRepository.findById(reviewId)
            .filter { it.userId == userId }
            .flatMap { review ->
                review.isDeleted = true
                reviewRepository.save(review)
                    .flatMap { updateAppRating(it.appId) }
            }
            .then()
    }

    /**
     * 标记评论为有帮助
     */
    fun markAsHelpful(reviewId: Long): Mono<Void> {
        return reviewRepository.incrementHelpfulCount(reviewId)
    }

    /**
     * 更新应用评分
     */
    private fun updateAppRating(appId: Long): Mono<Void> {
        return Mono.zip(
            reviewRepository.calculateAverageRating(appId).defaultIfEmpty(0.0),
            reviewRepository.countByAppId(appId)
        ).flatMap { tuple ->
            val average = tuple.t1
            val count = tuple.t2
            appRepository.findById(appId)
                .flatMap { app ->
                    app.ratingAverage = average
                    app.ratingCount = count
                    appRepository.save(app)
                }
        }.then()
    }

    /**
     * 转换为 VO
     */
    private fun toReviewVo(review: Review): Mono<ReviewVo> {
        return userRepository.findById(review.userId)
            .map { user ->
                ReviewVo(
                    id = review.id!!,
                    userId = review.userId,
                    username = user.username,
                    userAvatar = user.avatarUrl,
                    rating = review.rating,
                    title = review.title,
                    content = review.content,
                    helpfulCount = review.helpfulCount,
                    developerReply = review.developerReply,
                    developerReplyAt = review.developerReplyAt,
                    createdAt = review.createdAt
                )
            }
            .defaultIfEmpty(
                ReviewVo(
                    id = review.id!!,
                    userId = review.userId,
                    username = null,
                    userAvatar = null,
                    rating = review.rating,
                    title = review.title,
                    content = review.content,
                    helpfulCount = review.helpfulCount,
                    developerReply = review.developerReply,
                    developerReplyAt = review.developerReplyAt,
                    createdAt = review.createdAt
                )
            )
    }
}
