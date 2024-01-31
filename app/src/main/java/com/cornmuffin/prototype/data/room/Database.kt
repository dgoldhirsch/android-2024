package com.cornmuffin.prototype.data.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.cornmuffin.prototype.data.products.Product
import com.cornmuffin.prototype.data.room.products.ProductDao
import com.cornmuffin.prototype.data.room.products.ProductEntity
import com.cornmuffin.prototype.data.room.products.ProductsUpdatedAtDao
import com.cornmuffin.prototype.data.room.products.ProductsUpdatedAtEntity
import com.cornmuffin.prototype.data.room.settings.BinarySettingDao
import com.cornmuffin.prototype.data.room.settings.BinarySettingEntity
import com.cornmuffin.prototype.data.settings.DefaultSettings
import com.cornmuffin.prototype.data.settings.Setting
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Database(
    entities = [
        ProductEntity::class,
        ProductsUpdatedAtEntity::class,
        BinarySettingEntity::class,
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class Database : RoomDatabase() {

    abstract fun getProductDao(): ProductDao
    abstract fun getProductUpdatedAtDao(): ProductsUpdatedAtDao
    abstract fun getBinarySettingDao(): BinarySettingDao

    /**
     * Godlike functions so that callers don't have to work through Dao objects.
     * God-objects are considered to be an anti-pattern, and this would be true if this app
     * had different areas of code ownership or lots of modules.  It has neither, however,
     * and since we don't have to worry about different developers trying to edit this one
     * God object, we can get the benefit of funneling all DB access through this single facade.
     */

    /**
     * Products
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

    /**
     * Settings
     */
    fun binarySettings(): ImmutableList<Setting.BinarySetting> {
        var trimmedAndDefaultedSettings: List<Setting.BinarySetting> = emptyList()

        runInTransaction {
            val foundSettings = getBinarySettingDao().getAll().map { it.toSetting() }
            trimmedAndDefaultedSettings = DefaultSettings.applyTo(foundSettings)

            if (trimmedAndDefaultedSettings.toSet() != foundSettings.toSet()) {
                getBinarySettingDao().clear()
                getBinarySettingDao().insertAll(trimmedAndDefaultedSettings.map { BinarySettingEntity.fromSetting(it) })
            }
        }

        return trimmedAndDefaultedSettings.toPersistentList()
    }

    companion object {
        const val DATABASE_NAME = "com.cornmuffin.prototype"
    }
}
