package com.cornmuffin.prototype

import com.cornmuffin.prototype.data.products.Product
import com.cornmuffin.prototype.data.products.datasources.networkproducts.ProductBean
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductBeanTest {

    @Test
    fun `parse - given an empty bean - populates safe values`() {
        assertEquals(
            Product(),
            subject().parse(),
        )
    }

    private fun subject(
        name: String? = null,
        description: String? = null,
    ): ProductBean = ProductBean(
        title = name,
        description = description,
        image = null,
    )
}
