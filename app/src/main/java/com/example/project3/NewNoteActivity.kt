package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_new_note.*

class NewNoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)

        // обработчик нажатий на кнопку Save
        button_save_note.setOnClickListener {

            val replyIntent = Intent()

            // если поле пустое устанавливаем отриц. результат
            if (TextUtils.isEmpty(edit_note.text)) {
                // устанавливаем результат как RESULT_CANCELED (отменен)
                setResult(Activity.RESULT_CANCELED, replyIntent)
            }
            else
            {
                // создаем массив с названием и описанием дневника
                val note = arrayListOf(edit_note.text.toString(),
                    edit_text_note.text.toString())

                replyIntent.putExtra(EXTRA_REPLY_NOTE, note)

                setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK
            }
            // завершаем работу с активити
            finish()
        }
        button_cancel_note1.setOnClickListener {
            // устанавливаем результат и завершаем работу с активити
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_REPLY_NOTE = "NewNoteReply"
    }
} // TODO: Добавить кнопку Cancel
