package com.tudecitrus.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "detection_statistics",
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
        Index(value = ["disease_id"], unique = true)
    ]
)
data class DetectionStatisticEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "disease_id")
    val diseaseId: Int,
    @ColumnInfo(name = "total_detections")
    val totalDetections: Int = 0,
    @ColumnInfo(name = "avg_confidence")
    val avgConfidence: Double = 0.0,
    @ColumnInfo(name = "last_detection_date")
    val lastDetectionDate: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String
) {
    init {
        require(totalDetections >= 0) {
            "total_detections tidak boleh negatif"
        }
    }
}
