package com.cornmuffin.prototype.data.products.datasources.networkproducts

import com.cornmuffin.prototype.data.products.NoProductsException
import com.cornmuffin.prototype.data.products.ProductsResponse
import com.cornmuffin.prototype.data.products.UnsuccessfulHttpStatusException
import kotlinx.collections.immutable.toPersistentList
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
internal class ProductsNetworkDataSource @Inject constructor() {
    private var cursor: Int = 0 // hack, to emulate getting different data each fetch

    suspend fun fetchAndParseProducts(): ProductsResponse = try {
        ProductRetrofitInstance.productService.getProducts().toProductResponse()
    } catch (e: Exception) {
        ProductsResponse.Error(e)
    }

    private fun Response<List<ProductBean>>.toProductResponse() = if (isSuccessful) {
        val body = body()

        if (body.isNullOrEmpty()) {
            ProductsResponse.Error(NoProductsException)
        } else {
            // Emulate getting different product beans each request
            val chunkRange = cursor..min(cursor + CHUNK_SIZE, body.size - 1)
            val chunkOfProductBeans = body.slice(chunkRange)
            cursor = (cursor + chunkRange.last + 1) % body.size
            ProductsResponse.Success(data = chunkOfProductBeans.map { it.parse() }.toPersistentList())
        }

    } else {
        ProductsResponse.Error(UnsuccessfulHttpStatusException(message = message()))
    }

    private object ProductRetrofitInstance {
        private const val BASE_URL = "https://fakestoreapi.com/"

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val productService: ProductService = retrofit.create(ProductService::class.java)

        interface ProductService {
            @GET("products")
            suspend fun getProducts(): Response<List<ProductBean>>
        }
    }

    private companion object {
        const val CHUNK_SIZE = 3
    }
}
