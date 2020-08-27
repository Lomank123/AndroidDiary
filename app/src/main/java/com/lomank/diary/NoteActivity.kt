package com.lomank.diary

import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import recyclerviewadapter.ViewPagerStatesAdapter

class NoteActivity : AppCompatActivity() {

    private val permissionRequestCode = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        // При первом запуске приложения на уст-ве добавляем усл-ие для сортировки
        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        if (!(prefs!!.contains("sorted_notes")))
            prefs.edit().putBoolean("sorted_notes", false).apply()

        viewPagerWithFragments()
    }

    private fun viewPagerWithFragments()
    {
        val viewPager : ViewPager2 = findViewById(R.id.viewpager1)
        val pagerAdapter = ViewPagerStatesAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = pagerAdapter

        val tabLayout : TabLayout = findViewById(R.id.tablayout1)
        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            //TODO: Change to string resources
            when (position){
                0 -> tab.text = "Notes"
                1 -> tab.text = "Daily list"
            }
        }.attach()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            permissionRequestCode -> {
                var allSuccess = true
                for(i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allSuccess = false
                        val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                        if(requestAgain)
                            Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(this, "go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                    }
                }
                if(allSuccess)
                    Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
