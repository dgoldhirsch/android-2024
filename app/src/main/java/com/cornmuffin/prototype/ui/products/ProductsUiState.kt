package com.cornmuffin.prototype.ui.products

import com.cornmuffin.prototype.data.products.Product
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProductsUiState(
    val errorMessage: String = "",
    val isError: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val products: ImmutableList<Product> = persistentListOf(),
) {
    fun asError(message: String): ProductsUiState = copy(
        errorMessage = message,
        isError = true,
        isLoading = false,
        isRefreshing = false,
    )

    fun asLoading(): ProductsUiState = copy(
        errorMessage = "",
        isError = false,
        isLoading = true,
        isRefreshing = false,
    )

    fun asRefreshing(): ProductsUiState = copy(
        errorMessage = "",
        isError = false,
        isLoading = false,
        isRefreshing = true,
    )

    fun asSuccess(products: ImmutableList<Product>): ProductsUiState = copy(
        errorMessage = "",
        isError = false,
        isLoading = false,
        isRefreshing = false,
        products = products,
    )
}
