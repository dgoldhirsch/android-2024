package com.example.prototype.repositories.product

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object ProductRetrofitInstance {
    private const val BASE_URL = "https://fakestoreapi.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    internal val productService: ProductService = retrofit.create(ProductService::class.java)
}
