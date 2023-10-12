package com.example.takehome

class ProductRepository {
    private val productService = RetrofitInstance.productService

    suspend fun getProducts(): List<Product> = productService.getProducts()
}
