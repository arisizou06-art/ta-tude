package com.tudecitrus.feature.detection.ui

import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tudecitrus.feature.detection.model.DetectionResultUiModel
import com.tudecitrus.feature.detection.model.SelectedImage
import java.util.Locale

private val WarnAmber = Color(0xFFE0A100)

@Composable
fun DetectionResultScreen(
    result: DetectionResultUiModel?,
    selectedImage: SelectedImage?,
    onRetake: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (result == null) return

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hasil Deteksi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            ResultImagePreview(selectedImage = selectedImage)

            Spacer(modifier = Modifier.height(16.dp))
            ConfidenceBadge(confidence = result.confidence)

            Text(
                modifier = Modifier.padding(top = 14.dp),
                text = result.diseaseName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            if (!result.confidenceWarning.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                ConfidenceWarningCard(message = result.confidenceWarning)
            }

            Spacer(modifier = Modifier.height(10.dp))
            SectionBlock(title = "Deskripsi", body = result.description, kind = SectionKind.DESC)
            Spacer(modifier = Modifier.height(8.dp))
            SectionBlock(title = "Gejala", body = result.symptoms, kind = SectionKind.SYMPTOM)
            Spacer(modifier = Modifier.height(8.dp))
            SectionBlock(title = "Penyebab", body = result.causes, kind = SectionKind.CAUSE)
            Spacer(modifier = Modifier.height(8.dp))
            SectionBlock(title = "Penanganan", body = result.treatment, kind = SectionKind.TREATMENT)
            Spacer(modifier = Modifier.height(8.dp))
            SectionBlock(title = "Pencegahan", body = result.prevention, kind = SectionKind.PREVENTION)
        }

        Button(
            onClick = onRetake,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Foto Ulang", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ConfidenceWarningCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Keyakinan Sedang",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: Float) {
    val pct = confidence.coerceIn(0f, 1f)
    // Ambang selaras dengan ConfidenceLevel: HIGH >= 0,85; MEDIUM >= 0,75.
    val ringColor = when {
        pct >= 0.85f -> MaterialTheme.colorScheme.primary
        pct >= 0.75f -> MaterialTheme.colorScheme.secondary
        else -> WarnAmber
    }
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier.size(132.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(
                color = track, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = ringColor, startAngle = -90f, sweepAngle = 360f * pct, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatConfidence(confidence),
                style = MaterialTheme.typography.headlineMedium,
                color = ringColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "KEYAKINAN",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionBlock(title: String, body: String, kind: SectionKind) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionIconTile(kind = kind)
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ResultImagePreview(
    selectedImage: SelectedImage?,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp)),
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            when {
                selectedImage?.bitmap != null -> imageView.setImageBitmap(selectedImage.bitmap)
                selectedImage?.uri != null -> imageView.setImageURI(selectedImage.uri)
                else -> imageView.setImageDrawable(null)
            }
        }
    )
}

private fun formatConfidence(confidence: Float): String {
    return String.format(Locale.getDefault(), "%.0f%%", confidence * 100f)
}
