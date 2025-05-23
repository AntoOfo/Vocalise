package com.example.vocalise

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
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
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

// enables hilt injection to this activity
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // dialog stuff
    private lateinit var loadingDialog: AlertDialog

    // camera stuff
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private val CAMERA_PERMISSION_CODE = 1001
    private var photoUri: Uri? = null

    // injecting an instance of ttsmanager class
    @Inject lateinit var ttsManager: TTSManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        val startBtn = findViewById<Button>(R.id.startedBtn)

        // initalising camera launcher
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                showLoadingDialog()
                val imageBitmap =  BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri!!))

                // conditionals for if text is recognised
                if (imageBitmap != null) {
                    runTextRecog(
                        bitmap = imageBitmap,
                        onResult = { recognisedText ->
                            hideLoadingDialog()
                            if (recognisedText.isNotBlank()) {
                                // open new activity
                                val intent = Intent(this, TTSDisplayActivity::class.java)
                                intent.putExtra("recognised_text", recognisedText)
                                intent.putExtra("photo_uri", photoUri.toString())  // passing filepath
                                startActivity(intent)

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


        startBtn.setOnClickListener {
            checkCameraPermissionAndLaunch()

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
        // makes temporary file in cache directory
        val photoFile = File.createTempFile("IMG_", ".jpg", externalCacheDir)
        photoUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)

        // camera intent with output uri
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

    // function for text recognition from ml kit
    private fun runTextRecog(
        bitmap: Bitmap,     // scans for text
        onResult: (String) -> Unit,    // callback w recognised text
        onError: (Exception) -> Unit       // error
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image).addOnSuccessListener { visionText ->
            onResult(visionText.text)
        }
            .addOnFailureListener { e -> onError(e)}
    }
}