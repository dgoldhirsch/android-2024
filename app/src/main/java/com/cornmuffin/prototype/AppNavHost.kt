package com.cornmuffin.prototype

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cornmuffin.prototype.ui.products.ProductsLayout
import com.cornmuffin.prototype.ui.products.ProductsViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun AppNavHost(
    navController: NavHostController,
    navigator: Navigator,
) {
    LaunchedEffect("navigation") {
        navigator.sharedFlow.onEach {
            navController.navigate(it.label)
        }.launchIn(this)
    }

    NavHost(
        navController = navController,
        startDestination = "products"
    ) {
        composable("products") {
            ProductsLayout(productUiStateFlow = hiltViewModel<ProductsViewModel>().uiState)
        }
    }
}
