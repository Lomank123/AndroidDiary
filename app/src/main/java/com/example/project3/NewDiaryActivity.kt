package com.example.project3

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_new_diary.*

class NewDiaryActivity : AppCompatActivity() {

    // RequestCode для выбора фото
    private val choosePhotoRequestCode = 1
    private var isPhotoExist = false

    private val replyIntent = Intent()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_diary)

        imageView_new_diary.setImageResource(R.drawable.logo)
        // обработчик нажатий на кнопку Save
        button_save.setOnClickListener {
            // если поле пустое устанавливаем отриц. результат
            if (TextUtils.isEmpty(edit_diary.text)) {
                // устанавливаем результат как RESULT_CANCELED (отменен)
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                // создаем массив с названием и описанием дневника
                val diaryContext = arrayListOf(edit_diary.text.toString(),
                    edit_content.text.toString())
                // кладем то, что написано в editText в word и передаем по тегу EXTRA_REPLY (ниже)
                replyIntent.putExtra(EXTRA_NEW_DIARY, diaryContext)
                setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK
            }
            // завершаем работу с активити
            finish()
        }
        // Кнопка Cancel
        button_cancel_diary.setOnClickListener {
            // Если изменения были, спрашиваем, хочет ли пользователь покинуть окно
            if (isPhotoExist || edit_diary.text.toString().isNotEmpty() || edit_content.text.toString().isNotEmpty())
                makeDialog()
            else {
                // устанавливаем результат и завершаем работу с активити
                setResult(Activity.RESULT_CANCELED, replyIntent)
                finish()
            }
        }
        // Кнопка Delete
        delete_photo_button.setOnClickListener{
            if (isPhotoExist) {
                isPhotoExist = false
                imageView_new_diary.setImageResource(R.drawable.logo)
                replyIntent.putExtra(EXTRA_NEW_DIARY_IMAGE, "")
            }
        }
        // слушатель для кнопки Choose photo
        photo_button.setOnClickListener{
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
        if (isPhotoExist || edit_diary.text.toString().isNotEmpty() || edit_content.text.toString().isNotEmpty())
            makeDialog()
        else {
            super.onBackPressed()
        }
    }

    // обрабатываем выбор фото из галереи
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == choosePhotoRequestCode && resultCode == Activity.RESULT_OK)
        {
            isPhotoExist = true
            imageView_new_diary.setImageURI(data?.data)
            replyIntent.putExtra(EXTRA_NEW_DIARY_IMAGE, data?.data.toString())
        }
    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_NEW_DIARY = "EXTRA_NEW_DIARY"
        const val EXTRA_NEW_DIARY_IMAGE = "EXTRA_NEW_DIARY_IMAGE"
    }
}