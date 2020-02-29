package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_new_word.*

class NewWordActivity : AppCompatActivity() {


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_word)


        button_save.setOnClickListener {

            val replyIntent = Intent()

            if (TextUtils.isEmpty(edit_word.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent) // resultCode будет негативным
            }
            else {
                val word = arrayListOf(edit_word.text.toString(), edit_descr.text.toString())

                replyIntent.putExtra(EXTRA_REPLY, word)     // кладем то, что написано в editText в word и передаем по тегу EXTRA_REPLY (ниже)

                setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK
            }

            finish()
        }
    }

    companion object {
        const val EXTRA_REPLY = "com.example.android.wordlistsql.REPLY"
    }
}    // TODO: comment NewWordActivity class code