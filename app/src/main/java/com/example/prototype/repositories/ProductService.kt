package com.example.prototype.repositories

import retrofit2.Response
import retrofit2.http.GET

interface ProductService {
    @GET("products")
    suspend fun getProducts(): Response<List<ProductBean>>
}
