package com.cornmuffin.prototype.repositories.products

import com.cornmuffin.prototype.repositories.products.networkdatasource.ProductsNetworkDataSource
import kotlinx.coroutines.flow.Flow

class ProductsRepository internal constructor(networkDataSource: ProductsNetworkDataSource) {
    val fetchProductsResponse: Flow<ProductsResponse> = networkDataSource.fetchProductsResponse
}
