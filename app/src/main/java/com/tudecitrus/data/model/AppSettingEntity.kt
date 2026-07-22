package com.tudecitrus.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_settings",
    indices = [Index(value = ["setting_key"], unique = true)]
)
data class AppSettingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "setting_key")
    val settingKey: String,
    @ColumnInfo(name = "setting_value")
    val settingValue: String,
    val description: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
