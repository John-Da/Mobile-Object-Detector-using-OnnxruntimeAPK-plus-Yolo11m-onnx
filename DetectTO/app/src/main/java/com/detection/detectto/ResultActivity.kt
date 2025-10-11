package com.detection.detectto

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ResultActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var modelText: TextView
    private lateinit var labelText: TextView
    private lateinit var imgSizeText: TextView
    private lateinit var iouText: TextView
    private lateinit var outputCountText: TextView
    private lateinit var backButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var resultIdText: TextView

    private var annotatedImageUri: Uri? = null
    private val STORAGE_PERMISSION_CODE = 100

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_page)

        // --- Handle system back press using Dispatcher ---
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMain()
            }
        })
        // -----------------------------------------------------

        // Bind views
        imageView = findViewById(R.id.imageViewResult)
        recyclerView = findViewById(R.id.recyclerViewResults)
        modelText = findViewById(R.id.textViewModel)
        labelText = findViewById(R.id.textViewLabel)
        imgSizeText = findViewById(R.id.textViewImgSz)
        outputCountText = findViewById(R.id.textView7)
        backButton = findViewById(R.id.button9)
        saveButton = findViewById(R.id.button10)
        resultIdText = findViewById(R.id.textViewResultID)
        iouText = findViewById(R.id.textViewIou)

        // Get intent data
        val resultId = intent.getStringExtra("RESULT_ID") ?: "N/A"
        annotatedImageUri = intent.getStringExtra("ANNOTATED_IMAGE_URI")?.let { Uri.parse(it) }
        val results = intent.getParcelableArrayListExtra<SimpleDetectionResult>("RESULTS") ?: arrayListOf()
        val modelType = intent.getStringExtra("MODEL_NAME") ?: "Unknown"
        val labelType = intent.getStringExtra("LABEL_TYPE") ?: "Unknown"
        val imgSize = intent.getStringExtra("IMG_SIZE") ?: "Unknown"
        val iouThreshold = intent.getStringExtra("IOU_SIZE") ?: "N/A"

        // --- Display Logic ---

        annotatedImageUri?.let { imageView.setImageURI(it) }

        // Display the Result ID
        resultIdText.text = "$resultId"

        modelText.text = "Model: $modelType"
        labelText.text = "Label: $labelType"
        imgSizeText.text = "Model Input Size: $imgSize"
        iouText.text = "iou Threshold: $iouThreshold"
        outputCountText.text = "${results.size} Objects"

        // Use results list (correctly numbered in PreviewActivity)
        val finalResults = results

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ResultAdapter(finalResults)

        // --- Custom Button Listener ---
        backButton.setOnClickListener {
            navigateToMain()
        }

        saveButton.setOnClickListener {
            checkStoragePermissionAndSave()
        }
    }

    // --- Permission Check and Save Image Logic (No Change) ---
    private fun checkStoragePermissionAndSave() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            saveImageToGallery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImageToGallery()
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            Toast.makeText(this, "Permission denied. Cannot save image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToGallery() {
        val uriToSave = annotatedImageUri ?: return

        lifecycleScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uriToSave)?.use {
                        android.graphics.BitmapFactory.decodeStream(it)
                    }
                } ?: throw IllegalStateException("Failed to load image from URI.")

                withContext(Dispatchers.IO) {
                    val filename = "detection_${System.currentTimeMillis()}.png"
                    val mimeType = "image/png"
                    val outputStream: OutputStream?

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "DetectTo")
                        }
                        val contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        val imageUri = contentResolver.insert(contentUri, contentValues) ?: throw IllegalStateException("Failed to create new MediaStore record.")
                        outputStream = contentResolver.openOutputStream(imageUri)
                    } else {
                        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + File.separator + "DetectTo")
                        if (!imagesDir.exists()) imagesDir.mkdirs()
                        val imageFile = File(imagesDir, filename)
                        outputStream = FileOutputStream(imageFile)
                    }

                    outputStream?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    } ?: throw IllegalStateException("Failed to open output stream.")
                }

                Toast.makeText(this@ResultActivity, "Image saved to Gallery/DetectTo!", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ResultActivity, "Error saving image: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // RecyclerView adapter (remains the same)
    class ResultAdapter(private val items: List<SimpleDetectionResult>) :
        RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

        inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
            val labelText: TextView = view.findViewById(R.id.labelText)
            val confText: TextView = view.findViewById(R.id.confText)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.result_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.labelText.text = item.label
            holder.confText.text = "${"%.2f".format(item.confidence)} conf"
        }

        override fun getItemCount(): Int = items.size
    }
}