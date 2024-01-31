package com.cornmuffin.prototype.ui.settings

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
internal class SettingsContainer(coroutineScope: CoroutineScope) : ContainerHost<SettingsViewModelState, SettingsSideEffect> {
    override val container: Container<SettingsViewModelState, SettingsSideEffect> = coroutineScope.container(
        SettingsViewModelState()
    )

    val stateFlow: StateFlow<SettingsViewModelState>
        get() = container.stateFlow

    val sideEffectFlow: Flow<SettingsSideEffect>
        get() = container.sideEffectFlow
}
