package com.cornmuffin.prototype.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cornmuffin.prototype.data.room.products.ProductDao
import com.cornmuffin.prototype.data.room.products.ProductEntity

@Database(entities = [ProductEntity::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun getProductDao(): ProductDao

    companion object {
        const val DATABASE_NAME = "com.cornmuffin.prototype"
    }
}
