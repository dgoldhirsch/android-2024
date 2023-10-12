package com.example.takehome

import kotlin.test.Test
import kotlin.test.assertEquals

class ProductBeanTest {

    @Test
    fun `asProduct - given an empty bean - populates safe values`() {
        assertEquals(
            Product(),
            subject().asProduct(),
        )
    }

    private fun subject(
        name: String? = null,
        tagline: String? = null,
        date: String? = null,
    ): Product.Bean = Product.Bean(
        name = name,
        tagline = tagline,
        date = date,
    )
}
