package com.tudecitrus.data.repository

import com.tudecitrus.data.local.dao.DetectionResultDao
import com.tudecitrus.data.local.dao.DetectionStatisticsDao
import com.tudecitrus.data.model.DetectionStatisticEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Menyusun ulang tabel detection_statistics dari sumber kebenaran detection_results.
 * Dipanggil setiap kali data deteksi bertambah (insert) maupun berkurang (hapus riwayat),
 * sehingga rekap statistik di Beranda selalu sinkron dengan riwayat yang benar-benar ada.
 *
 * Pendekatan hitung-ulang penuh dipilih agar tidak ada penyimpangan (drift): berapa pun
 * item riwayat yang dihapus, statistik dihitung dari nol berdasarkan data yang tersisa.
 */
class DetectionStatisticsUpdater(
    private val detectionResultDao: DetectionResultDao,
    private val detectionStatisticsDao: DetectionStatisticsDao
) {
    suspend fun recalculate() {
        val aggregates = detectionResultDao.getDiseaseAggregates()
        // Kosongkan dulu: penyakit yang seluruh riwayatnya dihapus akan hilang dari statistik.
        detectionStatisticsDao.clearAll()
        if (aggregates.isEmpty()) return

        val now = currentTimestamp()
        aggregates.forEach { agg ->
            detectionStatisticsDao.upsert(
                DetectionStatisticEntity(
                    diseaseId = agg.diseaseId,
                    totalDetections = agg.totalDetections,
                    avgConfidence = agg.avgConfidence,
                    lastDetectionDate = agg.lastDetectionDate,
                    updatedAt = now
                )
            )
        }
    }

    private fun currentTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}
