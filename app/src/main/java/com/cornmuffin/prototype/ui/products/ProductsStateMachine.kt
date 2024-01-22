package com.cornmuffin.prototype.ui.products

internal data class ProductsStateMachine(
    val control: ProductsStateMachine.(ProductsViewModel.PsmAction, ProductsViewModel.PsmState) -> ProductsViewModel.PsmState
) {
    private var state: ProductsViewModel.PsmState = ProductsViewModel.PsmState.Uninitialized

    fun advance(psmAction: ProductsViewModel.PsmAction) {
        state = control(psmAction, state)
    }
}
