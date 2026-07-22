package com.tudecitrus.domain.ai

enum class ClassificationStatus {
    CONFIDENT,
    LOW_CONFIDENCE
}

data class ClassificationOutput(
    val diseaseName: String,
    val confidence: Float,
    val status: ClassificationStatus,
    /**
     * Full probability vector keyed by class label name (as defined in labels.txt).
     * Exposed agar layer feature bisa lakukan post-processing (mis. tie-breaker
     * blackspot vs greening menggunakan analisis citra tambahan).
     */
    val allProbabilities: Map<String, Float> = emptyMap()
)

sealed class AIModelResult {
    data class Success(val output: ClassificationOutput) : AIModelResult()

    data class InvalidInput(val message: String) : AIModelResult()

    data class Failure(val message: String, val cause: Throwable? = null) : AIModelResult()
}
