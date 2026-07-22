package com.tudecitrus.feature.info.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tudecitrus.feature.info.data.BookmarkRepository
import com.tudecitrus.feature.info.data.DiseaseImageAssetLoader
import com.tudecitrus.feature.info.data.InfoRepository
import com.tudecitrus.feature.info.data.LocalDiseaseImageAssetLoader
import com.tudecitrus.feature.info.model.DiseaseCategory
import com.tudecitrus.feature.info.model.DiseaseDetailUiModel
import com.tudecitrus.feature.info.model.DiseaseListItemUiModel
import com.tudecitrus.feature.info.model.InfoUiState
import com.tudecitrus.feature.info.presentation.InfoViewModel
import com.tudecitrus.R

@Composable
fun InfoRoute(
    repository: InfoRepository,
    bookmarkRepository: BookmarkRepository,
    modifier: Modifier = Modifier,
    onBackFromDetail: (() -> Unit)? = null
) {
    val viewModel: InfoViewModel = viewModel(
        factory = InfoViewModel.factory(repository, bookmarkRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    InfoScreen(
        modifier = modifier,
        uiState = uiState,
        onSearchChanged = viewModel::onSearchQueryChanged,
        onCategorySelected = viewModel::onCategorySelected,
        onDiseaseSelected = viewModel::onDiseaseSelected,
        onCloseDetail = {
            viewModel.onCloseDetail()
            onBackFromDetail?.invoke()
        },
        onToggleBookmark = viewModel::onToggleBookmark,
        onToggleBookmarkFilter = viewModel::onToggleBookmarkFilter
    )
}

@Composable
fun InfoScreen(
    uiState: InfoUiState,
    onSearchChanged: (String) -> Unit,
    onCategorySelected: (DiseaseCategory) -> Unit,
    onDiseaseSelected: (Int) -> Unit,
    onCloseDetail: () -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onToggleBookmarkFilter: () -> Unit,
    modifier: Modifier = Modifier,
    imageLoader: DiseaseImageAssetLoader = LocalDiseaseImageAssetLoader()
) {
    // Saat detail terbuka, tombol back sistem menutup detail (kembali ke daftar),
    // bukan keluar aplikasi. Ketika di daftar, handler ini nonaktif sehingga back
    // diteruskan ke handler ketuk-2x-untuk-keluar di CitrusCareApp.
    BackHandler(enabled = uiState.selectedDisease != null) {
        onCloseDetail()
    }

    if (uiState.selectedDisease != null) {
        DiseaseDetailContent(
            detail = uiState.selectedDisease,
            onBack = onCloseDetail,
            onToggleBookmark = { onToggleBookmark(uiState.selectedDisease.id) },
            imageLoader = imageLoader,
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.info_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = stringResource(id = R.string.info_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                value = uiState.query,
                onValueChange = onSearchChanged,
                singleLine = true,
                label = { Text(text = stringResource(id = R.string.info_search_hint)) }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.showBookmarksOnly,
                    onClick = onToggleBookmarkFilter,
                    label = { Text(text = "Bookmark") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (uiState.showBookmarksOnly) {
                                Icons.Filled.Bookmark
                            } else {
                                Icons.Outlined.BookmarkBorder
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            LazyRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(uiState.availableCategories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(text = category.label) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    EmptyState(text = uiState.errorMessage)
                }

                uiState.isEmpty -> {
                    EmptyState(
                        text = if (uiState.showBookmarksOnly) {
                            "Belum ada penyakit yang di-bookmark."
                        } else {
                            stringResource(id = R.string.info_empty_search)
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(uiState.diseases, key = { it.id }) { disease ->
                            DiseaseItemCard(
                                item = disease,
                                onClick = { onDiseaseSelected(disease.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiseaseItemCard(
    item: DiseaseListItemUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.diseaseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (item.isBookmarked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = "Bookmarked",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(text = item.category.label) },
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        disabledLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = item.shortDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DiseaseDetailContent(
    detail: DiseaseDetailUiModel,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    imageLoader: DiseaseImageAssetLoader,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, key1 = detail.diseaseNameId) {
        val bitmap = imageLoader.load(context, detail.diseaseNameId)
        value = bitmap?.asImageBitmap()
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack) {
                        Text(text = stringResource(id = R.string.info_detail_back))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                imageVector = if (detail.isBookmarked) {
                                    Icons.Filled.Bookmark
                                } else {
                                    Icons.Outlined.BookmarkBorder
                                },
                                contentDescription = if (detail.isBookmarked) {
                                    "Hapus bookmark"
                                } else {
                                    "Tambah bookmark"
                                },
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { shareDiseaseInfo(context, detail) }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Bagikan info penyakit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(text = detail.category.label) },
                            colors = AssistChipDefaults.assistChipColors(
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                disabledLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
            item {
                Text(
                    text = detail.diseaseName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                if (image != null) {
                    Image(
                        bitmap = image!!,
                        contentDescription = detail.diseaseName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "I", fontWeight = FontWeight.Bold)
                            }
                            Text(
                                modifier = Modifier.padding(top = 10.dp),
                                text = stringResource(id = R.string.info_asset_missing),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { DetailSection(title = stringResource(id = R.string.info_section_description), body = detail.description) }
            item { DetailSection(title = stringResource(id = R.string.info_section_symptoms), body = detail.symptoms) }
            item { DetailSection(title = stringResource(id = R.string.info_section_causes), body = detail.causes) }
            item { DetailSection(title = stringResource(id = R.string.info_section_treatment), body = detail.treatment) }
            item { DetailSection(title = stringResource(id = R.string.info_section_prevention), body = detail.prevention) }
            item { DetailSection(title = stringResource(id = R.string.info_section_severity), body = detail.severityLevel) }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Buka system share sheet dengan format teks info penyakit yang readable.
 */
private fun shareDiseaseInfo(
    context: android.content.Context,
    detail: DiseaseDetailUiModel
) {
    val body = buildString {
        appendLine("ℹ️ Info Penyakit Tanaman Jeruk")
        appendLine()
        appendLine("Nama: ${detail.diseaseName}")
        appendLine("Tingkat Keparahan: ${detail.severityLevel}")
        appendLine()
        appendLine("Deskripsi:")
        appendLine(detail.description)
        appendLine()
        appendLine("Gejala:")
        appendLine(detail.symptoms)
        appendLine()
        appendLine("Penyebab:")
        appendLine(detail.causes)
        appendLine()
        appendLine("Penanganan:")
        appendLine(detail.treatment)
        appendLine()
        appendLine("Pencegahan:")
        appendLine(detail.prevention)
        appendLine()
        appendLine("— Dibagikan dari aplikasi Deteksi Penyakit Tanaman Jeruk")
    }

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Info Penyakit: ${detail.diseaseName}")
        putExtra(Intent.EXTRA_TEXT, body)
    }
    val chooser = Intent.createChooser(sendIntent, "Bagikan info penyakit via")
    context.startActivity(chooser)
}
