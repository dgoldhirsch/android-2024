package com.cornmuffin.prototype.data.products.datasources.cacheproducts

import com.cornmuffin.prototype.data.products.Product
import com.cornmuffin.prototype.data.room.AppDatabase
import com.cornmuffin.prototype.util.TimeControl
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductsCacheDataSourceTest {

    private val products = persistentListOf<Product>(mockk())

    private val appDatabase: AppDatabase = mockk {
        every { products() } returns products
        every { productsUpdatedAt() } returns null
        every { resetProductCache() } just Runs
    }

    private val timeControl: TimeControl = mockk {
        every { now() } returns NOW
    }

    private val subject = ProductsCacheDataSource(
        appDatabase = appDatabase,
        timeControl = timeControl,
    )

    @Test
    fun `products - given null updatedAt - resets the cache and returns an empty collection`() {
        val products = subject.products()

        assertEquals(persistentListOf(), products)
        verify { appDatabase.resetProductCache() }
    }

    @Test
    fun `products - given expired updatedAt - resets the cache and returns an empty collection`() {
        every { appDatabase.productsUpdatedAt() } returns TOO_OLD

        val products = subject.products()

        assertEquals(persistentListOf(), products)
        verify { appDatabase.resetProductCache() }
    }

    @Test
    fun `products - given non-expired updatedAt - returns the products`() {
        every { appDatabase.productsUpdatedAt() } returns GOOD

        val products = subject.products()

        assertEquals(this@ProductsCacheDataSourceTest.products, products)
    }

    @Test
    fun `products - given a future updatedAt (presumably only for testing) - returns the products`() {
        every { appDatabase.productsUpdatedAt() } returns FUTURE

        val products = subject.products()

        assertEquals(this@ProductsCacheDataSourceTest.products, products)
    }

    private companion object {
        val NOW: ZonedDateTime = ZonedDateTime.of(
            2024,
            10,
            1,
            0, // hour
            0, // minute
            0, // second
            0, // nanoOfSecond
            ZoneId.systemDefault(),
        )

        // Set the other hard-coded times using that one
        val GOOD: ZonedDateTime = NOW.minus(ProductsCacheDataSource.CACHE_LIFETIME)
        val TOO_OLD: ZonedDateTime = NOW.minus(ProductsCacheDataSource.CACHE_LIFETIME.plus(Duration.ofSeconds(1)))
        val FUTURE: ZonedDateTime = NOW.plus(Duration.ofSeconds(1))
    }
}
