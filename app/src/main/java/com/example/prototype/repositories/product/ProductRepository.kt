package com.example.prototype.repositories.product

import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class ProductRepository {
    private val productService = ProductRetrofitInstance.productService

    val fetchProductsResponse: Flow<ProductsResponse> = flow {
        emit(
            fetchAndParseProducts { productService.getProducts() },
        )
    }

    private suspend fun fetchAndParseProducts(
        apiCall: suspend () -> Response<List<ProductBean>>
    ): ProductsResponse {
        return try {
            return apiCall().toProductResponse()
        } catch (e: Exception) {
            ProductsResponse.Error(e)
        }
    }

    private fun Response<List<ProductBean>>.toProductResponse() = if (isSuccessful) {
        val body = body().orEmpty()

        if (body.isEmpty()) {
            ProductsResponse.Error(NoProductsException)
        } else {
            ProductsResponse.Success(data = body.map { it.parse() }.toPersistentList())
        }
    } else {
        ProductsResponse.Error(UnsuccessfulHttpStatusException(message = message()))
    }
}
