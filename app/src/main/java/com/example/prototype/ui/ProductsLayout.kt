package com.example.prototype.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
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
import com.example.prototype.Product
import com.example.prototype.R
import com.example.prototype.roundToNearestHalf

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
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
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
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
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
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
fun Loading(text: String = stringResource(R.string.loading)) {
    LazyColumn(
        contentPadding = PaddingValues(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        item {
            Text(
                textAlign = TextAlign.Center,
                text = text,
            )
        }

        // Surely, there's better way to get some space between the two column items?
        item {
            Text(text = "   ")
        }

        item {
            CircularProgressIndicator(
                modifier = Modifier.width(32.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }
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
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
fun ProductsPreview() {
    Products(
        listOf(
            Product(
                title = "MegaFlame Blow Torch",
                description = "Once you've used this on them, they won't have much more to say to you or anyone else.",
                rating = 4.5,
                count = 123,
            ),
            Product(
                title = "Ronco Pocket Harpoon",
                description = "Gets the job done as nothing else can.",
                rating = 2.3,
                count = 3,
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
        Text(product.title)
        Text(product.description)
        Text(
            stringResource(
                id = R.string.rating,
                product.rating.roundToNearestHalf(),
                product.count
            )
        )
    }
}
