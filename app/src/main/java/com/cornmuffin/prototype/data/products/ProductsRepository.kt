package com.cornmuffin.prototype.data.products

import com.cornmuffin.prototype.data.products.datasources.cacheproducts.ProductsCacheDataSource
import com.cornmuffin.prototype.data.products.datasources.networkproducts.ProductsNetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsRepository @Inject internal constructor(
    cacheDataSource: ProductsCacheDataSource,
    networkDataSource: ProductsNetworkDataSource,
) {
    val products: Flow<ProductsResponse> = flow {
        val cachedProducts = cacheDataSource.products()

        emit (
            if (cachedProducts.isNotEmpty()) {
                ProductsResponse.Success(data = cachedProducts)
            } else {
                val networkProductResponse = networkDataSource.fetchAndParseProducts()

                if (networkProductResponse is ProductsResponse.Success) {
                    cacheDataSource.replaceAllProducts(networkProductResponse.data)
                }

                networkProductResponse
            }
        )
    }
}
