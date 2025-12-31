package com.di.dappstore.service

import com.di.dappstore.model.entity.Category
import com.di.dappstore.model.vo.CategorySummary
import com.di.dappstore.repository.CategoryRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    /**
     * 获取所有分类
     */
    fun getAllCategories(): Flux<CategorySummary> {
        return categoryRepository.findAllActiveSorted()
            .map { toSummary(it) }
    }

    /**
     * 根据 ID 获取分类
     */
    fun getCategoryById(id: Long): Mono<Category> {
        return categoryRepository.findById(id)
    }

    /**
     * 根据名称获取分类
     */
    fun getCategoryByName(name: String): Mono<Category> {
        return categoryRepository.findByName(name)
    }

    /**
     * 创建分类
     */
    fun createCategory(name: String, displayName: String, description: String?, iconName: String?, sortOrder: Int): Mono<Category> {
        val category = Category(
            name = name,
            displayName = displayName,
            description = description,
            iconName = iconName,
            sortOrder = sortOrder
        )
        return categoryRepository.save(category)
    }

    /**
     * 更新分类
     */
    fun updateCategory(id: Long, displayName: String?, description: String?, iconName: String?, sortOrder: Int?, isActive: Boolean?): Mono<Category> {
        return categoryRepository.findById(id)
            .flatMap { category ->
                displayName?.let { category.displayName = it }
                description?.let { category.description = it }
                iconName?.let { category.iconName = it }
                sortOrder?.let { category.sortOrder = it }
                isActive?.let { category.isActive = it }
                categoryRepository.save(category)
            }
    }

    /**
     * 转换为摘要
     */
    private fun toSummary(category: Category): CategorySummary {
        return CategorySummary(
            id = category.id!!,
            name = category.name,
            displayName = category.displayName
        )
    }
}
