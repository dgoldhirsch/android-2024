package com.example.prototype.repositories.product

import retrofit2.Response
import retrofit2.http.GET

internal interface ProductService {
    @GET("products")
    suspend fun getProducts(): Response<List<ProductBean>>
}
