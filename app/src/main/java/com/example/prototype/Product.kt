package com.example.prototype

data class Product(
    val title: String = "",
    val description: String = "",
    val rating: Double = 0.0,
    var count: Int = 0,
)
