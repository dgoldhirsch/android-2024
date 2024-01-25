package com.cornmuffin.prototype.data.room.products

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * There is only one row in this table
 */
@Entity
data class ProductsUpdatedAtEntity(
    @PrimaryKey @ColumnInfo(name = "updatedAt") val updatedAt: String,
)
