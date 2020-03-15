package com.example.project3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_clicked.*

class ClickedActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked)

        val noteId = intent.getLongExtra("note_idNote", -1)

        // получаем экстра данные из NoteActivity
        textView1.text = intent.getStringExtra("note_name")
        editText1.setText(intent.getStringExtra("note_text"))
        textView_date.text = intent.getStringExtra("note_date")

        if (intent.getStringExtra("note_img") != null &&
            intent.getStringExtra("note_img") != "")
        {
            val uriImage = Uri.parse(intent.getStringExtra("note_img"))
            imageView_clicked.setImageURI(uriImage)
        }

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

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // когда выбираешь элемент меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.save_btn_edit -> { // Кнопка Save
                val noteId = intent.getLongExtra("note_idNote", -1)
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
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            R.id.cancel_btn_edit -> { // Кнопка Cancel
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_REPLY_EDIT = "reply_edit"
    }
}
