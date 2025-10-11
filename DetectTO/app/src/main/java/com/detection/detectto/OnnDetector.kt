package com.detection.detectto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.min

data class DetectionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: RectF
)

class OnnxDetector(
    private val context: Context,
    modelFilePath: String,
    private val inputs: PreviewInputs
) {
    private val env = OrtEnvironment.getEnvironment()
    private val session: OrtSession

    init {
        val modelBytes = java.io.File(modelFilePath).readBytes()
        session = env.createSession(modelBytes)
    }

    /** Run detection and return list of DetectionResult */
    fun detect(bitmap: Bitmap, labels: List<String>): List<DetectionResult> {
        val inputTensor = preprocess(bitmap, inputs.imgWidth, inputs.imgHeight)

        val results = session.run(mapOf("images" to inputTensor))

        results.use {
            val outputRaw = results[0].value
            // The fix is primarily in extractOutput
            val outputArray: Array<FloatArray> = extractOutput(outputRaw, labels.size)

            val detections = parseOutput(outputArray, labels, inputs.confidenceThreshold, inputs.iou)

            val scaleX = bitmap.width.toFloat() / inputs.imgWidth
            val scaleY = bitmap.height.toFloat() / inputs.imgHeight
            detections.forEach { det ->
                det.boundingBox.left *= scaleX
                det.boundingBox.right *= scaleX
                det.boundingBox.top *= scaleY
                det.boundingBox.bottom *= scaleY
            }

            return detections
        }
    }

    /** Draw detection boxes (green) and labels (red) on bitmap */
    fun drawDetections(bitmap: Bitmap, detections: List<DetectionResult>, labelScale: Int): Bitmap {
        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val boxPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = max(2f, bitmap.width / 200f)
            color = android.graphics.Color.GREEN
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = android.graphics.Color.RED
            textSize = 20f * labelScale
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val labelCounts = mutableMapOf<String, Int>()

        detections.forEach { det ->
            canvas.drawRect(det.boundingBox, boxPaint)

            val count = (labelCounts[det.label] ?: 0) + 1
            labelCounts[det.label] = count

            val labelText = "${det.label} $count  %.2f".format(det.confidence)
            val textX = det.boundingBox.left
            val textY = det.boundingBox.top - 5f

            canvas.drawText(labelText, textX, textY, textPaint)
        }

        return mutable
    }

    /** Preprocess bitmap to [1,3,H,W] tensor (Confirmed 0-1 Scaling) */
    private fun preprocess(bitmap: Bitmap, width: Int, height: Int): OnnxTensor {
        val scaled = Bitmap.createScaledBitmap(bitmap, width, height, true)
        val buffer = FloatBuffer.allocate(width * height * 3)
        val pixels = IntArray(width * height)
        scaled.getPixels(pixels, 0, width, 0, 0, width, height)

        val redBuffer = FloatBuffer.allocate(width * height)
        val greenBuffer = FloatBuffer.allocate(width * height)
        val blueBuffer = FloatBuffer.allocate(width * height)

        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f

            redBuffer.put(r)
            greenBuffer.put(g)
            blueBuffer.put(b)
        }

        buffer.put(redBuffer.array())
        buffer.put(greenBuffer.array())
        buffer.put(blueBuffer.array())

        buffer.rewind()
        return OnnxTensor.createTensor(env, buffer, longArrayOf(1, 3, height.toLong(), width.toLong()))
    }

    /** Extract ONNX output reliably into Array<FloatArray> (FIXED: Transpose Logic with correct Feature Count 4+C) */
    private fun extractOutput(outputRaw: Any?, numClasses: Int): Array<FloatArray> {
        // CRITICAL FIX: YOLOv8 output has 4 (bbox) + C (classes). No separate object confidence.
        val expectedFeatureCount = 4 + numClasses

        val output2D: Array<FloatArray> = when (outputRaw) {
            is Array<*> -> {
                val firstElement = outputRaw[0]
                if (firstElement is Array<*>) {
                    @Suppress("UNCHECKED_CAST")
                    firstElement as? Array<FloatArray> ?: throw IllegalArgumentException("Expected inner array to be Array<FloatArray>.")
                } else if (firstElement is FloatArray) {
                    if (firstElement.size % expectedFeatureCount == 0) {
                        Array(firstElement.size / expectedFeatureCount) { i ->
                            FloatArray(expectedFeatureCount) { j -> firstElement[i * expectedFeatureCount + j] }
                        }
                    } else {
                        throw IllegalArgumentException("Flattened output size is not divisible by $expectedFeatureCount.")
                    }
                } else {
                    throw IllegalArgumentException("Unexpected array structure in ONNX output.")
                }
            }
            is FloatArray -> {
                if (outputRaw.size % expectedFeatureCount == 0) {
                    Array(outputRaw.size / expectedFeatureCount) { i ->
                        FloatArray(expectedFeatureCount) { j -> outputRaw[i * expectedFeatureCount + j] }
                    }
                } else {
                    throw IllegalArgumentException("Flattened output size is not divisible by $expectedFeatureCount.")
                }
            }
            else -> throw IllegalArgumentException("Unexpected ONNX output type: ${outputRaw?.javaClass?.name}.")
        }

        if (output2D.isEmpty()) return output2D

        val receivedRows = output2D.size
        val receivedCols = output2D[0].size

        // The error shows: (84 rows, 8400 columns). 84 is the corrected feature count (4+80).
        if (receivedRows == expectedFeatureCount && receivedCols > expectedFeatureCount) {
            val nDetections = receivedCols
            val nFeatures = receivedRows

            // Perform Transpose to [N, Features]
            return Array(nDetections) { i ->
                FloatArray(nFeatures) { j ->
                    output2D[j][i]
                }
            }
        }
        else if (receivedCols == expectedFeatureCount) {
            // Already in [N, Features] format
            return output2D
        }
        else {
            // Line 191 (now around here) will no longer throw for 84 vs 8400.
            throw IllegalArgumentException("Output tensor shape is invalid: $receivedRows rows, $receivedCols columns. Expected Feature count: $expectedFeatureCount.")
        }
    }

    /** Decode output and apply confidence threshold + NMS (FIXED: Simplified for YOLOv8 4+C format) */
    private fun parseOutput(
        output: Array<FloatArray>,
        labels: List<String>,
        confThreshold: Float,
        iouThreshold: Float
    ): MutableList<DetectionResult> {
        val candidates = mutableListOf<DetectionResult>()
        val numClasses = labels.size
        // We know the features are 4 + C
        val expectedSize = 4 + numClasses

        for (row in output) {
            if (row.size < expectedSize) continue

            val x = row[0]
            val y = row[1]
            val w = row[2]
            val h = row[3]

            // YOLOv8: Confidence is MAX(Class Scores)
            val classScores = row.sliceArray(4 until row.size)

            val maxClassIdx = classScores.indices.maxByOrNull { classScores[it] } ?: 0

            if (maxClassIdx >= numClasses) continue

            // For YOLOv8, the object confidence is effectively 1.0, so total confidence is just max class score.
            val conf = classScores[maxClassIdx]

            if (conf >= confThreshold) {
                val rect = RectF(x - w / 2f, y - h / 2f, x + w / 2f, y + h / 2f)
                candidates.add(DetectionResult(labels[maxClassIdx], conf, rect))
            }
        }

        return nonMaxSuppression(candidates, iouThreshold)
    }

    /** Non-Max Suppression per class (No change) */
    private fun nonMaxSuppression(detections: List<DetectionResult>, iouThreshold: Float): MutableList<DetectionResult> {
        val results = mutableListOf<DetectionResult>()
        val sorted = detections.sortedByDescending { it.confidence }
        val picked = BooleanArray(sorted.size) { false }

        for (i in sorted.indices) {
            if (picked[i]) continue
            val a = sorted[i]
            results.add(a)
            for (j in i + 1 until sorted.size) {
                if (picked[j]) continue
                val b = sorted[j]
                if (a.label != b.label) continue
                if (iou(a.boundingBox, b.boundingBox) > iouThreshold) picked[j] = true
            }
        }

        return results
    }

    private fun iou(a: RectF, b: RectF): Float {
        val interLeft = max(a.left, b.left)
        val interTop = max(a.top, b.top)
        val interRight = min(a.right, b.right)
        val interBottom = min(a.bottom, b.bottom)
        val interArea = max(0f, interRight - interLeft) * max(0f, interBottom - interTop)
        val unionArea = (a.width() * a.height()) + (b.width() * b.height()) - interArea
        return if (unionArea > 0f) interArea / unionArea else 0f
    }

    fun close() {
        session.close()
        env.close()
    }
}