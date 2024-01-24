package com.cornmuffin.prototype.data.products.datasources.networkproducts

import com.cornmuffin.prototype.data.products.Product

internal data class ProductBean(
    val title: String?,
    val description: String?,
    val rating: RatingBean? = null,
    val image: String?
) {
    fun parse(): Product = Product(
        title = title.orEmpty(),
        description = description.orEmpty(),
        rating = rating?.rate ?: 0.0,
        count = rating?.count ?: 0,
        image = image,
    )
}
