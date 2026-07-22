package com.tudecitrus.feature.history

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileInputStream
import com.tudecitrus.data.local.AppDatabase
import com.tudecitrus.data.repository.DetectionRepository
import com.tudecitrus.data.repository.DiseaseRepository
import com.tudecitrus.feature.history.data.RoomHistoryRepository
import com.tudecitrus.feature.history.model.HistoryFilterCategory
import com.tudecitrus.feature.history.model.HistoryListItem
import com.tudecitrus.feature.history.model.HistoryUiState
import com.tudecitrus.feature.history.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun HistoryRoute(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = rememberHistoryViewModel()
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState(initial = HistoryUiState())
    val detail by viewModel.selectedHistoryDetail.collectAsState(initial = null)

    HistoryScreen(
        modifier = modifier,
        uiState = uiState,
        onFilterChanged = viewModel::onFilterChanged,
        onHistoryClick = viewModel::onHistoryItemClicked,
        onExportClick = {
            scope.launch {
                viewModel.exportHistory(context)
            }
        },
        onDeleteAll = {
            scope.launch {
                viewModel.deleteAll()
            }
        },
        onEnterSelectionMode = viewModel::enterSelectionMode,
        onExitSelectionMode = viewModel::exitSelectionMode,
        onToggleItemSelection = viewModel::toggleItemSelection,
        onDeleteSelected = {
            scope.launch {
                viewModel.deleteSelected()
            }
        }
    )

    if (detail != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDetail,
            confirmButton = {
                TextButton(onClick = viewModel::dismissDetail) {
                    Text(text = "Tutup")
                }
            },
            title = {
                Text(text = detail?.diseaseName.orEmpty())
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetectionImage(
                        imagePath = detail?.imagePath.orEmpty(),
                        reqPx = 700,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Text(text = "Tingkat Keyakinan: ${formatConfidence(detail?.confidenceScore ?: 0.0)}")
                    Text(text = "Waktu Deteksi: ${detail?.detectionTimestamp.orEmpty()}")
                    Text(text = "Rekomendasi: ${detail?.treatment.orEmpty()}")
                    Text(text = "Catatan: ${detail?.notes.orEmpty().ifBlank { "-" }}")
                }
            }
        )
    }
}

@Composable
private fun rememberHistoryViewModel(): HistoryViewModel {
    val context = LocalContext.current
    return remember {
        val db = AppDatabase.getInstance(context)
        val detectionRepository = DetectionRepository(
            db.detectionResultDao(),
            db.detectionStatisticsDao()
        )
        val diseaseRepository = DiseaseRepository(db.diseaseInfoDao())
        val historyRepository = RoomHistoryRepository(detectionRepository, diseaseRepository)
        HistoryViewModel(historyRepository)
    }
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onFilterChanged: (HistoryFilterCategory) -> Unit,
    onHistoryClick: (Long) -> Unit,
    onExportClick: () -> Unit,
    onDeleteAll: () -> Unit,
    onEnterSelectionMode: () -> Unit,
    onExitSelectionMode: () -> Unit,
    onToggleItemSelection: (Long) -> Unit,
    onDeleteSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Hapus Semua Riwayat") },
            text = { Text("Semua data riwayat deteksi akan dihapus. Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteAllDialog = false
                    onDeleteAll()
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showDeleteSelectedDialog) {
        val count = uiState.selectedIds.size
        AlertDialog(
            onDismissRequest = { showDeleteSelectedDialog = false },
            title = { Text("Hapus $count Riwayat") },
            text = { Text("$count data riwayat terpilih akan dihapus. Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteSelectedDialog = false
                    onDeleteSelected()
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelectedDialog = false }) { Text("Batal") }
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isSelectionMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onExitSelectionMode) {
                        Text("Batal")
                    }
                    Text(
                        text = "${uiState.selectedIds.size} terpilih",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = "Riwayat Deteksi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            FilterSection(
                selectedFilter = uiState.selectedFilter,
                onFilterChanged = onFilterChanged
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onExportClick,
                    enabled = !uiState.isExporting
                ) {
                    Text(text = "Ekspor CSV")
                }

                IconButton(onClick = { showDeleteAllDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Hapus Semua",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                if (!uiState.isEmpty && !uiState.isSelectionMode) {
                    TextButton(onClick = onEnterSelectionMode) {
                        Text("Pilih")
                    }
                }

                if (uiState.isExporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            }

            uiState.exportMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider()

            if (uiState.isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum Ada Riwayat",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                HistoryList(
                    items = uiState.historyItems,
                    isSelectionMode = uiState.isSelectionMode,
                    selectedIds = uiState.selectedIds,
                    onHistoryClick = onHistoryClick,
                    onToggleSelection = onToggleItemSelection
                )
            }
        }

        if (uiState.isSelectionMode && uiState.selectedIds.isNotEmpty()) {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { showDeleteSelectedDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus (${uiState.selectedIds.size})", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    selectedFilter: HistoryFilterCategory,
    onFilterChanged: (HistoryFilterCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Filter Kategori",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(
                items = HistoryFilterCategory.entries.toList(),
                key = { it.name }
            ) { filter ->
                AssistChip(
                    onClick = { onFilterChanged(filter) },
                    label = { Text(filter.label) },
                    enabled = selectedFilter != filter
                )
            }
        }
    }
}

@Composable
private fun HistoryList(
    items: List<HistoryListItem>,
    isSelectionMode: Boolean,
    selectedIds: Set<Long>,
    onHistoryClick: (Long) -> Unit,
    onToggleSelection: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = items, key = { it.id }) { item ->
            val isSelected = item.id in selectedIds
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isSelectionMode) {
                            onToggleSelection(item.id)
                        } else {
                            onHistoryClick(item.id)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelection(item.id) }
                        )
                    }
                    DetectionImage(
                        imagePath = item.imagePath,
                        reqPx = 220,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = item.diseaseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Keyakinan ${formatConfidence(item.confidenceScore)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.detectionTimestamp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetectionImage(
    imagePath: String,
    modifier: Modifier = Modifier,
    reqPx: Int = 400
) {
    val context = LocalContext.current
    val bitmap = remember(imagePath) { decodeDetectionImage(context, imagePath, reqPx) }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Foto daun hasil deteksi",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Gambar\ntidak tersedia",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Decode gambar dari path file ATAU content/file URI, dengan downsample agar hemat memori. */
private fun decodeDetectionImage(context: Context, path: String, reqPx: Int): Bitmap? {
    if (path.isBlank()) return null
    val isUri = path.startsWith("content:") || path.startsWith("file:")
    fun openStream() =
        if (isUri) context.contentResolver.openInputStream(Uri.parse(path))
        else runCatching { FileInputStream(File(path)) }.getOrNull()

    return runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openStream()?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
        var sample = 1
        while (maxDim > 0 && maxDim / sample > reqPx) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        openStream()?.use { BitmapFactory.decodeStream(it, null, opts) }
    }.getOrNull()
}

private fun formatConfidence(confidenceScore: Double): String {
    return String.format(Locale.getDefault(), "%.1f%%", confidenceScore * 100.0)
}
