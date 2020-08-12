package com.example.project3

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_new_note.*

class NewNoteActivity : AppCompatActivity() {

    private val choosePhotoRequestCode = 1
    private var isPhotoExist = false
    private val replyIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)

        imageView_note.setImageResource(R.drawable.blank_sheet)
        // обработчик нажатий на кнопку Save
        button_save_note.setOnClickListener {
            // если поле пустое устанавливаем отриц. результат
            if (TextUtils.isEmpty(edit_note.text)) {
                // устанавливаем результат как RESULT_CANCELED (отменен)
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                // создаем массив с названием и описанием дневника
                val noteContext = arrayListOf(edit_note.text.toString(),
                    edit_text_note.text.toString())
                replyIntent.putExtra(EXTRA_NEW_NOTE, noteContext)
                setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK
            }
            // завершаем работу с активити
            finish()
        }
        button_cancel_note.setOnClickListener {
            // Если изменения были, спрашиваем, хочет ли пользователь покинуть окно
            if (isPhotoExist || edit_note.text.toString().isNotEmpty() || edit_text_note.text.toString().isNotEmpty())
                makeDialog()
            else {
                // устанавливаем результат и завершаем работу с активити
                setResult(Activity.RESULT_CANCELED, replyIntent)
                finish()
            }
        }
        // Кнопка Delete
        delete_photo_button_note.setOnClickListener{
            if (isPhotoExist) {
                isPhotoExist = false
                imageView_note.setImageResource(R.drawable.blank_sheet)
                replyIntent.putExtra(EXTRA_NEW_NOTE_IMAGE, "")
            }
        }
        photo_button_note.setOnClickListener{
            // Выбираем фото из галереи
            val choosePhotoIntent = Intent(Intent.ACTION_PICK)
            choosePhotoIntent.type = "image/*"
            startActivityForResult(choosePhotoIntent, choosePhotoRequestCode)
        }
    }

    private fun makeDialog()
    {
        val userDialog = AlertDialog.Builder(this)
        userDialog.setTitle(this.resources.getString(R.string.dialog_leave_changes))
        userDialog.setMessage(this.resources.getString(R.string.dialog_check_leave))
        userDialog.setPositiveButton(this.resources.getString(R.string.dialog_yes))
        { _, _ ->
            setResult(Activity.RESULT_CANCELED, replyIntent)
            finish()
        }
        userDialog.setNegativeButton(this.resources.getString(R.string.dialog_no))
        { dialog, _ ->
            dialog.dismiss()
        }
        userDialog.show()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        // Если изменения были, спрашиваем, хочет ли пользователь покинуть окно
        if (isPhotoExist || edit_note.text.toString().isNotEmpty() || edit_text_note.text.toString().isNotEmpty())
            makeDialog()
        else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == choosePhotoRequestCode && resultCode == Activity.RESULT_OK)
        {
            isPhotoExist = true
            imageView_note.setImageURI(data?.data)
            replyIntent.putExtra(EXTRA_NEW_NOTE_IMAGE, data?.data.toString())
        }
    }


    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_NEW_NOTE = "EXTRA_NEW_NOTE"
        const val EXTRA_NEW_NOTE_IMAGE = "EXTRA_NEW_NOTE_IMAGE"
    }
}
