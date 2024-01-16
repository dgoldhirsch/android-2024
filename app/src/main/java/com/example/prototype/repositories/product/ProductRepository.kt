package com.example.prototype.repositories.product

import com.example.prototype.repositories.NetworkResult
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class ProductRepository {
    private val productService = ProductRetrofitInstance.productService

    val fetchNetworkResult: Flow<NetworkResult> = flow {
        emit(
            fetchAndParseProducts { productService.getProducts() },
        )
    }

    private class EmptyBodyException : Exception()
    private class RetrofitNetworkException(code: Int, message: String) :
        Exception("Retrofit Network Exception $code: $message")

    private suspend fun fetchAndParseProducts(
        apiCall: suspend () -> Response<List<ProductBean>>
    ): NetworkResult {
        return try {
            return apiCall().toNetworkResponse()
        } catch (e: Exception) {
            NetworkResult.Error(e)
        }
    }

    private fun Response<List<ProductBean>>.toNetworkResponse() = if (isSuccessful) {
        body()?.let { productBeans ->
            NetworkResult.Success(productBeans.map { it.parse() }.toPersistentList())
        } ?: NetworkResult.Error(
            EmptyBodyException()
        )
    } else {
        NetworkResult.Error(RetrofitNetworkException(code(), message()))
    }
}
