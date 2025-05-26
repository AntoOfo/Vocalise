package com.example.vocalise

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vocalise.tts.TTSManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import java.io.File

@AndroidEntryPoint
class TTSDisplayActivity : AppCompatActivity() {

    // dialog stuff
    private lateinit var loadingDialog: AlertDialog

    // camera stuff
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private val CAMERA_PERMISSION_CODE = 1002
    private var photoUri: Uri? = null

    private lateinit var recogText: TextView
    private lateinit var recogImage: ImageView

    // injecting instance of ttsmanager class
    @Inject lateinit var ttsManager: TTSManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ttsdisplay)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // force lightmode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // ui elements
        recogText = findViewById(R.id.recogText)
        recogImage = findViewById(R.id.recogImage)
        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val scanBtn = findViewById<Button>(R.id.scanBtn)

        // passed on data from mainactivity
        val foundText = intent.getStringExtra("recognised_text")
        val photoUriString = intent.getStringExtra("photo_uri")

        if (foundText != null && photoUriString != null) {
            recogText.text = foundText

            try {
                val photoUri = Uri.parse(photoUriString)
                val inputStream = contentResolver.openInputStream(photoUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                recogImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Missing image or text", Toast.LENGTH_SHORT).show()
        }

        backBtn.setOnClickListener {
            backBtn.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .alpha(0.5f)
                .setDuration(100)
                .withEndAction {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    backBtn.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                showLoadingDialog()
                val imageBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri!!))

                if (imageBitmap != null) {
                    runTextRecog(
                        bitmap = imageBitmap,
                        onResult = { recognisedText ->
                            hideLoadingDialog()
                            if (recognisedText.isNotBlank()) {
                                recogText.text = recognisedText
                                recogImage.setImageBitmap(imageBitmap)
                                ttsManager.speak("Hey! I found the following text: $recognisedText")
                            } else {
                                ttsManager.speak("Sorry, I can't find any text in this image.")
                            }
                        },
                        onError = { e ->
                            Toast.makeText(this, "Text recognition failed", Toast.LENGTH_SHORT).show()
                            ttsManager.speak("The text recognition failed.")
                        }
                    )
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        scanBtn.setOnClickListener {
            scanBtn.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .alpha(0.5f)
                .setDuration(100)
                .withEndAction {
                    checkCameraPermissionAndLaunch()

                    scanBtn.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }
    }

    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE)
        }
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile("IMG_", ".jpg", externalCacheDir)
        photoUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] ==
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(this,
                    "Camera permission is required!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.loading_dialog, null)

        builder.setView(dialogView)
        loadingDialog = builder.create()
        loadingDialog.show()

        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun hideLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    private fun runTextRecog(
        bitmap: Bitmap,
        onResult: (String) -> Unit,
        onError: (Exception) -> Unit
     ){
        val image = InputImage.fromBitmap(bitmap, 0)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image).addOnSuccessListener { visionText ->
            val structuredText = StringBuilder()
            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    for (element in line.elements) {
                        structuredText.append(element.text).append(" ")
                    }
                    structuredText.append("\n")
                }
            }
            onResult(structuredText.toString().trim())
        }
            .addOnFailureListener { e -> onError(e) }
    }
}