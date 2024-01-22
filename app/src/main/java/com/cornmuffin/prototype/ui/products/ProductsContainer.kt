package com.cornmuffin.prototype.ui.products

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container

/**
 * Conflate the Orbit [ContainerHost] and [Container] so that the view model doesn't have to
 * implement the ContainerHost interface and thus expose an (overridden) container. Instead,
 * the view model can instantiate one of these conflated objects privately, hiding Orbit from
 * UI and other callers that want to monitor or affect the state of the view model.
 */
internal class ProductsContainer(coroutineScope: CoroutineScope) : ContainerHost<ProductsViewModelState, ProductsSideEffect> {
    override val container: Container<ProductsViewModelState, ProductsSideEffect> = coroutineScope.container(
        ProductsViewModelState()
    )

    val stateFlow: StateFlow<ProductsViewModelState>
        get() = container.stateFlow

    val sideEffectFlow: Flow<ProductsSideEffect>
        get() = container.sideEffectFlow
}
