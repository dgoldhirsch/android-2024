package com.cornmuffin.prototype.data.room.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDao {

    @Query("DELETE FROM settingsEntity")
    fun clear()

    @Query("SELECT * FROM settingsEntity LIMIT 1")
    fun get(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(settings: SettingsEntity)
}
