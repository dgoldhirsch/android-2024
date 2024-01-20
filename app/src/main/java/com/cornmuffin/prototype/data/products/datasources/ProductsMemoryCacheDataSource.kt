package com.cornmuffin.prototype.data.products.datasources

import com.cornmuffin.prototype.data.products.Product
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.lang.Long.max
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours

@Singleton
internal class ProductsMemoryCacheDataSource @Inject constructor() {
    private val entries: MutableMap<Product, LocalDateTime> = mutableMapOf()

    fun addAll(newProducts: List<Product>) = synchronized(this) {
        purgeExpiredEntries()

        newProducts.forEach {
            entries[it] = LocalDateTime.now()
        }
    }

    fun clear() = synchronized(this) {
        entries.clear()
    }

    fun products(): ImmutableList<Product> = synchronized(this) {
        purgeExpiredEntries()
        entries.keys.toPersistentList()
    }

    private fun expiredEntries() = entries.filterValues { updatedAt ->
        val hoursSinceUpdated = max(
            0L, // In case 'now' is earlier than updatedAt, presumably for the purpose of testing
            Duration.between(updatedAt, LocalDateTime.now()).toHours(),
        )

        hoursSinceUpdated > EXPIRATION_HOURS
    }

    private fun purgeExpiredEntries() {
        expiredEntries().keys.forEach { entries.remove(it) }
    }

    private companion object {
        val EXPIRATION_HOURS = 24.hours.inWholeHours
    }
}
