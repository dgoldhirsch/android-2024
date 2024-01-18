package com.cornmuffin.prototype.data.products

data class Product(
    val title: String = "",
    val description: String = "",
    val rating: Double = 0.0,
    val count: Int = 0,
    val image: String? = null, // TODO initialize with "No image available" if none given
)
