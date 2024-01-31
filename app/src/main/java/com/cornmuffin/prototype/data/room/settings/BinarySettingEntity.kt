package com.cornmuffin.prototype.data.room.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cornmuffin.prototype.data.settings.Setting

@Entity
data class BinarySettingEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "value") val value: Boolean
) {
    fun toSetting() = Setting.BinarySetting(
        name = name,
        value = value,
    )

    companion object {
        fun fromSetting(setting: Setting.BinarySetting) = BinarySettingEntity(
            id = setting.hashCode(),
            name = setting.name,
            value = setting.value,
        )
    }
}
