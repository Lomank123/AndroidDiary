package com.example.project3

import android.os.Bundle
import android.widget.Toast
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceFragmentCompat

// preference.xml - файл с отображением всех пунктов настроек (находится в папке xml)

class SettingsActivityNew : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.preference, rootKey)

        val i = findPreference<CheckBoxPreference>("pref_sync")
        i?.setOnPreferenceChangeListener { preference, newValue ->
            if(newValue == true)
            {
                Toast.makeText(activity,"enabled",Toast.LENGTH_LONG).show()
            }
            else
            {
                Toast.makeText(activity,"disabled",Toast.LENGTH_LONG).show()
            }
            true
        }

    }

}// TODO: Добавить больше настроек + комментариев к ним