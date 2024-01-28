package com.cornmuffin.prototype.ui.products

internal sealed interface ProductsSideEffect {
    data object FetchForLoad : ProductsSideEffect
    data object Refresh: ProductsSideEffect
}
