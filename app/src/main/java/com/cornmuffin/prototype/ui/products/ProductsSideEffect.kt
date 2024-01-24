package com.cornmuffin.prototype.ui.products

import com.cornmuffin.prototype.data.products.Product
import kotlinx.collections.immutable.ImmutableList

sealed interface ProductsSideEffect {
    data object FetchForLoad : ProductsSideEffect
    data class PrimeCache(val products: ImmutableList<Product>) : ProductsSideEffect
    data object Refresh: ProductsSideEffect
}
