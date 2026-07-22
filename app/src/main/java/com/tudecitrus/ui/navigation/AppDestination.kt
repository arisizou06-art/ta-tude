package com.tudecitrus.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainDestination(
    val label: String,
    val icon: ImageVector
) {
    HOME(label = "Beranda", icon = Icons.Filled.Home),
    DETECTION(label = "Deteksi", icon = Icons.Filled.CameraAlt),
    INFO(label = "Info", icon = Icons.Filled.Info),
    HISTORY(label = "Riwayat", icon = Icons.Filled.History)
}
