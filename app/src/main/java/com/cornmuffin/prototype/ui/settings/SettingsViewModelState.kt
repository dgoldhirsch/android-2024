package com.cornmuffin.prototype.ui.settings

import com.cornmuffin.prototype.data.settings.Setting
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SettingsViewModelState(
    val state: State = State.UNINITIALIZED,
    val settings: ImmutableList<Setting> = persistentListOf(),
) {

    fun asLoading() = SettingsViewModelState(state = State.LOADING)

    fun asSuccess(settings: ImmutableList<Setting>) = SettingsViewModelState(
        state = State.SUCCESSFUL,
        settings = settings,
    )

    enum class State {
        LOADING,
        SUCCESSFUL,
        UNINITIALIZED,
    }
}
