package com.cornmuffin.prototype.data.room.products

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductsUpdatedAtDao {
    @Query("DELETE FROM productsUpdatedAtEntity")
    fun clear()

    @Query("SELECT * from productsUpdatedAtEntity LIMIT 1")
    fun get(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(productsUpdatedAtEntity: ProductsUpdatedAtEntity)
}
