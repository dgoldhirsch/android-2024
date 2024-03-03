package com.cornmuffin.prototype

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cornmuffin.prototype.ui.products.ProductsLayout
import com.cornmuffin.prototype.ui.settings.SettingsLayout

private const val ANIMATION_MILLIS = 700
private val EASING = LinearEasing

@Composable
fun AppNavHost(
    navController: NavHostController,
    navigator: Navigator,
) {
    LaunchedEffect("navigation") {
        navigator.sharedFlow.collect {
            if (it == Navigator.NavTarget.Back) {
                navController.popBackStack()
            } else {
                navController.navigate(it.label)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Navigator.NavTarget.Products.name
    ) {
        val tween: FiniteAnimationSpec<Float> = tween(ANIMATION_MILLIS, 0, EASING)

        composable(
            route = Navigator.NavTarget.Products.name,
            enterTransition = { fadeIn(tween) },
            exitTransition = { fadeOut(tween) },
            popEnterTransition = { fadeIn(tween) },
        ) { ProductsLayout() }

        composable(
            route = Navigator.NavTarget.Settings.name,
            enterTransition = { fadeIn(tween) },
            exitTransition = { fadeOut(tween) },
            popExitTransition = { fadeOut(tween) }
        ) { SettingsLayout() }
    }
}
