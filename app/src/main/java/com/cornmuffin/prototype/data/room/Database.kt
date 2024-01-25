package com.cornmuffin.prototype.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cornmuffin.prototype.data.products.Product
import com.cornmuffin.prototype.data.room.products.ProductDao
import com.cornmuffin.prototype.data.room.products.ProductEntity
import com.cornmuffin.prototype.data.room.products.ProductsUpdatedAtEntity
import com.cornmuffin.prototype.data.room.products.ProductsUpdatedAtDao
import kotlinx.collections.immutable.toPersistentList
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Database(
    entities = [
        ProductEntity::class,
        ProductsUpdatedAtEntity::class
    ],
    version = 1
)
abstract class Database : RoomDatabase() {
    abstract fun getProductDao(): ProductDao
    abstract fun getProductUpdatedAtDao(): ProductsUpdatedAtDao

    /**
     * Godlike functions so that callers don't have to work through Dao objects.
     * God-objects are considered to be an anti-pattern, and this would be true if this app
     * had different areas of code ownership or lots of modules.  It has neither, however,
     * and since we don't have to worry about different developers trying to edit this one
     * God object, we can get the benefit of funneling all DB access through this single facade.
     */
    fun products() = getProductDao().getAll().map { it.toProduct() }.toPersistentList()

    fun resetProductCache() = runInTransaction {
        getProductDao().clear()
        getProductUpdatedAtDao().clear()
    }

    fun replaceProductsAndUpdatedAt(products: List<Product>) {
        runInTransaction {
            getProductDao().also { dao ->
                dao.clear()
                dao.insertAll(products.map { ProductEntity.fromProduct(it) })
            }

            getProductUpdatedAtDao().set(
                ProductsUpdatedAtEntity(ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
            )
        }
    }

    fun productsUpdatedAt(): ZonedDateTime? = getProductUpdatedAtDao().get()?.let {
        ZonedDateTime.parse(it)
    }

    companion object {
        const val DATABASE_NAME = "com.cornmuffin.prototype"
    }
}
