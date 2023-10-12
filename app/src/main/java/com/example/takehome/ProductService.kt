package com.example.takehome

import retrofit2.http.GET

interface ProductService {
    @GET("products.v1.json")
    suspend fun getProducts(): List<Product>
}
