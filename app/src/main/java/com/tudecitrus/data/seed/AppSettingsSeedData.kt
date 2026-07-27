package com.tudecitrus.data.seed

/**
 * Konfigurasi awal aplikasi yang disimpan pada tabel app_settings.
 *
 * Nilainya mengikuti konstanta yang benar-benar dipakai kode:
 *  - ambang keyakinan  -> DetectionModels.kt
 *  - berkas & ukuran input model -> AIModelService / MobileNetV3ImagePreprocessor
 *  - batas waktu inferensi -> LocalAIModelService
 * sehingga isi tabel selalu konsisten dengan perilaku aplikasi.
 */
object AppSettingsSeedData {
    data class Item(
        val id: Int,
        val key: String,
        val value: String,
        val description: String
    )

    val items: List<Item> = listOf(
        Item(
            id = 1,
            key = "confidence_threshold",
            value = "0.75",
            description = "Ambang minimum keyakinan agar hasil deteksi ditampilkan kepada pengguna."
        ),
        Item(
            id = 2,
            key = "high_confidence_threshold",
            value = "0.85",
            description = "Ambang keyakinan tinggi; di bawah nilai ini hasil disertai peringatan."
        ),
        Item(
            id = 3,
            key = "model_name",
            value = "citrus_mobilenet_v3.tflite",
            description = "Berkas model TensorFlow Lite yang dijalankan secara luring di perangkat."
        ),
        Item(
            id = 4,
            key = "model_input_size",
            value = "300",
            description = "Ukuran input model dalam piksel (300 x 300, RGB)."
        ),
        Item(
            id = 5,
            key = "inference_timeout_ms",
            value = "10000",
            description = "Batas waktu maksimal proses inferensi dalam milidetik."
        )
    )
}
