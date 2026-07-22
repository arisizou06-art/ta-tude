package com.tudecitrus.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tudecitrus.data.local.AppDatabase
import com.tudecitrus.feature.detection.data.RoomDetectionRepository
import com.tudecitrus.feature.detection.service.LocalAIModelService
import com.tudecitrus.feature.detection.ui.DetectionScreen
import com.tudecitrus.feature.detection.viewmodel.DetectionViewModel
import com.tudecitrus.feature.history.HistoryRoute
import com.tudecitrus.feature.home.HomeRoute
import com.tudecitrus.feature.info.data.DefaultInfoRepository
import com.tudecitrus.feature.info.data.SharedPrefsBookmarkRepository
import com.tudecitrus.feature.info.ui.InfoRoute
import com.tudecitrus.feature.splash.SplashScreen

@Composable
fun CitrusCareApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var selectedDestination by rememberSaveable { mutableStateOf(MainDestination.HOME) }

    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }

    val detectionRepository = remember {
        RoomDetectionRepository(
            detectionResultDao = database.detectionResultDao(),
            diseaseInfoDao = database.diseaseInfoDao(),
            detectionStatisticsDao = database.detectionStatisticsDao()
        )
    }
    val aiModelService = remember { LocalAIModelService(context) }

    val detectionViewModel: DetectionViewModel = viewModel(
        factory = DetectionViewModel.factory(
            repository = detectionRepository,
            aiModelService = aiModelService
        )
    )

    val infoRepository = remember { DefaultInfoRepository(database.diseaseInfoDao()) }
    val bookmarkRepository = remember { SharedPrefsBookmarkRepository(context) }

    if (showSplash) {
        SplashScreen(
            onSplashCompleted = {
                showSplash = false
                selectedDestination = MainDestination.HOME
            }
        )
        return
    }

    // Ketuk tombol back 2x (dalam 2 detik) untuk keluar aplikasi. Handler ini
    // nonaktif otomatis saat detail Info terbuka karena BackHandler di InfoScreen
    // (yang komposisinya lebih dalam) berprioritas lebih tinggi.
    val activity = context as? Activity
    var lastBackMillis by remember { mutableStateOf(0L) }
    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackMillis < 2000L) {
            activity?.finish()
        } else {
            lastBackMillis = now
            Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedDestination = selectedDestination,
                onDestinationSelected = { destination -> selectedDestination = destination }
            )
        }
    ) { innerPadding ->
        when (selectedDestination) {
            MainDestination.HOME -> {
                HomeRoute(
                    statisticsDao = database.detectionStatisticsDao(),
                    diseaseInfoDao = database.diseaseInfoDao(),
                    onStartDetection = { selectedDestination = MainDestination.DETECTION },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            MainDestination.DETECTION -> {
                DetectionScreen(
                    viewModel = detectionViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            MainDestination.INFO -> {
                InfoRoute(
                    repository = infoRepository,
                    bookmarkRepository = bookmarkRepository,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            MainDestination.HISTORY -> {
                HistoryRoute(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedDestination: MainDestination,
    onDestinationSelected: (MainDestination) -> Unit
) {
    NavigationBar {
        MainDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == selectedDestination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = {
                    Text(text = destination.label)
                }
            )
        }
    }
}
