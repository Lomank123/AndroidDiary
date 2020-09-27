package com.lomank.diary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import java.io.File

// Экран загрузки приложения
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val handler = Handler()
        handler.postDelayed({

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }, 300)
    }
    // this helps to delete empty voice note in new note if app was closed unexpectedly
    override fun onDestroy() {
        super.onDestroy()
        val name = this.getExternalFilesDir(null)!!.absolutePath + "/voice_note_empty.3gpp"
        if(File(name).exists())
            File(name).delete()
    }
}