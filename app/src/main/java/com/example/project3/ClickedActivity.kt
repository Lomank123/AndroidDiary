package com.example.project3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_clicked.*

class ClickedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked)
        textView1.text = "You have selected " + intent.getStringExtra("tag")


    }

    fun clicked(){



    }

}
