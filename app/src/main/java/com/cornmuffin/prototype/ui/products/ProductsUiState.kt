package com.cornmuffin.prototype.ui.products

import com.cornmuffin.prototype.data.products.Product
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProductsUiState(
    val errorMessage: String = "",
    val isError: Boolean = false,
    val isLoading: Boolean = false,
    val products: ImmutableList<Product> = persistentListOf(),
    val retryNumber: Int = 0,
) {
    fun asError(retryNumber: Int, errorMessage: String): ProductsUiState = copy(
        errorMessage = errorMessage,
        isError = true,
        isLoading = false,
        retryNumber = retryNumber,
    )

    fun asLoading(): ProductsUiState = copy(
        errorMessage = "",
        isError = false,
        isLoading = true,
    )

    fun asSuccess(products: ImmutableList<Product>): ProductsUiState = copy(
        errorMessage = "",
        isError = false,
        isLoading = false,
        products = products,
    )
}
