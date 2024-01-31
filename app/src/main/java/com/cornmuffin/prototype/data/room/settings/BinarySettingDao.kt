package com.cornmuffin.prototype.data.room.settings

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BinarySettingDao {

    @Query("DELETE FROM binarySettingEntity")
    fun clear()

    @Query("SELECT * from binarySettingEntity")
    fun getAll(): List<BinarySettingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(settings: List<BinarySettingEntity>)

    @Delete
    fun delete(setting: BinarySettingEntity)
}
