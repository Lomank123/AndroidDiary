package com.lomank.diary

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_note.*
import other.ViewPagerStatesAdapter

class NoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        setSupportActionBar(materialToolbar_note)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        materialToolbar_note.overflowIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_more_vert_32, null)
        materialToolbar_note.setNavigationIcon(R.drawable.ic_baseline_arrow_back_gray_32)

        MobileAds.initialize(this)
        adView2.loadAd(AdRequest.Builder().build())

        // При первом запуске приложения на уст-ве добавляем усл-ие для сортировки
        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        if (!(prefs!!.contains("sorted_notes")))
            prefs.edit().putBoolean("sorted_notes", false).apply()

        viewPagerWithFragments()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun viewPagerWithFragments()
    {
        val viewPager : ViewPager2 = findViewById(R.id.viewpager1)
        val pagerAdapter = ViewPagerStatesAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = pagerAdapter

        val tabLayout : TabLayout = findViewById(R.id.tablayout1)
        TabLayoutMediator(tabLayout, viewPager){ tab, position ->
            when (position){
                0 -> tab.text = resources.getString(R.string.fragment_notes)
                1 -> tab.text = resources.getString(R.string.fragment_daily_list)
            }
        }.attach()
    }
}
