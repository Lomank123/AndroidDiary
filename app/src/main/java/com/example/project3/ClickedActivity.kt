package com.example.project3

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.project3.SettingsActivity.Companion.APP_PREFERENCES
import com.example.project3.SettingsActivity.Companion.AP_SHOW_IMG
import kotlinx.android.synthetic.main.activity_clicked.*
import roomdatabase.Note

class ClickedActivity : AppCompatActivity() {

    lateinit var settings1 : SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked)

        settings1 = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)

        val note = intent.getSerializableExtra("noteSerializable") as? Note

        // получаем экстра данные из NoteActivity
        textView1.text = note!!.note
        editText1.setText(note.text)
        textView_date.text = note.dateNote


        // Обработчик нажатий для кнопки Save
        button_save1.setOnClickListener {

            val replyIntent = Intent()

            note.text = editText1.text.toString()

            replyIntent.putExtra(EXTRA_REPLY_EDIT, note)

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

    override fun onResume()
    {
        super.onResume()
        val note = intent.getSerializableExtra("noteSerializable") as? Note

        if (settings1.getBoolean(AP_SHOW_IMG, true))
        {
            if (note!!.imgNote != null)
            {
                val uriImage = Uri.parse(note.imgNote)
                imageView_clicked.setImageURI(uriImage)
            }
        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // когда выбираешь элемент меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val note = intent.getSerializableExtra("noteSerializable") as? Note

        when(item.itemId){

            R.id.save_btn_edit -> { // Кнопка Save
                val replyIntent = Intent()

                note!!.text = editText1.text.toString()

                replyIntent.putExtra(EXTRA_REPLY_EDIT, note)

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
} // TODO: добавить комментариев для раздела настроек
