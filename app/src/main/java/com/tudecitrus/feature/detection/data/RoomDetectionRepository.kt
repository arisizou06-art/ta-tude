package com.tudecitrus.feature.detection.data

import com.tudecitrus.data.local.dao.DetectionResultDao
import com.tudecitrus.data.local.dao.DetectionStatisticsDao
import com.tudecitrus.data.local.dao.DiseaseInfoDao
import com.tudecitrus.data.model.DetectionResultEntity
import com.tudecitrus.data.repository.DetectionStatisticsUpdater
import com.tudecitrus.feature.detection.model.DetectionResultRecord
import com.tudecitrus.feature.detection.model.DiseaseDetail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoomDetectionRepository(
    private val detectionResultDao: DetectionResultDao,
    private val diseaseInfoDao: DiseaseInfoDao,
    detectionStatisticsDao: DetectionStatisticsDao
) : DetectionRepository {

    private val statisticsUpdater = DetectionStatisticsUpdater(detectionResultDao, detectionStatisticsDao)

    override suspend fun insertDetectionResult(record: DetectionResultRecord): Long {
        val now = currentTimestamp()
        val entity = DetectionResultEntity(
            imagePath = record.imagePath,
            diseaseId = record.diseaseId,
            confidenceScore = record.confidenceScore.toDouble(),
            detectionTimestamp = record.detectionTimestamp,
            notes = record.notes,
            createdAt = now,
            updatedAt = now
        )
        val newId = detectionResultDao.insert(entity)
        // Susun ulang rekap statistik dari data terbaru (selalu sinkron dengan riwayat).
        statisticsUpdater.recalculate()
        return newId
    }

    override suspend fun getDiseaseDetailById(diseaseId: Int): DiseaseDetail? {
        val disease = diseaseInfoDao.getDiseaseById(diseaseId) ?: return null
        return DiseaseDetail(
            id = disease.id,
            diseaseName = disease.diseaseName,
            description = disease.description,
            symptoms = disease.symptoms,
            causes = disease.causes,
            treatment = disease.treatment,
            prevention = disease.prevention
        )
    }

    private fun currentTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}
