package com.cornmuffin.prototype.data.products.datasources.networkproducts

internal data class RatingBean(
    val rate: Double = 0.0, // guaranteed non-nullable through Gson
    val count: Int = 0, // ditto
)
