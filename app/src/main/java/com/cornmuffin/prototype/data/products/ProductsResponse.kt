package com.cornmuffin.prototype.data.products

import kotlinx.collections.immutable.ImmutableList

sealed interface ProductsResponse {
    data object Loading : ProductsResponse
    data class Success(val data: ImmutableList<Product>) : ProductsResponse
    data class Error(val exception: Throwable) : ProductsResponse
}

object NoProductsException : Exception()
class UnsuccessfulHttpStatusException(message: String) : Exception(message)
