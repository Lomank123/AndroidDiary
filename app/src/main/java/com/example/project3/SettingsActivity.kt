package com.example.project3

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    // создаем объект класса настроек
    lateinit var settings : SharedPreferences
    private var apShowImg = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // инициализируем переменную (2 параметр - доступ только компонентам приложения)
        settings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)


        button2.setOnClickListener()
        {
            apShowImg = !apShowImg
        }

    }

    override fun onPause() {
        super.onPause()

        // Запоминаем данные
        val editor = settings.edit()
        editor.putBoolean(AP_SHOW_IMG, apShowImg)
        editor.apply()
    }

    override fun onResume() {
        super.onResume()

        if(settings.contains(AP_SHOW_IMG))
        {
            apShowImg = settings.getBoolean(AP_SHOW_IMG, true)
            if(apShowImg)
                textView5.text = "It's true now"
            else
                textView5.text = "It's false now"
        }
    }

    // ключи
    companion object
    {
        val APP_PREFERENCES = "settings_main"
        var AP_SHOW_IMG = "showImg"
    }
} // TODO: добавить комментариев для SettingsActivity
