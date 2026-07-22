package com.tudecitrus.feature.history.model

enum class HistoryFilterCategory(val label: String) {
    ALL("Semua"),
    BACTERIA("Bakteri"),
    FUNGUS("Jamur"),
    VIRUS("Virus"),
    NUTRITION("Nutrisi")
}

data class HistoryListItem(
    val id: Long,
    val diseaseId: Int,
    val diseaseName: String,
    val confidenceScore: Double,
    val detectionTimestamp: String,
    val createdAt: String,
    val imagePath: String,
    val notes: String?,
    val category: HistoryFilterCategory?
)

data class HistoryDetailUiModel(
    val id: Long,
    val diseaseName: String,
    val confidenceScore: Double,
    val detectionTimestamp: String,
    val createdAt: String,
    val imagePath: String,
    val treatment: String,
    val notes: String?
)

data class HistoryUiState(
    val historyItems: List<HistoryListItem> = emptyList(),
    val selectedFilter: HistoryFilterCategory = HistoryFilterCategory.ALL,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val errorMessage: String? = null,
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet()
) {
    val isEmpty: Boolean
        get() = historyItems.isEmpty()
}

fun applyHistoryFilter(
    items: List<HistoryListItem>,
    filter: HistoryFilterCategory
): List<HistoryListItem> {
    if (filter == HistoryFilterCategory.ALL) {
        return items
    }

    return items.filter { it.category == filter }
}
