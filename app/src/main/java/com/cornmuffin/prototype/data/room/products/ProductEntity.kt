package com.cornmuffin.prototype.data.room.products

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cornmuffin.prototype.data.products.Product

@Entity
data class ProductEntity(
    @PrimaryKey val pid: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "rating") val rating: Double,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "image") val image: String?,
) {
    companion object {
        fun fromProduct(product: Product) = ProductEntity(
            pid = product.hashCode(),
            title = product.title,
            description = product.description,
            rating = product.rating,
            count = product.count,
            image = product.image,
        )
    }
}
