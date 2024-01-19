package com.cornmuffin.prototype.data.products.networkdatasource

import com.cornmuffin.prototype.data.products.NoProductsException
import com.cornmuffin.prototype.data.products.ProductsResponse
import com.cornmuffin.prototype.data.products.UnsuccessfulHttpStatusException
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ProductsNetworkDataSource @Inject constructor() {
    private val productService = ProductRetrofitInstance.productService

    val fetchProductsResponse: Flow<ProductsResponse> = flow {
        emit(
            fetchAndParseProducts { productService.getProducts() },
        )
    }

    private suspend fun fetchAndParseProducts(
        apiCall: suspend () -> Response<List<ProductBean>>
    ): ProductsResponse = try {
        apiCall().toProductResponse()
    } catch (e: Exception) {
        ProductsResponse.Error(e)
    }

    private fun Response<List<ProductBean>>.toProductResponse() = if (isSuccessful) {
        val body = body()

        if (body.isNullOrEmpty()) {
            ProductsResponse.Error(NoProductsException)
        } else {
            ProductsResponse.Success(data = body.map { it.parse() }.toPersistentList())
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
}
