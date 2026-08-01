package com.tudecitrus.feature.detection.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.tudecitrus.domain.ai.AIModelResult
import com.tudecitrus.domain.ai.CitrusLeafValidator
import com.tudecitrus.domain.ai.LeafValidationResult
import com.tudecitrus.feature.detection.model.ImageValidationResult
import com.tudecitrus.feature.detection.model.InferenceResult
import com.tudecitrus.feature.detection.model.SelectedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

interface AIModelService {
    suspend fun validateImage(image: SelectedImage): ImageValidationResult
    suspend fun runInference(image: SelectedImage): InferenceResult
}

/**
 * Ditandai terpisah agar lapisan UI dapat menampilkan pesan arahan (foto daun jeruk)
 * alih-alih pesan kegagalan teknis.
 */
class NotCitrusLeafException(message: String) : IllegalStateException(message)

class LocalAIModelService(
    private val context: Context
) : AIModelService {
    private val modelService = com.tudecitrus.domain.ai.AIModelService(context)
    private val leafValidator = CitrusLeafValidator(context)

    override suspend fun validateImage(image: SelectedImage): ImageValidationResult {
        val mimeType = image.mimeType?.lowercase().orEmpty()
        val isValidMimeType = mimeType.contains("jpeg") || mimeType.contains("jpg") || mimeType.contains("png")
        if (!isValidMimeType) {
            return ImageValidationResult.Invalid("Format gambar tidak didukung. Gunakan JPG/PNG.")
        }

        if (image.width < 200 || image.height < 200) {
            return ImageValidationResult.Invalid("Ukuran gambar terlalu kecil. Minimal 200 x 200 piksel.")
        }

        return ImageValidationResult.Valid
    }

    override suspend fun runInference(image: SelectedImage): InferenceResult {
        // Timeout 10 detik sesuai requirement proposal:
        // "Model AI harus dapat memproses dalam waktu maksimal 10 detik"
        return withTimeout(MODEL_INFERENCE_TIMEOUT_MS) {
            withContext(Dispatchers.Default) {
                runInferenceInternal(image)
            }
        }
    }

    private fun runInferenceInternal(image: SelectedImage): InferenceResult {
        val bitmap = image.bitmap ?: decodeBitmapFromUri(image.uri)
            ?: throw IllegalStateException("Gambar tidak dapat dibaca.")

        // TAHAP 1 - Validasi objek: pastikan citra memang daun jeruk sebelum diklasifikasikan
        // penyakitnya. Tanpa tahap ini, softmax model penyakit memaksa setiap citra masuk ke
        // salah satu dari lima kelas. Bila berkas validator belum dipasang, hasilnya
        // Unavailable dan alur lama tetap berjalan.
        when (val validation = leafValidator.validate(bitmap)) {
            is LeafValidationResult.NotCitrusLeaf -> throw NotCitrusLeafException(
                "Objek pada foto tidak dikenali sebagai daun jeruk. " +
                    "Silakan foto daun jeruk dengan jelas."
            )

            is LeafValidationResult.CitrusLeaf -> android.util.Log.d(
                "Inference",
                "Validator: daun jeruk (${"%.2f".format(validation.probability)})"
            )

            LeafValidationResult.Unavailable -> Unit
        }

        // TAHAP 2 - Klasifikasi penyakit. Satu kali inferensi tanpa augmentasi; TTA pernah
        // dicoba namun menurunkan akurasi test (89,7% -> 87,6%) dan 5x lebih lambat.
        val result = modelService.classify(
            bitmap = bitmap,
            mimeType = image.mimeType,
            fileName = extractFileName(image)
        )

        val probabilities = when (result) {
            is AIModelResult.Success -> result.output.allProbabilities
            is AIModelResult.InvalidInput -> throw IllegalStateException(result.message)
            is AIModelResult.Failure -> throw IllegalStateException(result.message, result.cause)
        }

        val topLabel = probabilities.maxByOrNull { it.value }?.key
            ?: throw IllegalStateException("Output AI kosong.")

        val diseaseId = diseaseNameToId(topLabel)
            ?: throw IllegalStateException(
                "Label model tidak dikenali: '$topLabel'. Periksa labels.txt dan mapping disease."
            )

        val confidence = probabilities[topLabel] ?: 0f

        android.util.Log.d(
            "Inference",
            "Probs: ${probabilities.entries.joinToString { "${it.key}=${"%.2f".format(it.value)}" }} -> $topLabel"
        )

        return InferenceResult(
            diseaseId = diseaseId,
            confidence = confidence
        )
    }

    private fun decodeBitmapFromUri(uri: Uri?): Bitmap? {
        if (uri == null) return null
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }.getOrNull()
    }

    private fun extractFileName(image: SelectedImage): String? {
        if (image.imagePath.isBlank()) return null
        return runCatching { File(image.imagePath).name }.getOrNull()
    }

    private fun diseaseNameToId(name: String): Int? {
        return when (normalizeDiseaseName(name)) {
            "citruscanker", "canker", "kankerjeruk" -> 1
            "greeninghlb", "greening", "huanglongbinghlb", "cvpdhlb" -> 2
            "citrusblackspot", "blackspot", "bercakhitamjeruk" -> 3
            "melanosis", "melanose", "melanosisjeruk" -> 4
            "healthy", "sehat", "daunsehat" -> 5
            else -> null
        }
    }

    private fun normalizeDiseaseName(name: String): String {
        return name
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "")
    }

    companion object {
        // Timeout untuk satu kali inferensi model (10 detik sesuai proposal)
        const val MODEL_INFERENCE_TIMEOUT_MS = 10_000L
    }
}
