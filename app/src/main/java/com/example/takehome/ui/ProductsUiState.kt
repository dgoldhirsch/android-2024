package com.example.takehome.ui

import com.example.takehome.Product
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProductsUiState(
    val isError: Boolean = false,
    val isLoading: Boolean = true,
    val products: ImmutableList<Product> = persistentListOf(),
    val retryNumber: Int = 0,
) {
    fun asError(retryNumber: Int, errorMessage: String): ProductsUiState = copy(
        isError = true,
        isLoading = false,
        retryNumber = retryNumber,
    )

    fun asLoading(): ProductsUiState = copy(
        isError = false,
        isLoading = true,
    )

    fun asSuccess(products: ImmutableList<Product>): ProductsUiState = copy(
        isError = false,
        isLoading = false,
        products = products,
    )
}
