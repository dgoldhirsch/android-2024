package com.cornmuffin.prototype.data.products

import com.cornmuffin.prototype.data.products.datasources.cacheproducts.ProductsCacheDataSource
import com.cornmuffin.prototype.data.products.datasources.networkproducts.ProductsNetworkDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsRepository @Inject internal constructor(
    private val cacheDataSource: ProductsCacheDataSource,
    private val networkDataSource: ProductsNetworkDataSource,
) {
    suspend fun getProducts(): ProductsResponse {
        val cachedProducts = cacheDataSource.products()

        return if (cachedProducts.isNotEmpty()) {
            ProductsResponse.Success(data = cachedProducts)
        } else {
            val networkProductResponse = networkDataSource.fetchAndParseProducts()

            if (networkProductResponse is ProductsResponse.Success) {
                cacheDataSource.replaceAllProducts(networkProductResponse.data)
            }

            networkProductResponse
        }
    }

    fun flushCache() {
        cacheDataSource.replaceAllProducts(emptyList())
    }
}
