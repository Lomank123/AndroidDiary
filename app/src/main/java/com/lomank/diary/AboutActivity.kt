package com.lomank.diary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(materialToolbar_about)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        materialToolbar_about.setNavigationIcon(R.drawable.ic_baseline_arrow_back_gray_32)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
