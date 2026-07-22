package com.tudecitrus.feature.info.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

interface DiseaseImageAssetLoader {
    fun load(context: Context, diseaseNameId: String): Bitmap?
}

class LocalDiseaseImageAssetLoader : DiseaseImageAssetLoader {
    override fun load(context: Context, diseaseNameId: String): Bitmap? {
        val normalizedId = diseaseNameId.trim().lowercase()
        val candidates = listOf(
            "diseases/$normalizedId.webp",
            "diseases/$normalizedId.png",
            "diseases/$normalizedId.jpg",
            "diseases/$normalizedId.jpeg",
            "$normalizedId.webp",
            "$normalizedId.png",
            "$normalizedId.jpg",
            "$normalizedId.jpeg"
        )

        candidates.forEach { path ->
            val bitmap = runCatching {
                context.assets.open(path).use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()

            if (bitmap != null) {
                return bitmap
            }
        }

        return null
    }
}
