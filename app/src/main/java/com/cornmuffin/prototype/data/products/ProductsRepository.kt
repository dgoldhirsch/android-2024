package com.cornmuffin.prototype.data.products

import com.cornmuffin.prototype.data.products.datasources.networkproducts.ProductsNetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsRepository @Inject internal constructor(networkDataSource: ProductsNetworkDataSource) {
    val products: Flow<ProductsResponse> = flow {
        emit(
            networkDataSource.fetchAndParseProducts()
        )
    }
}
