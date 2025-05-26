package com.example.vocalise

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.widget.Button
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
        val backBtn = findViewById<Button>(R.id.backBtn)
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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}