package com.cornmuffin.prototype.repositories.products

import com.cornmuffin.prototype.repositories.products.networkdatasource.ProductsNetworkDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsRepository @Inject internal constructor(networkDataSource: ProductsNetworkDataSource) {
    val fetchProductsResponse: Flow<ProductsResponse> = networkDataSource.fetchProductsResponse
}
