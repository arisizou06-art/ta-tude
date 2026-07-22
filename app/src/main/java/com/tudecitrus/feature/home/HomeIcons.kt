package com.tudecitrus.feature.home

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class HomeIconKind { DETECT, INFO, HISTORY }

@Composable
fun HomeIconTile(kind: HomeIconKind, modifier: Modifier = Modifier) {
    val bg: Color
    val fg: Color
    when (kind) {
        HomeIconKind.DETECT -> { bg = Color(0xFFE2F7E6); fg = Color(0xFF22A33E) }
        HomeIconKind.INFO -> { bg = Color(0xFFE3F0FF); fg = Color(0xFF2F6FED) }
        HomeIconKind.HISTORY -> { bg = Color(0xFFEFE7FF); fg = Color(0xFF7C4DFF) }
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(26.dp)) {
            when (kind) {
                HomeIconKind.DETECT -> drawCamera(fg)
                HomeIconKind.INFO -> drawBook(fg)
                HomeIconKind.HISTORY -> drawClock(fg)
            }
        }
    }
}

@Composable
fun ChevronRight(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        val w = size.width; val h = size.height
        drawLine(color, Offset(w * 0.40f, h * 0.28f), Offset(w * 0.64f, h * 0.5f), strokeWidth = w * 0.13f, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.64f, h * 0.5f), Offset(w * 0.40f, h * 0.72f), strokeWidth = w * 0.13f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawCamera(fg: Color) {
    val w = size.width; val h = size.height
    drawRoundRect(fg, Offset(w * 0.36f, h * 0.20f), Size(w * 0.24f, h * 0.14f), CornerRadius(w * 0.04f))
    drawRoundRect(fg, Offset(w * 0.08f, h * 0.30f), Size(w * 0.84f, h * 0.52f), CornerRadius(w * 0.12f))
    drawCircle(Color.White, radius = w * 0.18f, center = Offset(w * 0.5f, h * 0.56f))
    drawCircle(fg, radius = w * 0.10f, center = Offset(w * 0.5f, h * 0.56f))
}

private fun DrawScope.drawBook(fg: Color) {
    val w = size.width; val h = size.height
    drawRoundRect(fg, Offset(w * 0.18f, h * 0.16f), Size(w * 0.64f, h * 0.68f), CornerRadius(w * 0.06f))
    drawLine(Color.White, Offset(w * 0.33f, h * 0.16f), Offset(w * 0.33f, h * 0.84f), strokeWidth = w * 0.045f)
    listOf(0.36f, 0.52f, 0.68f).forEach { fy ->
        drawLine(Color.White, Offset(w * 0.44f, h * fy), Offset(w * 0.72f, h * fy), strokeWidth = w * 0.05f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawClock(fg: Color) {
    val w = size.width; val h = size.height
    val c = Offset(w * 0.5f, h * 0.5f)
    val r = w * 0.34f
    drawCircle(fg, radius = r, center = c, style = Stroke(width = w * 0.10f))
    drawLine(fg, c, Offset(c.x, c.y - r * 0.55f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
    drawLine(fg, c, Offset(c.x + r * 0.42f, c.y), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
    drawCircle(fg, radius = w * 0.05f, center = c)
}
