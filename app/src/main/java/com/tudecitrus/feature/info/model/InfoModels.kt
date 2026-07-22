package com.tudecitrus.feature.info.model

enum class DiseaseCategory(val label: String) {
    ALL(label = "Semua"),
    LOW(label = "Rendah"),
    MEDIUM(label = "Sedang"),
    HIGH(label = "Tinggi");

    companion object {
        fun fromSeverity(value: String): DiseaseCategory {
            val normalized = value.trim().lowercase()
            return when {
                normalized.contains("tinggi") || normalized.contains("high") -> HIGH
                normalized.contains("sedang") || normalized.contains("medium") -> MEDIUM
                normalized.contains("rendah") || normalized.contains("low") -> LOW
                else -> MEDIUM
            }
        }
    }
}

data class DiseaseListItemUiModel(
    val id: Int,
    val diseaseName: String,
    val diseaseNameId: String,
    val shortDescription: String,
    val category: DiseaseCategory,
    val isBookmarked: Boolean = false
)

data class DiseaseDetailUiModel(
    val id: Int,
    val diseaseName: String,
    val diseaseNameId: String,
    val description: String,
    val symptoms: String,
    val causes: String,
    val treatment: String,
    val prevention: String,
    val severityLevel: String,
    val category: DiseaseCategory,
    val isBookmarked: Boolean = false
)

data class InfoUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedCategory: DiseaseCategory = DiseaseCategory.ALL,
    val availableCategories: List<DiseaseCategory> = DiseaseCategory.entries,
    val diseases: List<DiseaseListItemUiModel> = emptyList(),
    val selectedDisease: DiseaseDetailUiModel? = null,
    val showBookmarksOnly: Boolean = false,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && diseases.isEmpty() && errorMessage == null
}
