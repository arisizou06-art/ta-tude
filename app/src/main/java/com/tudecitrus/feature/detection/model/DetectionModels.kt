package com.tudecitrus.feature.detection.model

import android.graphics.Bitmap
import android.net.Uri

// Model final 2026-06 (MobileNetV3, dataset SEIMBANG & bersih 141/kelas, input 300x300):
// akurasi penuh 90,0% (macro-F1 90,8%). Dengan mekanisme penolakan keyakinan-rendah:
// - Reject jika confidence < 75%  -> pada prediksi yang DITERIMA akurasi ~94% (cakupan ~92%)
// - High confidence >= 85%        -> akurasi ~97%
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
