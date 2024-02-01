package com.cornmuffin.prototype.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cornmuffin.prototype.Navigator
import com.cornmuffin.prototype.data.settings.Settings
import com.cornmuffin.prototype.data.settings.SettingsRepository
import com.cornmuffin.prototype.ui.common.CanGoBack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val navigator: Navigator,
) : CanGoBack, ViewModel() {

    private val container = SettingsContainer(viewModelScope)

    // Action inputs to the state machine, not to be confused with UI user actions.
    sealed interface Action {
        data object Load : Action
        data class ProcessLoadSuccess(val settings: Settings) : Action
        data class UpdateDisk(val newSettings: Settings) : Action
    }

    // Controls the view model based on current Orbit container state and a given action (event).
    private val stateMachine: (Action) -> Unit = { action ->
        when (container.stateFlow.value.state) {
            SettingsViewModelState.State.UNINITIALIZED -> when (action) { // Default state when state machine is constructed
                is Action.Load -> {
                    container.intent {
                        reduce { this.state.asLoading() }
                        postSideEffect(SettingsSideEffect.Load)
                    }
                }

                else -> {}
            }

            SettingsViewModelState.State.LOADING -> when (action) {
                is Action.ProcessLoadSuccess -> {
                    container.intent {
                        reduce { this.state.asSuccess(action.settings) }
                    }
                }

                else -> {}
            }

            SettingsViewModelState.State.SUCCESSFUL -> when (action) {
                is Action.UpdateDisk -> {
                    container.intent {
                        reduce { this.state.asSuccess(action.newSettings) }
                        postSideEffect(SettingsSideEffect.WriteSettingsToDisk(action.newSettings))
                    }
                }

                else -> {}
            }
        }
    }

    init {
        listenForSideEffects()
        reduceViewModel(Action.Load)
    }

    override fun goBack() {
        navigator.navigateTo(Navigator.NavTarget.Back)
    }

    fun settings(): Settings = container.stateFlow.value.settings ?: Settings()

    /**
     * Allow UI and other callers to advance our state based on an action.
     */
    internal fun reduceViewModel(action: Action) {
        stateMachine(action)
    }

    /**
     * Allow UI and other callers to listen to our state flow without having to understand
     * that our state is managed by an Orbit container.
     */
    internal fun stateFlow() = container.stateFlow

    private fun listenForSideEffects() {
        viewModelScope.launch {
            container.sideEffectFlow.collect { sideEffect ->
                when (sideEffect) {
                    is SettingsSideEffect.WriteSettingsToDisk -> viewModelScope.launch {
                        repository.replaceSettings(sideEffect.newSettings)
                    }

                    is SettingsSideEffect.Load -> viewModelScope.launch {
                        repository.initialize()
                        repository.settings.collect {
                            reduceViewModel(Action.ProcessLoadSuccess(it))
                        }
                    }
                }
            }
        }
    }
}
