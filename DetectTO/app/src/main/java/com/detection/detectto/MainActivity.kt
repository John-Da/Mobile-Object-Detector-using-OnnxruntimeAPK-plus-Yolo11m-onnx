package com.detection.detectto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.io.File
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ModelAdapter
    private lateinit var statusText: TextView
    private val models = mutableListOf<ModelFile>()
    private var isGridView = false
    private lateinit var emptyMessage: TextView
    private var modelClicked: ModelFile? = null

    // Properties for image and folder persistence
    private var currentPhotoUri: Uri? = null
    private val PREFS_NAME = "AppPrefs"
    private val FOLDER_URI_KEY = "folder_uri"


    private val folderPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (result.resultCode == Activity.RESULT_OK && uri != null) {
                // Persist access permission
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                // SAVE the new URI
                saveFolderUri(uri)
            }

            // Load ONNX files using DocumentFile (handles empty or null URI)
            loadOnnxFiles(uri)
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        statusText = findViewById(R.id.textView2)
        statusText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        emptyMessage = findViewById(R.id.emptyMessage)


        val gridCard: MaterialCardView = findViewById(R.id.gridCard)
        val listCard: MaterialCardView = findViewById(R.id.listCard)
        val fab: ExtendedFloatingActionButton = findViewById(R.id.button3)

        // Default layout: list
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ModelAdapter(models) { model ->
            showModelBottomSheet(model)
        }
        recyclerView.adapter = adapter

        // **PERSISTENCE CHANGE: Load saved folder URI automatically**
        loadSavedFolder()

        fab.setOnClickListener {
            openFolderPicker()
        }

        // Default layout: list
        isGridView = false
        recyclerView.layoutManager = LinearLayoutManager(this)
        updateViewIcons(gridCard, listCard)

// Grid card click
        gridCard.setOnClickListener {
            if (!isGridView) {
                isGridView = true
                recyclerView.layoutManager = GridLayoutManager(this, 2)
                updateViewIcons(gridCard, listCard)
            }
        }

