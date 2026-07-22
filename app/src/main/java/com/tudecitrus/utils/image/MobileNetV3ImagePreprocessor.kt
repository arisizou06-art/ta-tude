package com.tudecitrus.utils.image

import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

enum class ImageInvalidReason {
    UNSUPPORTED_FORMAT,
    TOO_SMALL
}

data class ImageValidationResult(
    val isValid: Boolean,
    val reason: ImageInvalidReason? = null,
    val message: String? = null
)

object MobileNetV3ImagePreprocessor {
    private const val MIN_DIMENSION = 200
    private val SUPPORTED_EXTENSIONS = setOf("jpg", "jpeg", "png")
    private val SUPPORTED_MIME_TYPES = setOf("image/jpeg", "image/png")

    fun validate(
        bitmap: Bitmap,
        mimeType: String? = null,
        fileName: String? = null
    ): ImageValidationResult {
        if (!isSupportedFormat(mimeType = mimeType, fileName = fileName)) {
            return ImageValidationResult(
                isValid = false,
                reason = ImageInvalidReason.UNSUPPORTED_FORMAT,
                message = "Format gambar harus JPG atau PNG."
            )
        }

        if (bitmap.width < MIN_DIMENSION || bitmap.height < MIN_DIMENSION) {
            return ImageValidationResult(
                isValid = false,
                reason = ImageInvalidReason.TOO_SMALL,
                message = "Ukuran minimum gambar adalah 200x200 piksel."
            )
        }

        return ImageValidationResult(isValid = true)
    }

    fun toInputBuffer(
        bitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        inputTensorType: DataType
    ): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        return if (inputTensorType == DataType.UINT8) {
            toUint8Buffer(resizedBitmap)
        } else {
            toFloat32Buffer(resizedBitmap)
        }
    }

    private fun toFloat32Buffer(bitmap: Bitmap): ByteBuffer {
        val pixelCount = bitmap.width * bitmap.height
        val buffer = ByteBuffer.allocateDirect(pixelCount * CHANNEL_COUNT * FLOAT_SIZE)
            .order(ByteOrder.nativeOrder())

        val pixels = IntArray(pixelCount)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        pixels.forEach { pixel ->
            // Model di-train dengan include_preprocessing=True (lihat notebook),
            // jadi rescale -1..1 sudah di dalam graph TFLite. Kirim raw [0, 255] float.
            buffer.putFloat(((pixel shr 16) and 0xFF).toFloat())
            buffer.putFloat(((pixel shr 8) and 0xFF).toFloat())
            buffer.putFloat((pixel and 0xFF).toFloat())
        }

        buffer.rewind()
        return buffer
    }

    private fun toUint8Buffer(bitmap: Bitmap): ByteBuffer {
        val pixelCount = bitmap.width * bitmap.height
        val buffer = ByteBuffer.allocateDirect(pixelCount * CHANNEL_COUNT)
            .order(ByteOrder.nativeOrder())

        val pixels = IntArray(pixelCount)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        pixels.forEach { pixel ->
            buffer.put(((pixel shr 16) and 0xFF).toByte())
            buffer.put(((pixel shr 8) and 0xFF).toByte())
            buffer.put((pixel and 0xFF).toByte())
        }

        buffer.rewind()
        return buffer
    }

    private fun isSupportedFormat(mimeType: String?, fileName: String?): Boolean {
        val normalizedMime = mimeType?.trim()?.lowercase()
        if (normalizedMime != null && normalizedMime in SUPPORTED_MIME_TYPES) {
            return true
        }

        val extension = fileName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?.trim()

        return !extension.isNullOrEmpty() && extension in SUPPORTED_EXTENSIONS
    }

    fun fallbackSquareInputSize(): Int {
        return max(MIN_DIMENSION, 224)
    }

    private const val CHANNEL_COUNT = 3
    private const val FLOAT_SIZE = 4
}
