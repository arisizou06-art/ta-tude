package com.tudecitrus.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "disease_info")
data class DiseaseInfoEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "disease_name")
    val diseaseName: String,
    @ColumnInfo(name = "disease_name_id")
    val diseaseNameId: String,
    val description: String,
    val symptoms: String,
    val causes: String,
    val treatment: String,
    val prevention: String,
    @ColumnInfo(name = "severity_level")
    val severityLevel: String
)
