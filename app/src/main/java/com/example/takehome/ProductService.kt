package com.example.takehome

import kotlinx.collections.immutable.ImmutableList
import retrofit2.Response
import retrofit2.http.GET

interface ProductService {
    @GET("products.v1.json")
    suspend fun getProducts(): Response<List<Product>>
}
