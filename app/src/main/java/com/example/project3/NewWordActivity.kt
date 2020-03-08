package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_new_word.*

class NewWordActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_word)

        // обработчик нажатий на кнопку Save
        button_save.setOnClickListener {

            val replyIntent = Intent()

            // если поле пустое устанавливаем отриц. результат
            if (TextUtils.isEmpty(edit_word.text)) {
                // устанавливаем результат как RESULT_CANCELED (отменен)
                setResult(Activity.RESULT_CANCELED, replyIntent)
            }
            else
            {
                // создаем массив с названием и описанием дневника
                val word = arrayListOf(edit_word.text.toString(),
                    edit_descr.text.toString())

                // кладем то, что написано в editText в word и передаем по тегу EXTRA_REPLY (ниже)
                replyIntent.putExtra(EXTRA_REPLY, word)

                setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK
            }
            // завершаем работу с активити
            finish()
        }
    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_REPLY = "com.example.android.wordlistsql.REPLY"
    }
}