package com.tudecitrus.feature.info.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tudecitrus.data.model.DiseaseInfoEntity
import com.tudecitrus.feature.info.data.BookmarkRepository
import com.tudecitrus.feature.info.data.InfoRepository
import com.tudecitrus.feature.info.model.DiseaseCategory
import com.tudecitrus.feature.info.model.DiseaseDetailUiModel
import com.tudecitrus.feature.info.model.DiseaseListItemUiModel
import com.tudecitrus.feature.info.model.InfoUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class InfoViewModel(
    private val repository: InfoRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow(DiseaseCategory.ALL)
    private val selectedDiseaseDetail = MutableStateFlow<DiseaseDetailUiModel?>(null)
    private val loadingSeed = MutableStateFlow(true)
    private val loadError = MutableStateFlow<String?>(null)
    private val showBookmarksOnly = MutableStateFlow(false)

    private val diseasesByQuery = searchQuery
        .flatMapLatest { query ->
            repository.observeDiseases(query)
        }

    private val baseUiState: Flow<InfoUiState> = combine(
        loadingSeed,
        searchQuery,
        selectedCategory,
        diseasesByQuery,
        combine(
            selectedDiseaseDetail,
            bookmarkRepository.bookmarkedIds,
            showBookmarksOnly
        ) { detail, bookmarks, bookmarksOnly ->
            Triple(detail, bookmarks, bookmarksOnly)
        }
    ) { isSeeding, query, category, diseases, detailBookmarksFlag ->
        val (selectedDetail, bookmarkedIds, bookmarksOnly) = detailBookmarksFlag
        val mapped = diseases.map { it.toListItemUi(bookmarkedIds.contains(it.id)) }
        val byCategory = if (category == DiseaseCategory.ALL) {
            mapped
        } else {
            mapped.filter { it.category == category }
        }
        val filtered = if (bookmarksOnly) {
            byCategory.filter { it.isBookmarked }
        } else {
            byCategory
        }

        // Sync bookmark state ke selectedDetail kalau detail lagi dibuka
        val syncedDetail = selectedDetail?.copy(
            isBookmarked = bookmarkedIds.contains(selectedDetail.id)
        )

        InfoUiState(
            isLoading = isSeeding,
            query = query,
            selectedCategory = category,
            diseases = filtered,
            selectedDisease = syncedDetail,
            showBookmarksOnly = bookmarksOnly,
            errorMessage = null
        )
    }

    val uiState: StateFlow<InfoUiState> = combine(baseUiState, loadError) { state, error ->
        state.copy(errorMessage = error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InfoUiState(isLoading = true)
    )

    init {
        seedDiseaseDataIfNeeded()
    }

    fun onSearchQueryChanged(value: String) {
        searchQuery.value = value
    }

    fun onCategorySelected(category: DiseaseCategory) {
        selectedCategory.value = category
    }

    fun onDiseaseSelected(diseaseId: Int) {
        viewModelScope.launch {
            val disease = repository.getDiseaseById(diseaseId)
            if (disease == null) {
                loadError.value = "Data penyakit tidak ditemukan."
                return@launch
            }
            selectedDiseaseDetail.value = disease.toDetailUi(
                isBookmarked = bookmarkRepository.isBookmarked(disease.id)
            )
            loadError.value = null
        }
    }

    fun onCloseDetail() {
        selectedDiseaseDetail.value = null
    }

    fun onToggleBookmark(diseaseId: Int) {
        bookmarkRepository.toggleBookmark(diseaseId)
    }

    fun onToggleBookmarkFilter() {
        showBookmarksOnly.value = !showBookmarksOnly.value
    }

    private fun seedDiseaseDataIfNeeded() {
        viewModelScope.launch {
            loadingSeed.value = true
            loadError.value = null
            runCatching {
                repository.seedIfEmpty()
            }.onFailure {
                loadError.value = "Gagal memuat data penyakit lokal."
            }
            loadingSeed.value = false
        }
    }

    companion object {
        fun factory(
            repository: InfoRepository,
            bookmarkRepository: BookmarkRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InfoViewModel(repository, bookmarkRepository) as T
                }
            }
        }
    }
}

private fun DiseaseInfoEntity.toListItemUi(isBookmarked: Boolean): DiseaseListItemUiModel {
    return DiseaseListItemUiModel(
        id = id,
        diseaseName = diseaseName,
        diseaseNameId = diseaseNameId,
        shortDescription = description,
        category = DiseaseCategory.fromSeverity(severityLevel),
        isBookmarked = isBookmarked
    )
}

private fun DiseaseInfoEntity.toDetailUi(isBookmarked: Boolean): DiseaseDetailUiModel {
    return DiseaseDetailUiModel(
        id = id,
        diseaseName = diseaseName,
        diseaseNameId = diseaseNameId,
        description = description,
        symptoms = symptoms,
        causes = causes,
        treatment = treatment,
        prevention = prevention,
        severityLevel = severityLevel,
        category = DiseaseCategory.fromSeverity(severityLevel),
        isBookmarked = isBookmarked
    )
}
