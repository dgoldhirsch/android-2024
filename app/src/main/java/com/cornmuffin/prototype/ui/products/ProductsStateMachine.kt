package com.cornmuffin.prototype.ui.products

internal data class ProductsStateMachine(
    val control: ProductsStateMachine.(ProductsViewModel.PsmAction) -> Unit
) {
    fun reduce(psmAction: ProductsViewModel.PsmAction) = control(psmAction)
}