// List card click
        listCard.setOnClickListener {
            if (isGridView) {
                isGridView = false
                recyclerView.layoutManager = LinearLayoutManager(this)
                updateViewIcons(gridCard, listCard)
            }
        }

    }

    private fun updateViewIcons(
        gridCard: MaterialCardView,
        listCard: MaterialCardView
    ) {
        val activeBg = ContextCompat.getColor(this, R.color.accent)
        val inactiveBg = ContextCompat.getColor(this, R.color.dark_100)

        if (isGridView) {
            gridCard.setCardBackgroundColor(activeBg)
            listCard.setCardBackgroundColor(inactiveBg)
        } else {
            listCard.setCardBackgroundColor(activeBg)
            gridCard.setCardBackgroundColor(inactiveBg)
        }
    }


    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        folderPicker.launch(intent)
    }

    // **PERSISTENCE HELPER: Tries to load the saved folder URI**
    private fun loadSavedFolder() {
        val savedUri = getSavedFolderUri()
        if (savedUri != null) {
            // Automatically load files from the saved URI
            loadOnnxFiles(savedUri)
        } else {
            // Show initial message if no folder is saved
            emptyMessage.visibility = View.VISIBLE
            emptyMessage.text = "Please select a folder containing ONNX files using the '+' button."
            statusText.text = "0 Model(s) Loaded"
            adapter.updateData(emptyList())
        }
    }


    private fun loadOnnxFiles(uri: Uri?) {
        if (uri == null) {
            // If URI is null (either initial unselected state or picker cancelled)
            emptyMessage.visibility = View.VISIBLE
            emptyMessage.text = "No folder selected. Please use the '+' button."
            statusText.text = "0 Model(s) Loaded"
            adapter.updateData(emptyList())
            return
        }

        val folder = DocumentFile.fromTreeUri(this, uri)
        val onnxFiles = folder?.listFiles()
            ?.filter { it.isFile && it.name?.endsWith(".onnx", true) == true }
            ?.map { ModelFile(it.name ?: "Unknown", it.uri.toString()) } ?: emptyList()

        if (onnxFiles.isEmpty()) {
            emptyMessage.visibility = View.VISIBLE
            emptyMessage.text = "No ONNX file found. Please select again."
            statusText.text = "0 Model(s) Loaded"
            adapter.updateData(emptyList())
        } else {
            emptyMessage.visibility = View.GONE
            adapter.updateData(onnxFiles)
            statusText.text = "${onnxFiles.size} Model(s) Loaded"
        }
    }

    // **PERSISTENCE HELPER: Saves the URI string to SharedPreferences**
    private fun saveFolderUri(uri: Uri?) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(FOLDER_URI_KEY, uri?.toString()).apply()
    }

    // **PERSISTENCE HELPER: Retrieves the URI string from SharedPreferences**
    private fun getSavedFolderUri(): Uri? {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val uriString = prefs.getString(FOLDER_URI_KEY, null)
        return uriString?.toUri()
    }


    // -----------------------------------------------------------------------
    // CAMERA & IMAGE HANDLING (UPDATED FOR 2K RESOLUTION)
    // -----------------------------------------------------------------------

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                modelClicked?.let { model ->
                    openPreviewActivity(model, it)
                } ?: run {
                    Toast.makeText(this, "No model selected!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // Use TakePicture to get the full-resolution image saved to our provided URI.
    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            currentPhotoUri?.let { originalUri ->
                if (success) {
                    // 1. Downscale the high-res image to roughly 2K (max 2048px on longest side)
                    val downscaledUri = downscaleImage(originalUri)

                    // 2. Open the preview with the resized image
                    modelClicked?.let { model ->
                        openPreviewActivity(model, downscaledUri)
                    } ?: run {
                        Toast.makeText(this, "No model selected!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Photo capture failed or cancelled.", Toast.LENGTH_SHORT).show()
                }
                // Clean up the original full-res photo file
                deleteTempFile(originalUri)
                currentPhotoUri = null
            }
        }

    /**
     * Creates a content URI for the camera to save the full-resolution photo.
     */
    private fun createImageUri(): Uri {
        val photoFile = File(getExternalFilesDir(null), "temp_camera_photo.jpg")
        return FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider", // IMPORTANT: Must match the authority in your manifest
            photoFile
        )
    }

    /**
     * Downscales the high-resolution image at the sourceUri to a target resolution (max 2048px).
     */
    private fun downscaleImage(sourceUri: Uri): Uri {
        // Target max size for 2K-like resolution (e.g., 2048 pixels on the longest side)
        val MAX_IMAGE_SIZE = 2048

        // 1. Decode bounds to calculate inSampleSize
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        var width = options.outWidth
        var height = options.outHeight
        var inSampleSize = 1

        // Calculate inSampleSize (power of 2) to roughly match the target size
        if (height > MAX_IMAGE_SIZE || width > MAX_IMAGE_SIZE) {
            var halfHeight = height / 2
            var halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= MAX_IMAGE_SIZE && (halfWidth / inSampleSize) >= MAX_IMAGE_SIZE) {
                inSampleSize *= 2
            }
        }

        // 2. Decode the downsampled Bitmap
        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        val originalBitmap: Bitmap? = contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        // 3. Final scaling for precise 2K target (maintaining aspect ratio)
        val finalBitmap = originalBitmap?.let { bmp ->
            var newWidth = bmp.width
            var newHeight = bmp.height

            if (newWidth > MAX_IMAGE_SIZE || newHeight > MAX_IMAGE_SIZE) {
                val scaleFactor = MAX_IMAGE_SIZE.toFloat() / Math.max(newWidth, newHeight)
                newWidth = (newWidth * scaleFactor).toInt()
                newHeight = (newHeight * scaleFactor).toInt()
            }

            Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true)
        }

        // 4. Save the new, downscaled bitmap to a temporary file
        val downscaledFile = File(cacheDir, "downscaled_image.jpg")
        downscaledFile.outputStream().use { out ->
            // Save as JPEG with a reasonable quality
            finalBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        originalBitmap?.recycle()
        finalBitmap?.recycle()

        return downscaledFile.toUri()
    }

    /**
     * Deletes the temporary file created for the original high-resolution camera output.
     */
    private fun deleteTempFile(uri: Uri) {
        try {
            val file = File(getExternalFilesDir(null), uri.lastPathSegment ?: return)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Log or handle cleanup failure if necessary
        }
    }

    // Helper to open PreviewActivity
    private fun openPreviewActivity(model: ModelFile, imageUri: Uri) {
        val intent = Intent(this, PreviewActivity::class.java).apply {
            putExtra("MODEL_NAME", model.name)
            putExtra("MODEL_URI", model.path)
            putExtra("IMAGE_URI", imageUri.toString())
        }
        startActivity(intent)
    }


    private fun showModelBottomSheet(model: ModelFile) {
        modelClicked = model  // store clicked model

        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        // Set model title
        val title = view.findViewById<TextView>(R.id.textView3)
        title.text = "Model: ${model.name}"

        // Buttons
        val selectBtn = view.findViewById<Button>(R.id.button2)
        val takePhotoBtn = view.findViewById<Button>(R.id.button4)
        val cancelBtn = view.findViewById<Button>(R.id.button5)

        selectBtn.setOnClickListener {
            // Pick an image from gallery
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            bottomSheetDialog.dismiss()
        }

        takePhotoBtn.setOnClickListener {
            // 1. Create the high-res URI before launching the camera
            currentPhotoUri = createImageUri()
            currentPhotoUri?.let { uri ->
                // 2. Take a photo (pass the URI to save the high-res image)
                takePhotoLauncher.launch(uri)
                bottomSheetDialog.dismiss()
            } ?: run {
                Toast.makeText(this, "Error creating photo file.", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }
        }

        cancelBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // Set bottom sheet height and behavior
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
            behavior.peekHeight = resources.displayMetrics.heightPixels / 2
            behavior.isDraggable = true
        }

        bottomSheetDialog.show()
    }
}