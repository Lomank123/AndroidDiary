package com.example.project3

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_new_note.*
import roomdatabase.Note

class EditNoteActivity : AppCompatActivity() {

    private val choosePhotoRequestCode = 1
    private var isPhotoChanged = false
    private var isPhotoExist = false
    private val replyIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)

        val note = intent.getSerializableExtra("noteSerializableEdit") as? Note

        edit_note.setText(note!!.name)
        edit_text_note.setText(note.content)

        if (note.img != null && note.img != "") {
            isPhotoExist = true
            val uriImage = Uri.parse(note.img)
            imageView_note.setImageURI(uriImage)
        }
        button_save_note.setOnClickListener {
            if (TextUtils.isEmpty(edit_note.text)) {
                // устанавливаем результат как RESULT_CANCELED (отменен)
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                note.name = edit_note.text.toString()
                note.content = edit_text_note.text.toString()
                replyIntent.putExtra(EXTRA_EDIT_NOTE, note)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
        button_cancel_note1.setOnClickListener {
            // Если изменения были, спрашиваем, хочет ли пользователь покинуть окно
            if (isPhotoChanged || edit_note.text.toString() != note.name || edit_text_note.text.toString() != note.content)
                makeDialog()
            else {
                setResult(Activity.RESULT_CANCELED, replyIntent)
                finish()
            }
        }
        // Кнопка Delete
        delete_photo_button_note.setOnClickListener{
            if (isPhotoExist) {
                isPhotoChanged = note.img != null
                isPhotoExist = false
                imageView_note.setImageResource(R.mipmap.ic_launcher_round)
                // Кладем в экстра данные картинки пустую строку
                replyIntent.putExtra(EXTRA_IMAGE_EDIT_NOTE,"")
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
        val note = intent.getSerializableExtra("noteSerializableEdit") as? Note
        // Если изменения были, спрашиваем, хочет ли пользователь покинуть окно
        if (isPhotoChanged || edit_note.text.toString() != note!!.name || edit_text_note.text.toString() != note.content)
            makeDialog()
        else
            super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == choosePhotoRequestCode && resultCode == Activity.RESULT_OK)
        {
            val note = intent.getSerializableExtra("noteSerializableEdit") as? Note
            imageView_note.setImageURI(data?.data)
            replyIntent.putExtra(EXTRA_IMAGE_EDIT_NOTE, data?.data.toString())

            isPhotoChanged = note!!.img != data?.data.toString()
            isPhotoExist = true
        }
    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_EDIT_NOTE = "EXTRA_EDIT_NOTE"
        const val EXTRA_IMAGE_EDIT_NOTE = "EXTRA_IMAGE_EDIT_NOTE"
    }
}