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

class AIModelService(
    private val context: Context,
    private val modelAssetPath: String = DEFAULT_MODEL_ASSET_PATH,
    initialClassLabels: List<String> = DEFAULT_CLASS_LABELS,
    private val lowConfidenceThreshold: Float = LOW_CONFIDENCE_THRESHOLD
) {
    private val interpreterLock = Any()
    private val classLabels: List<String> = loadLabelsFromAssets().ifEmpty { initialClassLabels }

    @Volatile
    private var cachedInterpreter: Interpreter? = null

    fun classify(
        bitmap: Bitmap,
        mimeType: String? = null,
        fileName: String? = null
    ): AIModelResult {
        val validationResult = MobileNetV3ImagePreprocessor.validate(
            bitmap = bitmap,
            mimeType = mimeType,
            fileName = fileName
        )

        if (!validationResult.isValid) {
            return AIModelResult.InvalidInput(
                message = validationResult.message ?: "Input gambar tidak valid."
            )
        }

        return try {
            val interpreter = getOrCreateInterpreter()
            val inputTensor = interpreter.getInputTensor(0)
                ?: return AIModelResult.Failure("Tensor input model tidak tersedia.")

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
                ?: return AIModelResult.Failure("Tensor output model tidak tersedia.")

            val outputShape = outputTensor.shape()
            val outputSize = outputShape.lastOrNull()?.takeIf { it > 0 }
                ?: return AIModelResult.Failure("Ukuran output model tidak valid.")

            val probabilities = when (outputTensor.dataType()) {
                DataType.UINT8 -> {
                    val quantizedOutput = Array(1) { ByteArray(outputSize) }
                    interpreter.run(inputBuffer, quantizedOutput)
                    dequantizeOutput(
                        values = quantizedOutput.first(),
                        scale = outputTensor.quantizationParams().scale,
                        zeroPoint = outputTensor.quantizationParams().zeroPoint
                    )
                }

                else -> {
                    val floatOutput = Array(1) { FloatArray(outputSize) }
                    interpreter.run(inputBuffer, floatOutput)
                    floatOutput.first()
                }
            }

            val bestIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            val confidence = probabilities.getOrElse(bestIndex) { 0f }.coerceIn(0f, 1f)
            val label = classLabels.getOrElse(bestIndex) { UNKNOWN_DISEASE }

            val status = if (confidence < lowConfidenceThreshold) {
                ClassificationStatus.LOW_CONFIDENCE
            } else {
                ClassificationStatus.CONFIDENT
            }

            val allProbs = classLabels.mapIndexed { index, name ->
                name to probabilities.getOrElse(index) { 0f }.coerceIn(0f, 1f)
            }.toMap()

            AIModelResult.Success(
                ClassificationOutput(
                    diseaseName = label,
                    confidence = confidence,
                    status = status,
                    allProbabilities = allProbs
                )
            )
        } catch (exception: Exception) {
            AIModelResult.Failure(
                message = "Inferensi model gagal dijalankan: ${exception.message ?: "unknown error"}",
                cause = exception
            )
        }
    }

    fun close() {
        synchronized(interpreterLock) {
            val interpreter = cachedInterpreter ?: return
            runCatching { interpreter.close() }
            cachedInterpreter = null
        }
    }

    private fun getOrCreateInterpreter(): Interpreter {
        cachedInterpreter?.let { return it }

        synchronized(interpreterLock) {
            cachedInterpreter?.let { return it }

            val modelBuffer = loadModelFile(modelAssetPath)
            val interpreter = Interpreter(
                modelBuffer,
                Interpreter.Options().apply {
                    setNumThreads(4)
                }
            )

            cachedInterpreter = interpreter
            return interpreter
        }
    }

    private fun loadModelFile(assetPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(assetPath)
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }

    private fun loadLabelsFromAssets(): List<String> {
        return runCatching {
            context.assets.open(LABELS_ASSET_PATH).use { input ->
                BufferedReader(InputStreamReader(input)).readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }
        }.getOrDefault(emptyList())
    }

    private fun dequantizeOutput(
        values: ByteArray,
        scale: Float,
        zeroPoint: Int
    ): FloatArray {
        return FloatArray(values.size) { index ->
            ((values[index].toInt() and 0xFF) - zeroPoint) * scale
        }
    }

    companion object {
        // Selaras dengan MIN_ACCEPTED_CONFIDENCE_THRESHOLD di DetectionModels.kt
        // (sumber kebenaran keputusan tolak). Dijaga sama agar tidak ada dua ambang berbeda.
        const val LOW_CONFIDENCE_THRESHOLD = 0.75f
        const val DEFAULT_MODEL_ASSET_PATH = "models/citrus_mobilenet_v3.tflite"
        const val LABELS_ASSET_PATH = "models/labels.txt"

        private const val UNKNOWN_DISEASE = "Unknown"

        val DEFAULT_CLASS_LABELS = listOf(
            "blackspot",
            "canker",
            "greening",
            "healthy",
            "melanose"
        )
    }
}
