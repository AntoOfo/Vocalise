package com.example.vocalise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
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

        startBtn.setOnClickListener {
            ttsManager.speak("Get started button clicked")

        }
    }

    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }
}