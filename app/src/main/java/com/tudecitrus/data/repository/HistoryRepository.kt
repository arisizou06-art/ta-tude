package com.tudecitrus.data.repository

import com.tudecitrus.data.local.dao.DetectionResultDao
import com.tudecitrus.data.model.DetectionHistoryDetail
import com.tudecitrus.data.model.DetectionResultEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepository(
    private val detectionResultDao: DetectionResultDao
) {
    fun observeHistoryList(): Flow<List<DetectionResultEntity>> =
        detectionResultDao.getAllHistory()

    fun observeHistoryCount(): Flow<Int> = detectionResultDao.getHistoryCount()

    fun observeHistoryDetail(id: Long): Flow<DetectionHistoryDetail?> =
        detectionResultDao.getHistoryDetail(id)

    suspend fun deleteHistoryById(id: Long) {
        detectionResultDao.deleteById(id)
    }
}
