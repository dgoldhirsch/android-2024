package com.cornmuffin.prototype.data.products.datasources

internal data class RatingBean(
    val rate: Double = 0.0, // guaranteed non-nullable through Gson
    val count: Int = 0, // ditto
)
