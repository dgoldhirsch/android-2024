package com.example.takehome.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.example.takehome.Product
import com.example.takehome.R
import com.example.takehome.roundToNearestHalf

@Composable
fun ProductsLayout(viewModel: ProductsViewModel) {
    val productsUiState by viewModel.uiState.collectAsState()
    Products(productsUiState.products)
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
