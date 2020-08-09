package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_new_word.*

class NewDiaryActivity : AppCompatActivity() {

    // RequestCode для выбора фото
    private val choosePhotoRequestCode = 1

    private val replyIntent = Intent()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_word)

        // обработчик нажатий на кнопку Save
        button_save.setOnClickListener {

            // если поле пустое устанавливаем отриц. результат
            if (TextUtils.isEmpty(edit_word.text)) {
                // устанавливаем результат как RESULT_CANCELED (отменен)
                setResult(Activity.RESULT_CANCELED, replyIntent)
            }
            else
            {
                // создаем массив с названием и описанием дневника
                val diaryContext = arrayListOf(edit_word.text.toString(),
                    edit_descr.text.toString())
                // кладем то, что написано в editText в word и передаем по тегу EXTRA_REPLY (ниже)
                replyIntent.putExtra(EXTRA_REPLY, diaryContext)
                setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK
            }
            // завершаем работу с активити
            finish()
        }

        // слушатель для кнопки Cancel
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

    // обрабатываем выбор фото из галереи
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == choosePhotoRequestCode && resultCode == Activity.RESULT_OK)
        {
            imageView5.setImageURI(data?.data)
            replyIntent.putExtra(EXTRA_IMAGE, data?.data.toString())
        }

    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_REPLY = "extra_reply_diary"
        const val EXTRA_IMAGE = "imgDiary"
    }
}