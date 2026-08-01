package com.tudecitrus.feature.detection.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.tudecitrus.feature.detection.model.ImageSource
import com.tudecitrus.feature.detection.model.SelectedImage
import com.tudecitrus.feature.detection.viewmodel.DetectionViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@Composable
fun DetectionScreen(
    viewModel: DetectionViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraPath by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val selected = selectedImageFromUri(context, uri, ImageSource.GALLERY)
            viewModel.onImageSelected(selected)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val imageUri = pendingCameraUri
        val imagePath = pendingCameraPath
        pendingCameraUri = null
        pendingCameraPath = null
        if (success && imageUri != null) {
            val selected = selectedImageFromUri(
                context = context,
                uri = imageUri,
                source = ImageSource.CAMERA,
                explicitPath = imagePath
            )
            viewModel.onImageSelected(selected)
        }
    }

    if (state.result != null) {
        DetectionResultScreen(
            result = state.result,
            selectedImage = state.selectedImage,
            onRetake = viewModel::onRetake,
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Pindai Daun Jeruk",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))
            SelectedImagePreview(selectedImage = state.selectedImage)

            Text(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .align(Alignment.CenterHorizontally),
                text = "Arahkan kamera ke daun jeruk",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = {
                    val captureData = createCameraOutputUri(context)
                    val uri = captureData.first
                    pendingCameraUri = uri
                    pendingCameraPath = captureData.second
                    cameraLauncher.launch(uri)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Ambil Foto", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Pilih dari Galeri", fontWeight = FontWeight.SemiBold)
            }

            // Status & pesan ditempatkan tepat di bawah tombol agar langsung terlihat
            // tanpa perlu menggulir; panduan foto (statis) digeser ke bawahnya.
            if (state.isAnalyzing) {
                Spacer(modifier = Modifier.height(12.dp))
                ProcessingCard(status = state.analysisStatus ?: "Memproses…")
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ErrorMessageCard(
                    message = state.errorMessage.orEmpty(),
                    onCopy = { message ->
                        clipboard.nativeClipboard.setText(AnnotatedString(message))
                        Toast.makeText(context, "Error copied", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (state.lowConfidenceMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                NoticeCard(message = state.lowConfidenceMessage.orEmpty())
            }

            PhotoGuidanceCard(modifier = Modifier.padding(top = 12.dp))

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { scope.launch { viewModel.analyzeSelectedImage() } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !state.isAnalyzing
        ) {
            if (state.isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text("Menganalisis…", fontWeight = FontWeight.SemiBold)
            } else {
                Text("Analisis Gambar", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Kartu pemberitahuan (bukan kegagalan sistem): citra ditolak karena bukan daun jeruk
 * atau keyakinan model terlalu rendah. Dibuat mencolok agar pengguna langsung menyadarinya.
 */
@Composable
private fun NoticeCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4D6))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF0B429)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = "Foto belum bisa dianalisis",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7A4F01)
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF7A4F01)
                )
            }
        }
    }
}

@Composable
private fun PhotoGuidanceCard(modifier: Modifier = Modifier) {
    val tips = listOf(
        "Gunakan 1 helai daun saja",
        "Latar belakang polos (mis. kertas putih)",
        "Cahaya terang & merata, hindari bayangan",
        "Daun mengisi sebagian besar layar & fokus tajam"
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Panduan Foto Akurat",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            tips.forEach { tip ->
                Text(
                    modifier = Modifier.padding(vertical = 3.dp),
                    text = "•  $tip",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProcessingCard(status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "Mohon tunggu, AI sedang menganalisis citra daun…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorMessageCard(
    message: String,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terjadi Kesalahan",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                TextButton(onClick = { onCopy(message) }) {
                    Text("Salin")
                }
            }
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun SelectedImagePreview(
    selectedImage: SelectedImage?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    if (selectedImage == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(280.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                .background(MaterialTheme.colorScheme.surfaceVariant, shape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Belum ada gambar",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "Ambil foto atau pilih dari galeri",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        return
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            when {
                selectedImage.bitmap != null -> imageView.setImageBitmap(selectedImage.bitmap)
                selectedImage.uri != null -> imageView.setImageURI(selectedImage.uri)
                else -> imageView.setImageDrawable(null)
            }
        }
    )
}

private fun selectedImageFromUri(
    context: Context,
    uri: Uri,
    source: ImageSource,
    explicitPath: String? = null
): SelectedImage {
    val mimeType = context.contentResolver.getType(uri)
    val (width, height) = decodeImageBounds(context, uri) ?: Pair(0, 0)
    val bitmap = decodeBitmap(context, uri)

    // Gambar dari galeri disalin ke storage permanen supaya tetap bisa dibuka di Riwayat
    // (URI galeri hanya berizin sementara).
    val resolvedPath = explicitPath
        ?: if (source == ImageSource.GALLERY) {
            persistPickedImage(context, uri) ?: uri.toString()
        } else {
            uri.toString()
        }

    return SelectedImage(
        source = source,
        uri = uri,
        bitmap = bitmap,
        mimeType = mimeType,
        width = if (bitmap != null) bitmap.width else width,
        height = if (bitmap != null) bitmap.height else height,
        imagePath = resolvedPath
    )
}

private fun persistPickedImage(context: Context, uri: Uri): String? {
    return runCatching {
        val dir = File(context.filesDir, "captures").apply { if (!exists()) mkdirs() }
        val file = File(dir, "pick_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    }.getOrNull()
}

private fun decodeImageBounds(context: Context, uri: Uri): Pair<Int, Int>? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(input, null, options)
            Pair(options.outWidth, options.outHeight)
        }
    }.getOrNull()
}

private fun decodeBitmap(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        }
    }.getOrNull()
}

private fun createCameraOutputUri(context: Context): Pair<Uri, String> {
    // Simpan ke penyimpanan permanen (filesDir/captures) supaya foto tetap ada di Riwayat,
    // tidak seperti cacheDir yang bisa dihapus sistem.
    val dir = File(context.filesDir, "captures").apply { if (!exists()) mkdirs() }
    val imageFile = File(dir, "capture_${UUID.randomUUID()}.jpg")
    if (!imageFile.exists()) {
        imageFile.createNewFile()
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    return uri to imageFile.absolutePath
}
