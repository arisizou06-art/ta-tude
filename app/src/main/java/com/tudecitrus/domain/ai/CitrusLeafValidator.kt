package com.tudecitrus.domain.ai

import android.content.Context
import android.graphics.Bitmap
import com.tudecitrus.utils.image.MobileNetV3ImagePreprocessor
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Hasil validasi objek sebelum klasifikasi penyakit.
 */
sealed interface LeafValidationResult {
    /** Citra dinilai sebagai daun jeruk; lanjut ke model penyakit. */
    data class CitrusLeaf(val probability: Float) : LeafValidationResult

    /** Citra bukan daun jeruk; klasifikasi penyakit tidak dijalankan. */
    data class NotCitrusLeaf(val probability: Float) : LeafValidationResult

    /** Validator tidak tersedia atau gagal; sistem melanjutkan tanpa validasi. */
    data object Unavailable : LeafValidationResult
}

/**
 * Tahap pertama pipeline deteksi: memastikan citra benar-benar daun jeruk sebelum
 * diklasifikasikan penyakitnya. Tanpa tahap ini, lapisan softmax pada model penyakit
 * akan memaksa setiap citra masuk ke salah satu dari lima kelas.
 *
 * Bersifat opsional: bila berkas model validator belum dipasang di assets, kelas ini
 * mengembalikan [LeafValidationResult.Unavailable] sehingga alur deteksi tetap berjalan
 * seperti sebelumnya (fail-open).
 */
class CitrusLeafValidator(
    private val context: Context,
    private val modelAssetPath: String = VALIDATOR_MODEL_ASSET_PATH,
    private val minCitrusProbability: Float = MIN_CITRUS_PROBABILITY
) {
    private val interpreterLock = Any()

    @Volatile
    private var cachedInterpreter: Interpreter? = null

    @Volatile
    private var modelMissing = false

    private val classLabels: List<String> by lazy {
        loadLabelsFromAssets().ifEmpty { DEFAULT_VALIDATOR_LABELS }
    }

    fun validate(bitmap: Bitmap): LeafValidationResult {
        if (modelMissing) return LeafValidationResult.Unavailable

        return try {
            val interpreter = getOrCreateInterpreter() ?: return LeafValidationResult.Unavailable
            val inputTensor = interpreter.getInputTensor(0)
                ?: return LeafValidationResult.Unavailable

            val shape = inputTensor.shape()
            val targetHeight = shape.getOrNull(1) ?: MobileNetV3ImagePreprocessor.fallbackSquareInputSize()
            val targetWidth = shape.getOrNull(2) ?: MobileNetV3ImagePreprocessor.fallbackSquareInputSize()

            val inputBuffer = MobileNetV3ImagePreprocessor.toInputBuffer(
                bitmap = bitmap,
                targetWidth = targetWidth,
                targetHeight = targetHeight,
                inputTensorType = inputTensor.dataType()
            )

            val outputTensor = interpreter.getOutputTensor(0)
                ?: return LeafValidationResult.Unavailable
            val outputSize = outputTensor.shape().lastOrNull()?.takeIf { it > 0 }
                ?: return LeafValidationResult.Unavailable

            val probabilities = when (outputTensor.dataType()) {
                DataType.UINT8 -> {
                    val quantized = Array(1) { ByteArray(outputSize) }
                    interpreter.run(inputBuffer, quantized)
                    val params = outputTensor.quantizationParams()
                    FloatArray(outputSize) { i ->
                        ((quantized.first()[i].toInt() and 0xFF) - params.zeroPoint) * params.scale
                    }
                }

                else -> {
                    val floatOutput = Array(1) { FloatArray(outputSize) }
                    interpreter.run(inputBuffer, floatOutput)
                    floatOutput.first()
                }
            }

            val citrusIndex = classLabels.indexOfFirst { it.equals(CITRUS_LABEL, ignoreCase = true) }
                .takeIf { it >= 0 } ?: (outputSize - 1)
            val citrusProbability = probabilities.getOrElse(citrusIndex) { 0f }.coerceIn(0f, 1f)

            if (citrusProbability >= minCitrusProbability) {
                LeafValidationResult.CitrusLeaf(citrusProbability)
            } else {
                LeafValidationResult.NotCitrusLeaf(citrusProbability)
            }
        } catch (exception: Exception) {
            // Kegagalan validator tidak boleh menghentikan deteksi.
            LeafValidationResult.Unavailable
        }
    }

    fun close() {
        synchronized(interpreterLock) {
            cachedInterpreter?.let { runCatching { it.close() } }
            cachedInterpreter = null
        }
    }

    private fun getOrCreateInterpreter(): Interpreter? {
        cachedInterpreter?.let { return it }

        synchronized(interpreterLock) {
            cachedInterpreter?.let { return it }

            val modelBuffer = runCatching { loadModelFile(modelAssetPath) }.getOrNull()
            if (modelBuffer == null) {
                // Berkas validator belum dipasang: tandai agar tidak dicoba berulang kali.
                modelMissing = true
                return null
            }

            val interpreter = Interpreter(
                modelBuffer,
                Interpreter.Options().apply { setNumThreads(2) }
            )
            cachedInterpreter = interpreter
            return interpreter
        }
    }

    private fun loadModelFile(assetPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(assetPath)
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            return inputStream.channel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }

    private fun loadLabelsFromAssets(): List<String> {
        return runCatching {
            context.assets.open(VALIDATOR_LABELS_ASSET_PATH).use { input ->
                BufferedReader(InputStreamReader(input)).readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        const val VALIDATOR_MODEL_ASSET_PATH = "models/validator_daun_jeruk.tflite"
        const val VALIDATOR_LABELS_ASSET_PATH = "models/labels_validator.txt"

        /**
         * Ambang minimum probabilitas kelas "jeruk".
         *
         * Ditetapkan dari pengujian pada data hold-out yang tidak dipakai saat pelatihan
         * validator (200 citra daun jeruk, 200 daun durian, dan 200 daun padi): pada ambang
         * 0,90 seluruh daun jeruk tetap diterima (100%), sementara daun durian tertolak 94,5%
         * dan daun padi 93,5%. Ambang tidak dinaikkan lebih jauh agar tersisa margin bagi
         * variasi kondisi pemotretan di lapangan.
         */
        const val MIN_CITRUS_PROBABILITY = 0.90f

        private const val CITRUS_LABEL = "jeruk"
        private val DEFAULT_VALIDATOR_LABELS = listOf("bukan_jeruk", "jeruk")
    }
}
