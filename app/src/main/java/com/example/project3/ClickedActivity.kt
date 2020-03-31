package com.example.project3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_clicked.*
import roomdatabase.Note
import java.text.SimpleDateFormat
import java.util.*

class ClickedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked)



        val note = intent.getSerializableExtra("noteSerializable") as? Note

        // получаем экстра данные из NoteActivity
        textView1.text = note!!.note
        editText1.setText(note.text)

    }

    override fun onResume()
    {
        super.onResume()
        val note = intent.getSerializableExtra("noteSerializable") as? Note

        // получаем наши настройки
        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)

        if (prefs!!.getBoolean("img_check", false))
        {
            if (note!!.imgNote != null)
            {
                val uriImage = Uri.parse(note.imgNote)
                imageView_clicked.setImageURI(uriImage)
            }
        }

        // стандартный шрифт - roboto_regular.ttf
        when(prefs.getString("list_preference_1", "0"))
        {
            "Default" ->
            {
                textView1.typeface = Typeface.DEFAULT
                editText1.typeface = Typeface.DEFAULT
            }
            "Serif" ->
            {
                textView1.typeface = Typeface.SERIF
                editText1.typeface = Typeface.SERIF
            }
            "Sans Serif" ->
            {
                textView1.typeface = Typeface.SANS_SERIF
                editText1.typeface = Typeface.SANS_SERIF
            }
            "Default Bald" ->
            {
                textView1.typeface = Typeface.DEFAULT_BOLD
                editText1.typeface = Typeface.DEFAULT_BOLD
            }
            "Monospace" ->
            {
                textView1.typeface = Typeface.MONOSPACE
                editText1.typeface = Typeface.MONOSPACE
            }
        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // когда выбираешь элемент меню
    @SuppressLint("SimpleDateFormat")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val note = intent.getSerializableExtra("noteSerializable") as? Note

        when(item.itemId){

            R.id.save_btn_edit -> { // Кнопка Save
                val replyIntent = Intent()

                // обновляем введенный текст
                note!!.text = editText1.text.toString()

                // обновляем дату изменения заметки
                val pattern = "\t\t\tHH:mm\n\ndd.MM.yyyy"
                val simpleDateFormat =
                    SimpleDateFormat(pattern)
                val currentDate = simpleDateFormat.format(Date())

                note.dateNote = currentDate

                replyIntent.putExtra(EXTRA_REPLY_EDIT, note)

                setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK
                // Завершаем работу с активити
                Toast.makeText(this, resources.getString(R.string.saved), Toast.LENGTH_SHORT).show()
                finish()
            }
            R.id.cancel_btn_edit -> { // Кнопка Cancel
                setResult(Activity.RESULT_CANCELED)
                Toast.makeText(this, resources.getString(R.string.canceled), Toast.LENGTH_SHORT).show()
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