package com.tudecitrus.feature.history.data

import android.content.Context
import com.tudecitrus.feature.history.model.HistoryDetailUiModel
import com.tudecitrus.feature.history.model.HistoryFilterCategory
import com.tudecitrus.feature.history.model.HistoryListItem
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observeHistoryItems(): Flow<List<HistoryListItem>>

    fun observeHistoryDetail(historyId: Long): Flow<HistoryDetailUiModel?>

    suspend fun exportHistoryAsCsv(
        context: Context,
        filter: HistoryFilterCategory
    ): Result<String>

    suspend fun deleteHistoryItem(id: Long)

    suspend fun deleteHistoryItems(ids: List<Long>)

    suspend fun deleteAllHistory()
}
