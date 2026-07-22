package com.tudecitrus.feature.history.viewmodel

import android.content.Context
import com.tudecitrus.feature.history.data.HistoryRepository
import com.tudecitrus.feature.history.model.HistoryDetailUiModel
import com.tudecitrus.feature.history.model.HistoryFilterCategory
import com.tudecitrus.feature.history.model.HistoryUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.tudecitrus.feature.history.model.applyHistoryFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val repository: HistoryRepository
) {
    private val selectedFilter = MutableStateFlow(HistoryFilterCategory.ALL)
    private val isExporting = MutableStateFlow(false)
    private val exportMessage = MutableStateFlow<String?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val selectedHistoryId = MutableStateFlow<Long?>(null)
    private val isSelectionMode = MutableStateFlow(false)
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val isDeleting = MutableStateFlow(false)

    val uiState: Flow<HistoryUiState> = combine(
        combine(
            repository.observeHistoryItems(),
            selectedFilter,
            isExporting,
            exportMessage,
            errorMessage
        ) { items, filter, exporting, successMessage, failureMessage ->
            CoreState(items, filter, exporting, successMessage, failureMessage)
        },
        isSelectionMode,
        selectedIds,
        isDeleting
    ) { (items, filter, exporting, successMessage, failureMessage), selMode, selIds, _ ->
        HistoryUiState(
            historyItems = applyHistoryFilter(items, filter),
            selectedFilter = filter,
            isExporting = exporting,
            exportMessage = successMessage,
            errorMessage = failureMessage,
            isSelectionMode = selMode,
            selectedIds = selIds
        )
    }

    private data class CoreState(
        val items: List<com.tudecitrus.feature.history.model.HistoryListItem>,
        val filter: HistoryFilterCategory,
        val exporting: Boolean,
        val successMessage: String?,
        val failureMessage: String?
    )

    val selectedHistoryDetail: Flow<HistoryDetailUiModel?> = selectedHistoryId.flatMapLatest { id ->
        if (id == null) {
            flowOf(null)
        } else {
            repository.observeHistoryDetail(id)
        }
    }

    fun onFilterChanged(filter: HistoryFilterCategory) {
        selectedFilter.value = filter
    }

    fun onHistoryItemClicked(historyId: Long) {
        selectedHistoryId.value = historyId
    }

    fun dismissDetail() {
        selectedHistoryId.value = null
    }

    fun consumeMessages() {
        exportMessage.value = null
        errorMessage.value = null
    }

    fun enterSelectionMode() {
        isSelectionMode.value = true
    }

    fun exitSelectionMode() {
        isSelectionMode.value = false
        selectedIds.value = emptySet()
    }

    fun toggleItemSelection(id: Long) {
        val current = selectedIds.value
        selectedIds.value = if (id in current) current - id else current + id
    }

    suspend fun deleteSelected() {
        val ids = selectedIds.value
        if (ids.isEmpty()) return
        isDeleting.value = true
        repository.deleteHistoryItems(ids.toList())
        selectedIds.value = emptySet()
        isSelectionMode.value = false
        isDeleting.value = false
    }

    suspend fun deleteAll() {
        isDeleting.value = true
        repository.deleteAllHistory()
        isSelectionMode.value = false
        selectedIds.value = emptySet()
        isDeleting.value = false
    }

    suspend fun exportHistory(context: Context) {
        isExporting.value = true
        exportMessage.value = null
        errorMessage.value = null

        repository.exportHistoryAsCsv(context, selectedFilter.value)
            .onSuccess { path ->
                exportMessage.value = "CSV tersimpan di: $path"
            }
            .onFailure { throwable ->
                errorMessage.value = throwable.message ?: "Gagal mengekspor data CSV."
            }

        isExporting.value = false
    }
}
