package com.lomank.diary

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.timePicker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class SettingsHolderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(materialToolbar_settings)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        materialToolbar_settings.setNavigationIcon(R.drawable.ic_baseline_arrow_back_gray_32)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.title_activity_settings, SettingsFragment())
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val rateButton = findPreference<Preference>("rate_button")
            val scheduleButton = findPreference<Preference>("notify_time")
            val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(requireContext())

            val calendar = Calendar.getInstance()
            val scheduleTime = prefs!!.getString("notify_time", null)
            if(scheduleTime != null) {
                val gson = Gson()
                val type = object : TypeToken<List<Int>>() {}.type
                val dateList: List<Int> = gson.fromJson(scheduleTime, type)

                calendar.set(Calendar.HOUR_OF_DAY, dateList[0])
                calendar.set(Calendar.MINUTE, dateList[1])
                calendar.set(Calendar.SECOND, dateList[2])
                calendar.set(Calendar.MILLISECOND, dateList[3])
            }

            scheduleButton!!.setOnPreferenceClickListener {
                val dialog = MaterialDialog(requireContext())
                dialog.show{
                    timePicker(calendar) { _, datetime ->
                        Log.e("time", datetime.time.toString())

                        val listOfDate = listOf(datetime.get(Calendar.HOUR_OF_DAY),
                            datetime.get(Calendar.MINUTE),
                            datetime.get(Calendar.SECOND),
                            datetime.get(Calendar.MILLISECOND))

                        val gson = Gson()
                        val json = gson.toJson(listOfDate)

                        prefs.edit().putString("notify_time", json).apply()
                    }
                    positiveButton {
                        dialog.dismiss()
                    }
                    negativeButton {
                        dialog.dismiss()
                    }
                }
                true
            }
            rateButton!!.setOnPreferenceClickListener {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + requireActivity().packageName)
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + requireActivity().packageName)
                        )
                    )
                }
                true
            }
        }
    }
}