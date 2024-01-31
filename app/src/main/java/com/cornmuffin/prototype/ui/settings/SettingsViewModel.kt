package com.cornmuffin.prototype.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cornmuffin.prototype.data.settings.Setting
import com.cornmuffin.prototype.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {

    private val container = SettingsContainer(viewModelScope)

    // Action inputs to the state machine, not to be confused with UI user actions.
    sealed interface Action {
        data object Load : Action
        data class ProcessLoadSuccess(val settings: ImmutableList<Setting>) : Action
    }

    // Controls the view model based on current Orbit container state and a given action (event).
    private val stateMachine: (Action) -> Unit = { action ->
        when (container.stateFlow.value.state) {
            SettingsViewModelState.State.UNINITIALIZED -> when (action) { // Default state when state machine is constructed
                is Action.Load -> {
                    container.intent {
                        postSideEffect(SettingsSideEffect.FetchForLoad)
                        reduce { this.state.asLoading() }
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

            else -> { } // SUCCESSFUL
        }
    }

    init {
        listenForSideEffects()
        reduceViewModel(Action.Load)
    }

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

    private fun fetchSettings() = repository.settings
        .flowOn(Dispatchers.IO)
        // TODO catch errors, emit a SettingsResponse.Error object

    private fun listenForSideEffects() {
        viewModelScope.launch {
            container.sideEffectFlow.collect { sideEffect ->
                when (sideEffect) {
                    is SettingsSideEffect.FetchForLoad -> viewModelScope.launch {
                        fetchSettings().collect {
                            reduceViewModel(Action.ProcessLoadSuccess(it))
                        }
                    }
                }
            }
        }
    }
}
