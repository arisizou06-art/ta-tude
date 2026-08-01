package com.tudecitrus.feature.detection.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tudecitrus.feature.detection.data.DetectionRepository
import com.tudecitrus.feature.detection.model.DetectionResultRecord
import com.tudecitrus.feature.detection.model.DetectionResultUiModel
import com.tudecitrus.feature.detection.model.DetectionUiState
import com.tudecitrus.feature.detection.model.ImageValidationResult
import com.tudecitrus.feature.detection.model.SelectedImage
import com.tudecitrus.feature.detection.service.AIModelService
import com.tudecitrus.feature.detection.service.NotCitrusLeafException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetectionViewModel(
    private val repository: DetectionRepository,
    private val aiModelService: AIModelService
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetectionUiState())
    val uiState: StateFlow<DetectionUiState> = _uiState.asStateFlow()

    fun onImageSelected(image: SelectedImage) {
        _uiState.value = _uiState.value.copy(
            selectedImage = image,
            errorMessage = null,
            lowConfidenceMessage = null
        )
    }

    fun onRetake() {
        _uiState.value = DetectionUiState()
    }

    suspend fun analyzeSelectedImage() {
        val currentImage = _uiState.value.selectedImage
        if (currentImage == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Pilih gambar terlebih dahulu.")
            return
        }

        _uiState.value = _uiState.value.copy(
            isAnalyzing = true,
            analysisStatus = "Memvalidasi gambar…",
            errorMessage = null,
            lowConfidenceMessage = null
        )
        delay(450)

        when (val validation = aiModelService.validateImage(currentImage)) {
            is ImageValidationResult.Invalid -> {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    analysisStatus = null,
                    errorMessage = validation.reason
                )
                return
            }

            ImageValidationResult.Valid -> Unit
        }

        _uiState.value = _uiState.value.copy(analysisStatus = "Mengekstrak fitur daun…")
        delay(650)
        _uiState.value = _uiState.value.copy(analysisStatus = "Menjalankan model AI…")

        val inference = runCatching { aiModelService.runInference(currentImage) }
            .getOrElse { throwable ->
                val message = when (throwable) {
                    is TimeoutCancellationException ->
                        "Proses analisis AI melebihi batas waktu 10 detik. Silakan coba lagi atau gunakan gambar dengan kualitas berbeda."
                    else -> throwable.message ?: "Gagal menjalankan analisis AI."
                }
                // Objek bukan daun jeruk bukan kegagalan sistem, melainkan arahan bagi
                // pengguna, sehingga ditampilkan seperti pesan keyakinan rendah.
                val isNotCitrusLeaf = throwable is NotCitrusLeafException
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    analysisStatus = null,
                    errorMessage = if (isNotCitrusLeaf) null else message,
                    lowConfidenceMessage = if (isNotCitrusLeaf) message else null
                )
                return
            }

        _uiState.value = _uiState.value.copy(analysisStatus = "Menghitung tingkat keyakinan…")
        delay(550)

        if (inference.shouldReject) {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = false,
                analysisStatus = null,
                lowConfidenceMessage = "Confidence terlalu rendah (${formatConfidence(inference.confidence)}). Silakan foto ulang."
            )
            return
        }

        val diseaseDetail = repository.getDiseaseDetailById(inference.diseaseId)
            ?: run {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    analysisStatus = null,
                    errorMessage = "Data penyakit tidak ditemukan."
                )
                return
            }

        _uiState.value = _uiState.value.copy(analysisStatus = "Menyusun hasil…")
        delay(450)

        val timestamp = createTimestamp()
        repository.insertDetectionResult(
            DetectionResultRecord(
                imagePath = currentImage.imagePath,
                diseaseId = inference.diseaseId,
                confidenceScore = inference.confidence,
                detectionTimestamp = timestamp,
                notes = null
            )
        )

        _uiState.value = _uiState.value.copy(
            isAnalyzing = false,
            analysisStatus = null,
            result = DetectionResultUiModel(
                imagePath = currentImage.imagePath,
                diseaseName = diseaseDetail.diseaseName,
                confidence = inference.confidence,
                confidenceWarning = if (inference.shouldShowWarning) {
                    "Hasil ini masih bisa dipakai, tetapi confidence baru ${formatConfidence(inference.confidence)}. Sebaiknya cek ulang atau ambil foto yang lebih jelas."
                } else {
                    null
                },
                description = diseaseDetail.description,
                symptoms = diseaseDetail.symptoms,
                causes = diseaseDetail.causes,
                treatment = diseaseDetail.treatment,
                prevention = diseaseDetail.prevention
            )
        )
    }

    private fun createTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun formatConfidence(confidence: Float): String {
        val percentage = confidence * 100f
        return String.format(Locale.getDefault(), "%.1f%%", percentage)
    }

    companion object {
        fun factory(
            repository: DetectionRepository,
            aiModelService: AIModelService
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DetectionViewModel(repository, aiModelService) as T
                }
            }
        }
    }
}
