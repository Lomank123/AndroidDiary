package com.lomank.diary

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import kotlinx.android.synthetic.main.activity_new_note.*

class NewNoteActivity : AppCompatActivity() {

    private val permissionRequestCode = 11
    private val permissionsList = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val choosePhotoRequestCode = 1
    private var isPhotoExist = false
    private val replyIntent = Intent()
    private val colorArray = intArrayOf(RED, BLUE, GREEN)

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if(checkPermission(this, permissionsList)) {
                    val choosePhotoIntent = Intent(Intent.ACTION_PICK)
                    choosePhotoIntent.type = "image/*"
                    startActivityForResult(choosePhotoIntent, choosePhotoRequestCode)
                } else {
                    ActivityCompat.requestPermissions(this, permissionsList, permissionRequestCode)
                }
        }
        // color button
        imageButton_color_note.setOnClickListener {
            val colorDialog = MaterialDialog(this)
            colorDialog.show {
                title(R.string.dialog_item_name)
                colorChooser(
                    colors = colorArray,
                    allowCustomArgb = true,
                    showAlphaSelector = true
                ) { _, color ->
                    replyIntent.putExtra(EXTRA_NEW_NOTE_COLOR, color)
                }
                positiveButton(R.string.dialog_yes) {
                    colorDialog.dismiss()
                }
                negativeButton(R.string.dialog_no) {
                    replyIntent.putExtra(EXTRA_NEW_NOTE_COLOR, 0)
                    colorDialog.dismiss()
                }
            }

        }
    }

    private fun checkPermission(context : Context, permissions : Array<String>) : Boolean {
        var allSuccess = true
        for(i in permissions.indices) {
            if(PermissionChecker.checkCallingOrSelfPermission(
                    context,
                    permissions[i]
                ) == PermissionChecker.PERMISSION_DENIED) {
                allSuccess = false
            }
        }
        return allSuccess
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            permissionRequestCode -> {
                var allSuccess = true
                for(i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allSuccess = false
                        val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                        if(requestAgain)
                            Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(this, "go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                    }
                }
                if(allSuccess)
                    Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_NEW_NOTE = "EXTRA_NEW_NOTE"
        const val EXTRA_NEW_NOTE_IMAGE = "EXTRA_NEW_NOTE_IMAGE"
        const val EXTRA_NEW_NOTE_COLOR = "EXTRA_NEW_NOTE_COLOR"
    }
}
