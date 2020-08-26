package com.lomank.diary

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import kotlinx.android.synthetic.main.activity_new_diary.*
import roomdatabase.Diary

class EditDiaryActivity : AppCompatActivity() {

    private val choosePhotoRequestCode = 1
    private var isPhotoChanged = false
    private var isPhotoExist = false
    private val colorArray = intArrayOf(Color.RED, Color.BLUE, Color.GREEN)
    private val replyIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_diary)

        val diary = intent.getSerializableExtra("diaryEdit") as? Diary

        edit_diary.setText(diary!!.name)
        edit_content.setText(diary.content)

        if (diary.img != null && diary.img != "") {
            isPhotoExist = true
            val uriImage = Uri.parse(diary.img)
            imageView_new_diary.setImageURI(uriImage)
        }
        else
            imageView_new_diary.setImageResource(R.drawable.logo)

        button_save.setOnClickListener {
            if (TextUtils.isEmpty(edit_diary.text)) {
                // устанавливаем результат как RESULT_CANCELED (отменен)
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                diary.name = edit_diary.text.toString()
                diary.content = edit_content.text.toString()
                replyIntent.putExtra(EXTRA_EDIT_DIARY, diary)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            // завершаем работу с активити
            finish()
        }
        button_cancel_diary.setOnClickListener {
            // Если изменения были, спрашиваем, хочет ли пользователь покинуть окно
            if (isPhotoChanged || edit_diary.text.toString() != diary.name || edit_content.text.toString() != diary.content)
                makeDialog()
            else {
                setResult(Activity.RESULT_CANCELED, replyIntent)
                finish()
            }
        }
        // Кнопка Delete
        delete_photo_button.setOnClickListener{
            if (isPhotoExist) {
                isPhotoChanged = diary.img != null
                isPhotoExist = false
                imageView_new_diary.setImageResource(R.drawable.logo)
                // Кладем в экстра данные картинки пустую строку
                replyIntent.putExtra(EXTRA_EDIT_DIARY_IMAGE, "")
            }
        }
        // Кнопка Choose photo
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
                    replyIntent.putExtra(EXTRA_EDIT_DIARY_COLOR, color)
                }
                positiveButton(R.string.dialog_yes) {
                    colorDialog.dismiss()
                }
                negativeButton(R.string.dialog_no) {
                    replyIntent.putExtra(EXTRA_EDIT_DIARY_COLOR, 0)
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
        val diary = intent.getSerializableExtra("diaryEdit") as? Diary
        // Если изменения были, спрашиваем, хочет ли пользователь покинуть окно
        if (isPhotoChanged || edit_diary.text.toString() != diary!!.name || edit_content.text.toString() != diary.content)
            makeDialog()
        else
            super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == choosePhotoRequestCode && resultCode == Activity.RESULT_OK)
        {
            val diary = intent.getSerializableExtra("diaryEdit") as? Diary
            imageView_new_diary.setImageURI(data?.data)
            replyIntent.putExtra(EXTRA_EDIT_DIARY_IMAGE, data?.data.toString())

            isPhotoChanged = diary!!.img != data?.data.toString()
            isPhotoExist = true
        }
    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_EDIT_DIARY = "EXTRA_EDIT_DIARY"
        const val EXTRA_EDIT_DIARY_IMAGE = "EXTRA_EDIT_DIARY_IMAGE"
        const val EXTRA_EDIT_DIARY_COLOR = "EXTRA_EDIT_DIARY_COLOR"
    }
}