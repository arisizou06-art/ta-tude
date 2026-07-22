package com.tudecitrus.feature.history.data

import android.content.Context
import com.tudecitrus.data.model.DiseaseInfoEntity
import com.tudecitrus.data.repository.DetectionRepository
import com.tudecitrus.data.repository.DiseaseRepository
import com.tudecitrus.feature.history.model.HistoryDetailUiModel
import com.tudecitrus.feature.history.model.HistoryFilterCategory
import com.tudecitrus.feature.history.model.HistoryListItem
import com.tudecitrus.feature.history.model.applyHistoryFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RoomHistoryRepository(
    private val detectionRepository: DetectionRepository,
    private val diseaseRepository: DiseaseRepository
) : HistoryRepository {

    override fun observeHistoryItems(): Flow<List<HistoryListItem>> {
        return combine(
            detectionRepository.observeHistory(),
            diseaseRepository.observeDiseases()
        ) { historyList, diseaseList ->
            val diseaseMap = diseaseList.associateBy { it.id }
            historyList
                .mapNotNull { history ->
                    val disease = diseaseMap[history.diseaseId] ?: return@mapNotNull null
                    HistoryListItem(
                        id = history.id,
                        diseaseId = history.diseaseId,
                        diseaseName = disease.diseaseName,
                        confidenceScore = history.confidenceScore,
                        detectionTimestamp = history.detectionTimestamp,
                        createdAt = history.createdAt,
                        imagePath = history.imagePath,
                        notes = history.notes,
                        category = classifyCategory(disease)
                    )
                }
                .sortedByDescending { it.createdAt }
        }
    }

    override fun observeHistoryDetail(historyId: Long): Flow<HistoryDetailUiModel?> {
        return detectionRepository.observeHistoryDetail(historyId)
            .map { detail ->
                detail?.let {
                    HistoryDetailUiModel(
                        id = it.detectionResult.id,
                        diseaseName = it.diseaseName,
                        confidenceScore = it.detectionResult.confidenceScore,
                        detectionTimestamp = it.detectionResult.detectionTimestamp,
                        createdAt = it.detectionResult.createdAt,
                        imagePath = it.detectionResult.imagePath,
                        treatment = it.treatment,
                        notes = it.detectionResult.notes
                    )
                }
            }
    }

    override suspend fun exportHistoryAsCsv(
        context: Context,
        filter: HistoryFilterCategory
    ): Result<String> {
        return runCatching {
            val allItems = observeHistoryItems().first()
            val filteredItems = applyHistoryFilter(allItems, filter)

            val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
            val filename = "history_export_${timestampForFilename()}.csv"
            val outputFile = File(exportDir, filename)

            outputFile.writeText(buildCsv(filteredItems))
            outputFile.absolutePath
        }
    }

    private fun buildCsv(items: List<HistoryListItem>): String {
        val header = listOf(
            "id",
            "disease_id",
            "disease_name",
            "category",
            "confidence_score",
            "detection_timestamp",
            "created_at",
            "image_path",
            "notes"
        )

        return buildString {
            appendLine(header.joinToString(","))
            items.forEach { item ->
                val row = listOf(
                    item.id.toString(),
                    item.diseaseId.toString(),
                    item.diseaseName,
                    item.category?.label.orEmpty(),
                    item.confidenceScore.toString(),
                    item.detectionTimestamp,
                    item.createdAt,
                    item.imagePath,
                    item.notes.orEmpty()
                )
                appendLine(row.joinToString(",") { escapeCsvValue(it) })
            }
        }
    }

    private fun escapeCsvValue(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    override suspend fun deleteHistoryItem(id: Long) {
        detectionRepository.deleteById(id)
    }

    override suspend fun deleteHistoryItems(ids: List<Long>) {
        detectionRepository.deleteByIds(ids)
    }

    override suspend fun deleteAllHistory() {
        detectionRepository.deleteAll()
    }

    private fun timestampForFilename(): String {
        val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun classifyCategory(disease: DiseaseInfoEntity): HistoryFilterCategory? {
        val content = listOf(
            disease.diseaseName,
            disease.description,
            disease.causes
        ).joinToString(" ").lowercase(Locale.getDefault())

        return when {
            "bakteri" in content || "xanthomonas" in content || "liberibacter" in content -> {
                HistoryFilterCategory.BACTERIA
            }

            "jamur" in content || "phyllosticta" in content || "diaporthe" in content -> {
                HistoryFilterCategory.FUNGUS
            }

            "virus" in content || "viral" in content -> {
                HistoryFilterCategory.VIRUS
            }

            "nutrisi" in content || "defisiensi" in content -> {
                HistoryFilterCategory.NUTRITION
            }

            else -> null
        }
    }
}
