package com.example.project3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

// Экран загрузки приложения
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
