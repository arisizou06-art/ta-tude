package com.tudecitrus.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class DetectionHistoryDetail(
    @Embedded
    val detectionResult: DetectionResultEntity,
    @ColumnInfo(name = "disease_name")
    val diseaseName: String,
    @ColumnInfo(name = "treatment")
    val treatment: String
)
