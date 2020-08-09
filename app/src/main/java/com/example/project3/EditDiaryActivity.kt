package com.example.project3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_new_word.*
import roomdatabase.Diary

class EditDiaryActivity : AppCompatActivity() {

    private val choosePhotoRequestCode = 1

    private val replyIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_word)

        val diary = intent.getSerializableExtra("diarySerializableEdit") as? Diary

        edit_word.setText(diary!!.diary_name)
        edit_descr.setText(diary.diary_content)

        if (diary.diaryImg != null && diary.diaryImg != "") {
            val uriImage = Uri.parse(diary.diaryImg)
            imageView5.setImageURI(uriImage)
        }
        button_save.setOnClickListener {

            if (TextUtils.isEmpty(edit_word.text)) {
                // устанавливаем результат как RESULT_CANCELED (отменен)
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                diary.diary_name = edit_word.text.toString()
                diary.diary_content = edit_descr.text.toString()
                replyIntent.putExtra(EXTRA_EDIT_WORD, diary)
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
        const val EXTRA_EDIT_WORD = "DiaryReplyEdit"
        const val EXTRA_IMAGE_EDIT_WORD = "ImgDiaryReplyEdit"
    }
}