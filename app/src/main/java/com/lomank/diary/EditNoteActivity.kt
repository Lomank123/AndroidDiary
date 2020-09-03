package com.lomank.diary

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_new_note.*
import roomdatabase.Note

class EditNoteActivity : AppCompatActivity() {

    private val permissionRequestCode = 11
    private val permissionsList = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val choosePhotoRequestCode = 1
    private var isPhotoChanged = false
    private var isPhotoExist = false
    private val colorArray = intArrayOf(Color.RED, Color.BLUE, Color.GREEN)
    private val replyIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)

        val note = intent.getSerializableExtra("noteSerializableEdit") as? Note

        edit_note.setText(note!!.name)
        edit_text_note.setText(note.content)

        // photo
        if (note.img != null && note.img != "") {
            isPhotoExist = true
            Glide.with(this).load(note.img).into(imageView_note)
        }
        else
            imageView_note.setImageResource(R.drawable.blank_sheet)

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
        button_cancel_note.setOnClickListener {
            // Если изменения были, спрашиваем, хочет ли пользователь покинуть окно
            if (isPhotoChanged || edit_note.text.toString() != note.name || edit_text_note.text.toString() != note.content)
                makeDialog()
            else {
                setResult(Activity.RESULT_CANCELED, replyIntent)
                finish()
            }
        }
        // Кнопка Delete
        delete_photo_button_note.setOnClickListener {
            if (isPhotoExist) {
                isPhotoChanged = note.img != null
                isPhotoExist = false
                imageView_note.setImageResource(R.drawable.blank_sheet)
                // Кладем в экстра данные картинки пустую строку
                replyIntent.putExtra(EXTRA_IMAGE_EDIT_NOTE,"")
            }
        }
        photo_button_note.setOnClickListener {
            // Выбираем фото из галереи
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkPermission(this, permissionsList)) {
                    makePhotoChooseIntent()
                } else {
                    ActivityCompat.requestPermissions(this, permissionsList, permissionRequestCode)
                }
            } else {
                makePhotoChooseIntent()
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
                    replyIntent.putExtra(EXTRA_COLOR_EDIT_NOTE, color)
                }
                positiveButton(R.string.dialog_yes) {
                    colorDialog.dismiss()
                }
                negativeButton(R.string.dialog_no) {
                    colorDialog.dismiss()
                }
            }
        }
    }

    private fun makePhotoChooseIntent() {
        val choosePhotoIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        choosePhotoIntent.type = "image/*"
        startActivityForResult(choosePhotoIntent, choosePhotoRequestCode)
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
            val uriImage = data?.data

            contentResolver.takePersistableUriPermission(uriImage!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            Glide.with(this).load(uriImage).into(imageView_note)

            replyIntent.putExtra(EXTRA_IMAGE_EDIT_NOTE, uriImage.toString())
            isPhotoChanged = note!!.img != uriImage.toString()
            isPhotoExist = true
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
                            Toast.makeText(this, resources.getString(R.string.perm_denied), Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(this, resources.getString(R.string.perm_denied_again), Toast.LENGTH_SHORT).show()
                    }
                }
                if(allSuccess) {
                    Toast.makeText(this, resources.getString(R.string.perm_granted), Toast.LENGTH_SHORT).show()
                    makePhotoChooseIntent()
                }
            }
        }
    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_EDIT_NOTE = "EXTRA_EDIT_NOTE"
        const val EXTRA_IMAGE_EDIT_NOTE = "EXTRA_IMAGE_EDIT_NOTE"
        const val EXTRA_COLOR_EDIT_NOTE = "EXTRA_COLOR_EDIT_NOTE"
    }
}