package com.cornmuffin.prototype.data.settings

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

object DefaultSettings {
    private const val ENABLE_DEBUGGING = "Enable Debugging"

    private val supportedBinarySettings: ImmutableList<Setting.BinarySetting> = persistentListOf(
        Setting.BinarySetting(name = ENABLE_DEBUGGING, value = false)
    )

    fun applyTo(binarySettings: List<Setting.BinarySetting>): ImmutableList<Setting.BinarySetting> {
        val supportedNames = supportedBinarySettings.map { it.name }
        val foundNames = binarySettings.map { it.name }

        return buildList {
            addAll(binarySettings.filter { it.name in supportedNames })
            addAll(supportedBinarySettings.filterNot { it.name in foundNames })
        }.toPersistentList()
    }
}
