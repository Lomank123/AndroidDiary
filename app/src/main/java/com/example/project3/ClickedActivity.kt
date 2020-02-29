package com.example.project3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_clicked.*

class ClickedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked)
        val st = intent.getStringExtra("tag")
        textView1.text = "You have selected " + st


    }

    fun clicked(){



    }

}
