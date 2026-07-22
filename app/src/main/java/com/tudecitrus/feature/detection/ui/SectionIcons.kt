package com.tudecitrus.feature.detection.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** Jenis icon ilustrasi flat berwarna (gaya tile seperti Gojek). */
enum class SectionKind { DESC, SYMPTOM, CAUSE, TREATMENT, PREVENTION }

@Composable
fun SectionIconTile(kind: SectionKind, modifier: Modifier = Modifier) {
    val bg: Color
    val fg: Color
    when (kind) {
        SectionKind.DESC -> { bg = Color(0xFFE3F0FF); fg = Color(0xFF2F6FED) }
        SectionKind.SYMPTOM -> { bg = Color(0xFFFFF1DD); fg = Color(0xFFEF9A00) }
        SectionKind.CAUSE -> { bg = Color(0xFFFFE3E6); fg = Color(0xFFE5484D) }
        SectionKind.TREATMENT -> { bg = Color(0xFFE2F7E6); fg = Color(0xFF22A33E) }
        SectionKind.PREVENTION -> { bg = Color(0xFFD9F3F0); fg = Color(0xFF0E9F8E) }
    }
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            when (kind) {
                SectionKind.DESC -> drawDoc(fg)
                SectionKind.SYMPTOM -> drawLens(fg)
                SectionKind.CAUSE -> drawGerm(fg)
                SectionKind.TREATMENT -> drawCross(fg)
                SectionKind.PREVENTION -> drawShield(fg)
            }
        }
    }
}

// Dokumen: halaman + garis teks
private fun DrawScope.drawDoc(fg: Color) {
    val w = size.width; val h = size.height
    drawRoundRect(
        color = fg,
        topLeft = Offset(w * 0.20f, h * 0.08f),
        size = Size(w * 0.60f, h * 0.84f),
        cornerRadius = CornerRadius(w * 0.10f)
    )
    val lx = w * 0.31f; val lw = w * 0.38f; val lh = h * 0.06f
    listOf(0.30f, 0.48f, 0.66f).forEach { fy ->
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(lx, h * fy),
            size = Size(lw, lh),
            cornerRadius = CornerRadius(lh / 2)
        )
    }
}

// Kaca pembesar: cincin + gagang
private fun DrawScope.drawLens(fg: Color) {
    val r = size.minDimension * 0.28f
    val c = Offset(size.width * 0.42f, size.height * 0.42f)
    drawCircle(color = fg, radius = r, center = c, style = Stroke(width = size.width * 0.12f))
    drawLine(
        color = fg,
        start = Offset(c.x + r * 0.75f, c.y + r * 0.75f),
        end = Offset(size.width * 0.86f, size.height * 0.86f),
        strokeWidth = size.width * 0.14f,
        cap = StrokeCap.Round
    )
}

// Kuman/sel: bulatan + titik-titik
private fun DrawScope.drawGerm(fg: Color) {
    val c = Offset(size.width / 2, size.height / 2)
    drawCircle(color = fg, radius = size.minDimension * 0.34f, center = c)
    val d = size.minDimension * 0.055f
    listOf(
        Offset(-0.11f, -0.08f), Offset(0.10f, -0.02f), Offset(-0.02f, 0.12f), Offset(0.08f, 0.11f)
    ).forEach {
        drawCircle(Color.White, radius = d, center = Offset(c.x + it.x * size.width, c.y + it.y * size.height))
    }
}

// Salib medis (tanda plus membulat)
private fun DrawScope.drawCross(fg: Color) {
    val w = size.width; val h = size.height
    val t = w * 0.26f
    drawRoundRect(fg, Offset(w / 2 - t / 2, h * 0.16f), Size(t, h * 0.68f), CornerRadius(t * 0.35f))
    drawRoundRect(fg, Offset(w * 0.16f, h / 2 - t / 2), Size(w * 0.68f, t), CornerRadius(t * 0.35f))
}

// Perisai + centang
private fun DrawScope.drawShield(fg: Color) {
    val w = size.width; val h = size.height
    val shield = Path().apply {
        moveTo(w * 0.5f, h * 0.08f)
        lineTo(w * 0.86f, h * 0.22f)
        lineTo(w * 0.86f, h * 0.5f)
        quadraticBezierTo(w * 0.86f, h * 0.82f, w * 0.5f, h * 0.93f)
        quadraticBezierTo(w * 0.14f, h * 0.82f, w * 0.14f, h * 0.5f)
        lineTo(w * 0.14f, h * 0.22f)
        close()
    }
    drawPath(shield, fg)
    val check = Path().apply {
        moveTo(w * 0.37f, h * 0.5f)
        lineTo(w * 0.46f, h * 0.6f)
        lineTo(w * 0.65f, h * 0.37f)
    }
    drawPath(check, Color.White, style = Stroke(width = w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
