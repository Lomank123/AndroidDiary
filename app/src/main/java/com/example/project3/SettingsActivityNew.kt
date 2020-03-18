package com.example.project3

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

// preference.xml - файл с отображением всех пунктов настроек (находится в папке xml)

class SettingsActivityNew : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.preference, rootKey)

    }

}// TODO: Добавить больше настроек + комментариев к ним