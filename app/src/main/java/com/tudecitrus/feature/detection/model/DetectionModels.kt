package com.tudecitrus.feature.detection.model

import android.graphics.Bitmap
import android.net.Uri

// Model final (MobileNetV3, dataset seimbang 141/kelas, input 300x300), diuji pada 110 citra uji:
// akurasi 90,00% dengan prediksi tunggal (skema yang dipakai aplikasi ini) / 90,91% dengan TTA;
// macro-F1 90,7%. Ambang di bawah berasal dari analisis akurasi selektif (Tabel 4.2 Bab IV):
// - Reject jika confidence < 75%  -> akurasi 95,8% pada cakupan 86,4% kasus
// - High confidence >= 85%        -> akurasi 97,5% pada cakupan 73,6% kasus
private const val MIN_ACCEPTED_CONFIDENCE_THRESHOLD = 0.75f
private const val HIGH_CONFIDENCE_THRESHOLD = 0.85f

enum class ImageSource {
    CAMERA,
    GALLERY
}

data class SelectedImage(
    val source: ImageSource,
    val uri: Uri? = null,
    val bitmap: Bitmap? = null,
    val mimeType: String? = null,
    val width: Int,
    val height: Int,
    val imagePath: String
)

sealed interface ImageValidationResult {
    data object Valid : ImageValidationResult
    data class Invalid(val reason: String) : ImageValidationResult
}

enum class ConfidenceLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class InferenceResult(
    val diseaseId: Int,
    val confidence: Float
) {
    val confidenceLevel: ConfidenceLevel = when {
        confidence < MIN_ACCEPTED_CONFIDENCE_THRESHOLD -> ConfidenceLevel.LOW
        confidence < HIGH_CONFIDENCE_THRESHOLD -> ConfidenceLevel.MEDIUM
        else -> ConfidenceLevel.HIGH
    }
    val shouldReject: Boolean = confidenceLevel == ConfidenceLevel.LOW
    val shouldShowWarning: Boolean = confidenceLevel == ConfidenceLevel.MEDIUM
}

data class DiseaseDetail(
    val id: Int,
    val diseaseName: String,
    val description: String,
    val symptoms: String,
    val causes: String,
    val treatment: String,
    val prevention: String
)

data class DetectionResultRecord(
    val imagePath: String,
    val diseaseId: Int,
    val confidenceScore: Float,
    val detectionTimestamp: String,
    val notes: String?
)

data class DetectionResultUiModel(
    val imagePath: String,
    val diseaseName: String,
    val confidence: Float,
    val confidenceWarning: String? = null,
    val description: String,
    val symptoms: String,
    val causes: String,
    val treatment: String,
    val prevention: String
)

data class DetectionUiState(
    val selectedImage: SelectedImage? = null,
    val isAnalyzing: Boolean = false,
    val analysisStatus: String? = null,
    val errorMessage: String? = null,
    val lowConfidenceMessage: String? = null,
    val result: DetectionResultUiModel? = null
)
