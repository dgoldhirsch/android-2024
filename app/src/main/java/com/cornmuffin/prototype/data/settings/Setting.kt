package com.cornmuffin.prototype.data.settings

sealed interface Setting {
    val name: String
        get() = ""

    val displayableValue: String

    data class BinarySetting(
        override val name: String = "",
        val value: Boolean = false,
    ) : Setting {
        override val displayableValue: String = "$value"
    }
}
