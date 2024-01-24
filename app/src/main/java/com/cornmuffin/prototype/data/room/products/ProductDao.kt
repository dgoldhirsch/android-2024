package com.cornmuffin.prototype.data.room.products

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cornmuffin.prototype.data.products.Product

@Dao
interface ProductDao {
    @Query("SELECT * from productEntity")
    fun getAll(): List<Product>

    @Query("SELECT * from productEntity WHERE pid IN (:pids)")
    fun loadAllByIds(pids: IntArray): List<ProductEntity>

    // For this database containing this one table, the following is fine.
    // As noted in https://betterprogramming.pub/upserting-in-room-8207a100db53,
    // however, if there are other tables in which the products were part of a
    // potentially cascading deletion, we'd much rather use UPSERT than
    // to delete/insert.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(products: List<ProductEntity>)

    @Delete
    fun delete(product: ProductEntity)

    @Query("DELETE FROM productEntity")
    fun deleteAll()
}
