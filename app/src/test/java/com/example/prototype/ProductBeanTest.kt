package com.example.prototype

import com.example.prototype.repositories.ProductBean
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductBeanTest {

    @Test
    fun `asProduct - given an empty bean - populates safe values`() {
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
    )
}
