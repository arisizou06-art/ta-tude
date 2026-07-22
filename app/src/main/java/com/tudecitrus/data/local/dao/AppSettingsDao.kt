package com.tudecitrus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tudecitrus.data.model.AppSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AppSettingEntity): Long

    @Update
    suspend fun update(item: AppSettingEntity)

    @Query("SELECT * FROM app_settings WHERE setting_key = :key LIMIT 1")
    suspend fun getByKey(key: String): AppSettingEntity?

    @Query("SELECT * FROM app_settings ORDER BY setting_key ASC")
    fun getAll(): Flow<List<AppSettingEntity>>
}
