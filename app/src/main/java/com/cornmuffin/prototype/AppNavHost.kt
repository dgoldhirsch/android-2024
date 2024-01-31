package com.cornmuffin.prototype

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cornmuffin.prototype.ui.products.ProductsLayout
import com.cornmuffin.prototype.ui.settings.SettingsLayout
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun AppNavHost(
    navController: NavHostController,
    navigator: Navigator,
) {
    LaunchedEffect("navigation") {
        navigator.sharedFlow.onEach {
            if (it == Navigator.NavTarget.Back) {
                navController.popBackStack()
            } else {
                navController.navigate(it.label)
            }
        }.launchIn(this)
    }

    NavHost(
        navController = navController,
        startDestination = Navigator.NavTarget.Products.name
    ) {
        composable(Navigator.NavTarget.Products.name) { ProductsLayout() }
        composable(Navigator.NavTarget.Settings.name) { SettingsLayout() }
    }
}
