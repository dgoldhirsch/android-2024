package com.example.prototype.repositories.product

import com.example.prototype.Product

interface ProductsResponse {
    object Loading : ProductsResponse
    data class Success(val data: List<Product>) : ProductsResponse
    data class Error(val exception: Throwable) : ProductsResponse
}

object NoProductsException : Exception()
class UnsuccessfulHttpStatusException(message: String) : Exception(message)
