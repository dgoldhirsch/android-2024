package com.cornmuffin.prototype.ui.products

internal data class ProductsStateMachine(
    val control: (ProductsViewModel.Action, ProductsViewModel.State) -> ProductsViewModel.State
) {
    private var state: ProductsViewModel.State = ProductsViewModel.State.Uninitialized

    fun reduce(action: ProductsViewModel.Action) {
        state = control(action, state)
    }

}
