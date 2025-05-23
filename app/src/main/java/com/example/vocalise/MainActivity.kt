package com.example.vocalise

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vocalise.tts.TTSManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// enables hilt injection to this activity
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // camera stuff
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private val CAMERA_PERMISSION_CODE = 1001

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

        val startBtn = findViewById<Button>(R.id.startedBtn)

        // initalising camera launcher
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                ttsManager.speak("Picture taken successfully")
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
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
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
}