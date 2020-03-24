package com.example.project3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_new_note.*
import roomdatabase.Note
import java.text.SimpleDateFormat
import java.util.*

class EditActivityNote : AppCompatActivity() {

    private val choosePhotoRequestCode = 1

    private val replyIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)


        val note = intent.getSerializableExtra("noteSerializableEdit") as? Note

        edit_note.setText(note!!.note)
        edit_text_note.setText(note.text)

        if (note.imgNote != null && note.imgNote != "") {
            val uriImage = Uri.parse(note.imgNote)
            imageView_note.setImageURI(uriImage)
        }

        button_save_note.setOnClickListener {
            if (TextUtils.isEmpty(edit_note.text)) {
                // устанавливаем результат как RESULT_CANCELED (отменен)
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val pattern = "\t\t\tHH:mm\n\ndd.MM.yyyy"
                val simpleDateFormat =
                    SimpleDateFormat(pattern)
                val currentDate = simpleDateFormat.format(Date())


                note.note = edit_note.text.toString()
                note.text = edit_text_note.text.toString()
                note.dateNote = currentDate

                replyIntent.putExtra(EXTRA_EDIT_NOTE, note)

                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }

        button_cancel_note1.setOnClickListener {
            // устанавливаем результат и завершаем работу с активити
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        photo_button_note.setOnClickListener{
            // Выбираем фото из галереи
            val choosePhotoIntent = Intent(Intent.ACTION_PICK)
            choosePhotoIntent.type = "image/*"
            startActivityForResult(choosePhotoIntent, choosePhotoRequestCode)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == choosePhotoRequestCode && resultCode == Activity.RESULT_OK)
        {
            imageView_note.setImageURI(data?.data)
            replyIntent.putExtra(EXTRA_IMAGE_EDIT_NOTE, data?.data.toString())
        }

    }


    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_EDIT_NOTE = "EditNoteReply"
        const val EXTRA_IMAGE_EDIT_NOTE = "ImgNoteReply"
    }


}