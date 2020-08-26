package com.lomank.diary

import android.app.Activity
import android.content.Intent
import android.graphics.Color.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import kotlinx.android.synthetic.main.activity_new_diary.*

class NewDiaryActivity : AppCompatActivity() {

    // RequestCode для выбора фото
    private val choosePhotoRequestCode = 1
    private var isPhotoExist = false
    private val colorArray = intArrayOf(RED, BLUE, GREEN)

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
        // color button
        imageButton_color.setOnClickListener {
            val colorDialog = MaterialDialog(this)
            colorDialog.show {
                title(R.string.dialog_item_name)
                colorChooser(
                    colors = colorArray,
                    allowCustomArgb = true,
                    showAlphaSelector = true
                ) { _, color ->
                    replyIntent.putExtra(EXTRA_NEW_DIARY_COLOR, color)
                }
                positiveButton(R.string.dialog_yes) {
                    colorDialog.dismiss()
                }
                negativeButton(R.string.dialog_no) {
                    replyIntent.putExtra(EXTRA_NEW_DIARY_COLOR, 0)
                    colorDialog.dismiss()
                }
            }
        }

    }

    private fun makeDialog()
    {
        val newDialog = MaterialDialog(this)
        newDialog.show {
            title(R.string.dialog_leave_changes)
            message(R.string.dialog_check_leave)
            positiveButton(R.string.dialog_yes) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
                newDialog.dismiss()
                finish()
            }
            negativeButton(R.string.dialog_no) {
                newDialog.dismiss()
            }
        }
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
        const val EXTRA_NEW_DIARY_COLOR = "EXTRA_NEW_DIARY_COLOR"
    }
}