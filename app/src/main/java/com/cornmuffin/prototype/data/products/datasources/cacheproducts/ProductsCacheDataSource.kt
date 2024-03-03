package com.cornmuffin.prototype.data.products.datasources.cacheproducts

import androidx.annotation.VisibleForTesting
import com.cornmuffin.prototype.data.products.Product
import com.cornmuffin.prototype.data.room.AppDatabase
import com.cornmuffin.prototype.util.TimeControl
import com.cornmuffin.prototype.util.dawnOfZonedDateTime
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.Duration
import javax.inject.Inject

class ProductsCacheDataSource @Inject constructor(
    private val appDatabase: AppDatabase,
    private val timeControl: TimeControl,
) {
    fun products() = productsUnlessExpired()

    fun replaceAllProducts(products: List<Product>) {
        appDatabase.replaceProductsAndUpdatedAt(products)
    }

    private fun productsUnlessExpired(): ImmutableList<Product> {
        val updatedAt = appDatabase.productsUpdatedAt() ?: dawnOfZonedDateTime()
        val now = timeControl.now()
        val earliestAllowed = now.minus(CACHE_LIFETIME)
        val fromUpdatedAtToEarliestAllowed = Duration.between(updatedAt, earliestAllowed)

        return if (fromUpdatedAtToEarliestAllowed.toMillis() > 0) {
            appDatabase.resetProductCache()
            persistentListOf()
        } else {
            appDatabase.products()
        }
    }

    @VisibleForTesting
    companion object {
        val CACHE_LIFETIME: Duration = Duration.ofMinutes(15)
    }
}
