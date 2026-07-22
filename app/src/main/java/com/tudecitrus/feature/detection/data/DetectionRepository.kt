package com.tudecitrus.feature.detection.data

import com.tudecitrus.feature.detection.model.DetectionResultRecord
import com.tudecitrus.feature.detection.model.DiseaseDetail

interface DetectionRepository {
    suspend fun insertDetectionResult(record: DetectionResultRecord): Long
    suspend fun getDiseaseDetailById(diseaseId: Int): DiseaseDetail?
}

