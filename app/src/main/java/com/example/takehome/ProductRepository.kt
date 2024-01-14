package com.example.takehome

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class ProductRepository {
    private val productService = RetrofitInstance.productService

    val productFlow: Flow<NetworkResult<List<Product>>> = flow {
        emit(
            makeApiCall { productService.getProducts() },
        )
    }

    private class EmptyBodyException : Exception()
    private class RetrofitNetworkException(code: Int, message: String) :
        Exception("Retrofit Network Exception $code: $message")

    private suspend fun <T> makeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = apiCall()

            return if (response.isSuccessful) {
                response.body()?.let { NetworkResult.Success(it) } ?: NetworkResult.Error(EmptyBodyException())
            } else {
                NetworkResult.Error(RetrofitNetworkException(response.code(), response.message()))
            }
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }
}
