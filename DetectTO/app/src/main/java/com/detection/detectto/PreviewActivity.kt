package com.detection.detectto

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import android.view.ViewGroup

class PreviewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var seekBarLabelScale: SeekBar
    private lateinit var labelScaleText: TextView // Added for label scale display
    private lateinit var widthInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var spinner: Spinner
    private lateinit var confirmBtn: Button
    private lateinit var selectCustomLabelBtn: Button
    private lateinit var seekBarConfidence: SeekBar
    private lateinit var confidenceText: TextView
    private lateinit var backButton: Button

    private lateinit var modelUri: Uri
    private lateinit var imageUri: Uri
    private var customLabelUri: Uri? = null
    private val labelOptions = listOf("Default (COCO Labels)", "Custom Labels")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preview_page)

        bindViews()

        modelUri = intent.getStringExtra("MODEL_URI")?.toUri() ?: error("MODEL_URI missing")
        imageUri = intent.getStringExtra("IMAGE_URI")?.toUri() ?: error("IMAGE_URI missing")
        imageView.setImageURI(imageUri)

        initializeInputs()
        setupSpinner()
        setupCustomLabelPicker()

        confirmBtn.setOnClickListener { runDetectionAsync() }
    }

    private fun bindViews() {
        imageView = findViewById(R.id.imageView3)
        seekBarLabelScale = findViewById(R.id.seekBar2)
        labelScaleText = findViewById(R.id.labelScaleText)

        widthInput = findViewById(R.id.imgWidthInput)
        heightInput = findViewById(R.id.imgHeightInput)
        spinner = findViewById(R.id.spinner)
        confirmBtn = findViewById(R.id.button8)
        selectCustomLabelBtn = findViewById(R.id.selectCustomLabelBtn)
        selectCustomLabelBtn.visibility = View.GONE

        backButton = findViewById(R.id.backButton)

        seekBarConfidence = findViewById(R.id.seekBarConfidence)
        confidenceText = findViewById(R.id.confidenceText)

        seekBarConfidence.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress / 100f
                confidenceText.text = "Confidence / IOU: %.2f".format(value)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Add listener for label scale seek bar text
        seekBarLabelScale.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                labelScaleText.text = "Label Scale: ${progress}x"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // FIX: Set OnClickListener for the back button to navigate back
        backButton.setOnClickListener {
            // This is safer than navigating directly to MainActivity, just goes to the previous screen
            super.onBackPressed()
        }
    }

    private fun initializeInputs() {
        seekBarConfidence.progress = 20
        seekBarLabelScale.progress = 1
        widthInput.setText("640")
        heightInput.setText("640")

        // Initialize the TextViews with default values
        confidenceText.text = "Confidence / IOU: %.2f".format(seekBarConfidence.progress / 100f)
        labelScaleText.text = "Label Scale: ${seekBarLabelScale.progress}x"
    }

    // FIX: Override system back behavior to return to the previous activity
    override fun onBackPressed() {
        // Calling super.onBackPressed() handles the default navigation, which is usually sufficient.
        super.onBackPressed()
    }

    private fun setupSpinner() {
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labelOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                val scale = context.resources.displayMetrics.density
                val layoutParams = view.layoutParams
                layoutParams.width = (200 * scale).toInt()
                layoutParams.height = (48 * scale).toInt()
                view.layoutParams = layoutParams
                val padding = (12 * scale).toInt()
                view.setPadding(padding, padding, padding, padding)
                return view
            }
        }

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectCustomLabelBtn.visibility = if (labelOptions[position] == "Custom Labels") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupCustomLabelPicker() {
        val customLabelPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                customLabelUri = it
                val labels = readLabelsFromUri(it)
                Toast.makeText(this, "Loaded ${labels.size} labels", Toast.LENGTH_SHORT).show()
            }
        }

        selectCustomLabelBtn.setOnClickListener {
            customLabelPicker.launch(arrayOf("text/plain"))
        }
    }

    private fun runDetectionAsync() {
        val inputs = getPreviewInputs()
        val resultId = System.currentTimeMillis().toString()

        // 1. Show a loading indicator (optional but recommended)
        confirmBtn.isEnabled = false // Disable button
        // You might want to show a ProgressBar here (e.g., findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE)


        lifecycleScope.launch {
            val savedImageUri: Uri?
            val simpleResults: ArrayList<SimpleDetectionResult>

            try {
                val labels: List<String> = when (inputs.labelType) {
                    "Custom Labels" -> customLabelUri?.let { readLabelsFromUri(it) }
                        ?: run { Toast.makeText(this@PreviewActivity, "Select custom labels", Toast.LENGTH_SHORT).show(); return@launch }
                    else -> defaultCocoLabels()
                }

                // --- ALL HEAVY WORK MOVED INSIDE THE try BLOCK ---
                val bitmap = withContext(Dispatchers.IO) { imageUri.toBitmap(this@PreviewActivity) }
                val detector = OnnxDetector(this@PreviewActivity, modelUri.toFilePath(this@PreviewActivity)!!, inputs)

                val detectionResults = withContext(Dispatchers.Default) {
                    detector.detect(bitmap, labels)
                }

                val annotatedBitmap = detector.drawDetections(bitmap, detectionResults, inputs.labelScale)
                // ❌ REMOVE: imageView.setImageBitmap(annotatedBitmap)
                detector.close()

                // 3. Save the image (still in Dispatchers.IO)
                savedImageUri = withContext(Dispatchers.IO) {
                    saveBitmapToCache(annotatedBitmap)
                }

                // 4. Correctly number detections
                val labelCounts = mutableMapOf<String, Int>()
                simpleResults = ArrayList(detectionResults.map { det ->
                    val count = (labelCounts[det.label] ?: 0) + 1
                    labelCounts[det.label] = count
                    SimpleDetectionResult("${det.label} $count", det.confidence)
                })
                // --- END heavy work ---

            } catch (e: Exception) {
                Toast.makeText(this@PreviewActivity, "Detection Failed: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
                return@launch
            } finally {
                // Re-enable button and hide progress bar in the finally block
                confirmBtn.isEnabled = true
                // Hide ProgressBar here
            }

            // --- LAUNCH ResultActivity IMMEDIATELY AFTER PROCESSING IS DONE ---

            // Open ResultActivity
            val intent = Intent(this@PreviewActivity, ResultActivity::class.java)

            // Ensure savedImageUri is not null before proceeding
            if (savedImageUri == null) {
                Toast.makeText(this@PreviewActivity, "Failed to save annotated image.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            intent.putExtra("RESULT_ID", resultId)
            intent.putExtra("ANNOTATED_IMAGE_URI", savedImageUri.toString())
            intent.putExtra("MODEL_NAME", modelUri.getFileName(this@PreviewActivity))
            intent.putExtra("LABEL_TYPE", inputs.labelType)
            intent.putExtra("IOU_SIZE", "%.2f".format(inputs.iou))
            intent.putExtra("IMG_SIZE", "${widthInput.text}x${heightInput.text}")
            intent.putParcelableArrayListExtra("RESULTS", simpleResults)

            // 5. Launch the next activity now
            startActivity(intent)
            // Optionally, call finish() if you don't want the user to navigate back to this preview
        }
    }

    // Add this helper function inside the PreviewActivity class
    private fun Uri.getFileName(context: Context): String {
        var result: String? = null
        if (this.scheme == "content") {
            context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex("_display_name")
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex)
                    }
                }
            }
        }
        return result ?: this.lastPathSegment ?: "model.onnx"
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        val file = File(cacheDir, "annotated_image_${System.currentTimeMillis()}.png")
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.toUri()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getPreviewInputs(): PreviewInputs {
        val value = seekBarConfidence.progress / 100f
        return PreviewInputs(
            modelUri = modelUri,
            imageUri = imageUri,
            iou = value,
            labelType = spinner.selectedItem.toString(),
            labelScale = seekBarLabelScale.progress,
            imgWidth = widthInput.text.toString().toIntOrNull() ?: 640,
            imgHeight = heightInput.text.toString().toIntOrNull() ?: 640,
            customLabelUri = if (spinner.selectedItem.toString() == "Custom Labels") customLabelUri else null,
            confidenceThreshold = value
        )
    }

    private fun readLabelsFromUri(uri: Uri): List<String> = mutableListOf<String>().apply {
        contentResolver.openInputStream(uri)?.bufferedReader()?.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty()) add(trimmed)
        }
    }

    private fun defaultCocoLabels() = listOf(
        "person","bicycle","car","motorcycle","airplane","bus","train","truck","boat",
        "traffic light","fire hydrant","stop sign","parking meter","bench","bird","cat",
        "dog","horse","sheep","cow","elephant","bear","zebra","giraffe","backpack",
        "umbrella","handbag","tie","suitcase","frisbee","skis","snowboard","sports ball",
        "kite","baseball bat","baseball glove","skateboard","surfboard","tennis racket",
        "bottle","wine glass","cup","fork","knife","spoon","bowl","banana","apple",
        "sandwich","orange","broccoli","carrot","hot dog","pizza","donut","cake",
        "chair","couch","potted plant","bed","dining table","toilet","tv","laptop",
        "mouse","remote","keyboard","cell phone","microwave","oven","toaster","sink",
        "refrigerator","book","clock","vase","scissors","teddy bear","hair drier",
        "toothbrush"
    )

    private fun Uri.toBitmap(context: Context): Bitmap =
        context.contentResolver.openInputStream(this)?.use { android.graphics.BitmapFactory.decodeStream(it)!! }!!

    private fun Uri.toFilePath(context: Context): String? {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(this, "r") ?: return null
        val file = File(context.cacheDir, "temp_model.onnx")
        parcelFileDescriptor.use { pfd ->
            java.io.FileInputStream(pfd.fileDescriptor).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file.absolutePath
    }
}

@Parcelize
data class SimpleDetectionResult(val label: String, val confidence: Float) : Parcelable

data class PreviewInputs(
    val modelUri: Uri,
    val imageUri: Uri,
    val iou: Float,
    val labelType: String,
    val labelScale: Int,
    val imgWidth: Int,
    val imgHeight: Int,
    val customLabelUri: Uri? = null,
    val confidenceThreshold: Float = 0.20f
)