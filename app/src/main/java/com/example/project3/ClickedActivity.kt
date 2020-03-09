package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_clicked.*

class ClickedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked)

        // получаем экстра данные из NoteActivity
        textView1.text = intent.getStringExtra("note_name")
        editText1.setText(intent.getStringExtra("note_text"))

        val noteId = intent.getLongExtra("note_idNote", -1)

        // Обработчик нажатий для кнопки Save
        button_save1.setOnClickListener {

            val replyIntent = Intent()

            // создаем массив с названием и текстом заметки
            val note = arrayListOf(textView1.text.toString(),
                editText1.text.toString())

            // кладем то, что записано в массив и передаем по тегу EXTRA_REPLY_EDIT
            replyIntent.putExtra(EXTRA_REPLY_EDIT, note)
            // также передаем idNote заметки чтобы обновить ее в NoteActivity
            replyIntent.putExtra("noteId", noteId)

            setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK
            // Завершаем работу с активити
            finish()
        }

        // обработчик нажатий на кнопку Cancel
        button_cancel1.setOnClickListener {
            // устанавливаем результат и завершаем работу с активити
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_REPLY_EDIT = "reply_edit"
    }
}
