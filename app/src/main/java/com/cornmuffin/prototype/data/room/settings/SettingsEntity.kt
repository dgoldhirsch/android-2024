package com.cornmuffin.prototype.data.room.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cornmuffin.prototype.data.settings.Settings

@Entity
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = ENABLE_DEBUGGING) val enableDebugging: Boolean = false,
) {
    fun toSettings() = Settings(
        isInitialized = true,
        enableDebugging = enableDebugging,
    )

    companion object {
        private const val ENABLE_DEBUGGING = "enable_debugging"

        fun fromSettings(settings: Settings) = SettingsEntity(
            enableDebugging = settings.enableDebugging,
        )
    }
}
