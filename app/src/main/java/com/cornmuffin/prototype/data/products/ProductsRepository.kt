package com.cornmuffin.prototype.data.products

import com.cornmuffin.prototype.data.products.networkdatasource.ProductsNetworkDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsRepository @Inject internal constructor(networkDataSource: ProductsNetworkDataSource) {
    val fetchProductsResponse: Flow<ProductsResponse> = networkDataSource.fetchProductsResponse
}
