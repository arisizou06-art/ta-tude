package com.tudecitrus.data.model

/**
 * Hasil agregasi detection_results per penyakit — dipakai untuk menyusun ulang
 * tabel detection_statistics agar selalu sinkron dengan riwayat yang tersimpan.
 */
data class DiseaseAggregate(
    val diseaseId: Int,
    val totalDetections: Int,
    val avgConfidence: Double,
    val lastDetectionDate: String?
)
