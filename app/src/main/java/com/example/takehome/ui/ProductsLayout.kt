package com.example.takehome.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.takehome.Product
import com.example.takehome.R
import com.example.takehome.roundToNearestHalf

@Composable
fun ProductsLayout(viewModel: ProductsViewModel) {
    val productsUiState by viewModel.uiState.collectAsState()
    when {
        productsUiState.isError -> Error(productsUiState.errorMessage)
        productsUiState.isLoading -> Loading()
        productsUiState.products.isEmpty() -> Empty()
        else -> Products(productsUiState.products)
    }
}

@Composable
fun Empty() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.no_products),
        )
    }
}

@Composable
fun Error(message: String = stringResource(R.string.no_details)) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.error, message),
        )
    }
}

@Composable
fun Loading(text: String = stringResource(R.string.loading)) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = text,
        )
    }
}

@Composable
fun Products(products: List<Product>) {
    LazyColumn {
        items(products) {
            Product(it)
            Divider()
        }
    }
}

@Composable
@Preview
fun EmptyPreview() = Empty()

@Composable
@Preview
fun ErrorPreview() {
    Error("Kneecaps broke")
}

@Composable
@Preview
fun LoadingPreview() {
    Loading()
}

@Composable
@Preview
fun ProductsPreview() {
    Products(
        listOf(
            Product(
                name = "MegaFlame Blow Torch",
                tagline = "Once you've used this on them, they won't have much more to say to you or anyone else.",
                rating = 4.5,
                date = "1-13-2018",
            ),
        ),
    )
}

@Composable
private fun Product(product: Product) {
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(product.name)
        Text(product.tagline)
        Text(product.rating.roundToNearestHalf().toString())
        Text(product.date)
    }
}
