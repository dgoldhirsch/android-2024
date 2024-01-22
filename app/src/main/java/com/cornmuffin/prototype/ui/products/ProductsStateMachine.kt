package com.cornmuffin.prototype.ui.products

internal data class ProductsStateMachine(
    val control: ProductsStateMachine.(ProductsViewModel.PsmEvent, ProductsViewModel.PsmState) -> ProductsViewModel.PsmState
) {
    private var state: ProductsViewModel.PsmState = ProductsViewModel.PsmState.Uninitialized

    fun advance(psmEvent: ProductsViewModel.PsmEvent) {
        state = control(psmEvent, state)
    }
}
