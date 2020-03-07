package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_clicked.*
import roomdatabase.Note
import viewmodel.NoteViewModel

class ClickedActivity : AppCompatActivity() {

    private lateinit var noteViewModel: NoteViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked)

        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        textView1.text = intent.getStringExtra("tag1")
        editText1.setText(intent.getStringExtra("tag2"))

        val noteId = intent.getLongExtra("idTag", -1)

        button_save1.setOnClickListener {

            val replyIntent = Intent()

            if (TextUtils.isEmpty(editText1.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent) // resultCode будет негативным
            }
            else {
                val note = arrayListOf(textView1.text.toString(),
                    editText1.text.toString())

                // кладем то, что написано в editText в word и передаем по тегу EXTRA_REPLY (ниже)
                replyIntent.putExtra(EXTRA_REPLY_EDIT, note)
                replyIntent.putExtra("noteId", noteId)

                setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK

            }

            finish()

        }

    }

    companion object {
        const val EXTRA_REPLY_EDIT = "reply_edit"
    }
}
