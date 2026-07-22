package com.tudecitrus.data.repository

import com.tudecitrus.data.local.dao.DetectionResultDao
import com.tudecitrus.data.local.dao.DetectionStatisticsDao
import com.tudecitrus.data.model.DetectionHistoryDetail
import com.tudecitrus.data.model.DetectionResultEntity
import kotlinx.coroutines.flow.Flow

class DetectionRepository(
    private val detectionResultDao: DetectionResultDao,
    detectionStatisticsDao: DetectionStatisticsDao
) {
    private val statisticsUpdater = DetectionStatisticsUpdater(detectionResultDao, detectionStatisticsDao)

    suspend fun saveDetection(item: DetectionResultEntity): Long {
        require(item.confidenceScore in 0.0..1.0) {
            "confidence_score harus dalam rentang 0.0..1.0"
        }
        val id = detectionResultDao.insert(item)
        statisticsUpdater.recalculate()
        return id
    }

    suspend fun updateDetection(item: DetectionResultEntity) {
        require(item.confidenceScore in 0.0..1.0) {
            "confidence_score harus dalam rentang 0.0..1.0"
        }
        detectionResultDao.update(item)
        statisticsUpdater.recalculate()
    }

    fun observeHistory(): Flow<List<DetectionResultEntity>> = detectionResultDao.getAllHistory()

    fun observeHistoryCount(): Flow<Int> = detectionResultDao.getHistoryCount()

    suspend fun getHistoryCountOnce(): Int = detectionResultDao.getHistoryCountOnce()

    fun observeHistoryDetail(id: Long): Flow<DetectionHistoryDetail?> =
        detectionResultDao.getHistoryDetail(id)

    suspend fun deleteById(id: Long) {
        detectionResultDao.deleteById(id)
        statisticsUpdater.recalculate()
    }

    suspend fun deleteByIds(ids: List<Long>) {
        detectionResultDao.deleteByIds(ids)
        statisticsUpdater.recalculate()
    }

    suspend fun deleteAll() {
        detectionResultDao.deleteAll()
        statisticsUpdater.recalculate()
    }
}
