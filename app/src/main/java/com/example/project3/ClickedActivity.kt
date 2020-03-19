package com.example.project3

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

class ClickedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked)



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
}
