package com.tudecitrus.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tudecitrus.data.local.dao.DetectionStatisticsDao
import com.tudecitrus.data.local.dao.DiseaseInfoDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

data class DiseaseStatUi(
    val diseaseName: String,
    val totalDetections: Int,
    val avgConfidencePercent: Int,
    val lastDetectionDate: String?
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val totalDetections: Int = 0,
    val overallAvgConfidencePercent: Int = 0,
    val topDiseaseName: String? = null,
    val perDisease: List<DiseaseStatUi> = emptyList()
)

/**
 * Menyediakan ringkasan statistik deteksi untuk Beranda, dibangun dari tabel
 * detection_statistics (rekap per penyakit) yang digabung dengan nama penyakit
 * dari disease_info. Data mengalir reaktif: setiap deteksi baru langsung ter-update.
 */
class HomeViewModel(
    statisticsDao: DetectionStatisticsDao,
    diseaseInfoDao: DiseaseInfoDao
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        statisticsDao.getAll(),
        diseaseInfoDao.getAllDiseases()
    ) { stats, diseases ->
        val nameById = diseases.associate { it.id to it.diseaseName }
        val active = stats.filter { it.totalDetections > 0 }

        val perDisease = active
            .map { s ->
                DiseaseStatUi(
                    diseaseName = nameById[s.diseaseId] ?: "Tidak diketahui",
                    totalDetections = s.totalDetections,
                    avgConfidencePercent = (s.avgConfidence * 100).roundToInt().coerceIn(0, 100),
                    lastDetectionDate = s.lastDetectionDate
                )
            }
            .sortedByDescending { it.totalDetections }

        val total = active.sumOf { it.totalDetections }
        val overallAvgPercent = if (total > 0) {
            ((active.sumOf { it.avgConfidence * it.totalDetections } / total) * 100)
                .roundToInt().coerceIn(0, 100)
        } else {
            0
        }

        HomeUiState(
            isLoading = false,
            totalDetections = total,
            overallAvgConfidencePercent = overallAvgPercent,
            topDiseaseName = perDisease.firstOrNull()?.diseaseName,
            perDisease = perDisease
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    companion object {
        fun factory(
            statisticsDao: DetectionStatisticsDao,
            diseaseInfoDao: DiseaseInfoDao
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(statisticsDao, diseaseInfoDao) as T
            }
        }
    }
}
