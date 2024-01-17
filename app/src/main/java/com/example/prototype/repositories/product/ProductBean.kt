package com.example.prototype.repositories.product

import com.example.prototype.Product

internal data class ProductBean(
    val title: String?,
    val description: String?,
    val rating: RatingBean? = null,
) {
    fun parse(): Product = Product(
        title = title.orEmpty(),
        description = description.orEmpty(),
        rating = rating?.rate ?: 0.0,
        count = rating?.count ?: 0,
    )
}
