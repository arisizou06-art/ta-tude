package com.tudecitrus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tudecitrus.data.model.DetectionHistoryDetail
import com.tudecitrus.data.model.DetectionResultEntity
import com.tudecitrus.data.model.DiseaseAggregate
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DetectionResultEntity): Long

    @Update
    suspend fun update(item: DetectionResultEntity)

    @Query("DELETE FROM detection_results WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM detection_results WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM detection_results")
    suspend fun deleteAll()

    @Query("SELECT * FROM detection_results ORDER BY created_at DESC")
    fun getAllHistory(): Flow<List<DetectionResultEntity>>

    @Query("SELECT COUNT(*) FROM detection_results")
    fun getHistoryCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM detection_results")
    suspend fun getHistoryCountOnce(): Int

    @Query(
        """
        SELECT dr.*, di.disease_name AS disease_name, di.treatment AS treatment
        FROM detection_results dr
        JOIN disease_info di ON dr.disease_id = di.id
        WHERE dr.id = :id
        """
    )
    fun getHistoryDetail(id: Long): Flow<DetectionHistoryDetail?>

    /**
     * Agregasi per penyakit dari seluruh riwayat: jumlah deteksi, rata-rata keyakinan,
     * dan waktu deteksi terakhir. Sumber kebenaran untuk membangun detection_statistics.
     */
    @Query(
        """
        SELECT disease_id AS diseaseId,
               COUNT(*) AS totalDetections,
               AVG(confidence_score) AS avgConfidence,
               MAX(detection_timestamp) AS lastDetectionDate
        FROM detection_results
        GROUP BY disease_id
        """
    )
    suspend fun getDiseaseAggregates(): List<DiseaseAggregate>
}
