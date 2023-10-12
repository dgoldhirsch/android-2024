package com.example.takehome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource

@Composable
fun ProductPage(viewModel: ProductPageViewModel) {
    val products by viewModel.products.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchProducts()
    }

    Products(products)
}

@Composable
fun Products(products: List<Product>) {
    Column {
        if (products.isEmpty()) {
            Text(stringResource(R.string.Loading___))
        } else {
            LazyColumn {
                items(products) {
                    Product(it)
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun Product(product: Product) {
    Text(product.name)
    Text(product.tagline)
    Text(product.rating.roundToNearestHalf().toString())
}
