package com.cornmuffin.prototype.ui.products

import com.cornmuffin.prototype.data.products.Product
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProductsUiState(
    val state: State = State.UNINITIALIZED,
    val errorMessage: String = "",
    val products: ImmutableList<Product> = persistentListOf(),
) {
    fun asError(message: String) = copy(
        state = State.ERROR,
        errorMessage = message,
    )

    fun asLoading() = ProductsUiState(state = State.LOADING)
    fun asRefreshing() = copy(state = State.REFRESHING)

    fun asSuccess(products: ImmutableList<Product>) = ProductsUiState(
        state = State.SUCCESSFUL,
        products = products,
    )

    enum class State {
        ERROR,
        LOADING,
        REFRESHING,
        SUCCESSFUL,
        UNINITIALIZED,
    }
}
