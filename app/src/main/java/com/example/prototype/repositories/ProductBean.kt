package com.example.prototype.repositories

import com.example.prototype.Product

data class ProductBean(
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
