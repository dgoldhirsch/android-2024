package com.cornmuffin.prototype.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cornmuffin.prototype.Navigator
import com.cornmuffin.prototype.pages.products.ProductsLayout
import com.cornmuffin.prototype.pages.products.ProductsViewModel
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
            ProductsLayout(productUiStateFlow = viewModel<ProductsViewModel>().uiState)
        }
    }
}
