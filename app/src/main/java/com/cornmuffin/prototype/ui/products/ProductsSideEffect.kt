package com.cornmuffin.prototype.ui.products

sealed interface ProductsSideEffect {
    data object FetchForLoad : ProductsSideEffect
    data object Refresh: ProductsSideEffect
}
