package com.tudecitrus.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "detection_results",
    foreignKeys = [
        ForeignKey(
            entity = DiseaseInfoEntity::class,
            parentColumns = ["id"],
            childColumns = ["disease_id"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["disease_id"]),
        Index(value = ["created_at"])
    ]
)
data class DetectionResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "image_path")
    val imagePath: String,
    @ColumnInfo(name = "disease_id")
    val diseaseId: Int,
    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Double,
    @ColumnInfo(name = "detection_timestamp")
    val detectionTimestamp: String,
    val notes: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String
) {
    init {
        require(confidenceScore in 0.0..1.0) {
            "confidence_score harus dalam rentang 0.0..1.0"
        }
    }
}
