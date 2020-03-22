package com.example.project3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_new_word.*
import roomdatabase.Word
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    private val choosePhotoRequestCode = 1

    private val replyIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_word)

        val word = intent.getSerializableExtra("wordSerializableEdit") as? Word

        edit_word.setText(word!!.word)
        edit_descr.setText(word.description)

        if (word.img != null && word.img != "") {
            val uriImage = Uri.parse(word.img)
            imageView5.setImageURI(uriImage)
        }

        button_save.setOnClickListener {

            if (TextUtils.isEmpty(edit_word.text)) {
                // устанавливаем результат как RESULT_CANCELED (отменен)
                setResult(Activity.RESULT_CANCELED, replyIntent)
            }
            else
            {
                val pattern = "\t\t\tHH:mm\n\ndd.MM.yyyy"
                val simpleDateFormat =
                    SimpleDateFormat(pattern)
                val currentDate = simpleDateFormat.format(Date())

                word.word = edit_word.text.toString()
                word.description = edit_descr.text.toString()
                word.date = currentDate

                replyIntent.putExtra(EXTRA_EDIT_WORD, word)

                setResult(Activity.RESULT_OK, replyIntent)
            }
            // завершаем работу с активити
            finish()
        }


        button_cancel_word1.setOnClickListener {
            // устанавливаем результат и завершаем работу с активити
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        // слушатель для кнопки Choose photo
        photo_button.setOnClickListener{
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
            imageView5.setImageURI(data?.data)
            replyIntent.putExtra(EXTRA_IMAGE_EDIT_WORD, data?.data.toString())
        }

    }


    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_EDIT_WORD = "WordReplyEdit"
        const val EXTRA_IMAGE_EDIT_WORD = "ImgWordReplyEdit"
    }

}
