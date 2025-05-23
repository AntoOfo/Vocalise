package com.example.vocalise

import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TTSDisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ttsdisplay)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // ui elements
        val recogText = findViewById<TextView>(R.id.recogText)
        val recogImage = findViewById<ImageView>(R.id.recogImage)

        // passed on data from mainactivity
        val foundText = intent.getStringExtra("recognised_text")
        val foundImage = intent.getByteArrayExtra("recognised_image")

        if (foundText != null) {
            val bitmap = BitmapFactory.decodeByteArray(foundImage, 0, foundImage!!.size)
            recogImage.setImageBitmap(bitmap)
            recogText.text = foundText
        } else {
            Toast.makeText(this, "No image data passed", Toast.LENGTH_SHORT).show()
        }

    }
}