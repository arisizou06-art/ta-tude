package com.tudecitrus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tudecitrus.data.model.DetectionStatisticEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionStatisticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: DetectionStatisticEntity): Long

    @Update
    suspend fun update(item: DetectionStatisticEntity)

    @Query("SELECT * FROM detection_statistics WHERE disease_id = :diseaseId LIMIT 1")
    suspend fun getByDiseaseId(diseaseId: Int): DetectionStatisticEntity?

    @Query("SELECT * FROM detection_statistics ORDER BY updated_at DESC")
    fun getAll(): Flow<List<DetectionStatisticEntity>>

    @Query("DELETE FROM detection_statistics")
    suspend fun clearAll()
}
