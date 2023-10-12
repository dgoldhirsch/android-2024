package com.example.takehome

data class Product(
    val name: String = "",
    val tagline: String = "",
    val rating: Double = 0.0,
    val date: String = "",
) {
    data class Bean(
        val name: String?,
        val tagline: String?,
        val rating: Double = 0.0, // guaranteed non-nullable through Gson
        val date: String?,
    ) {
        fun asProduct() = Product(
            name = name.orEmpty(),
            tagline = tagline.orEmpty(),
            rating = rating,
            date = date.orEmpty(),
        )
    }
}
