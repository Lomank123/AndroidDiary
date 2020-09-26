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
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.activity_new_diary.*

class NewDiaryActivity : AppCompatActivity() {

    private val permissionRequestCode = 11
    private val permissionsList = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    // RequestCode для выбора фото
    private val choosePhotoRequestCode = 1
    private var isPhotoExist = false
    private val colorArray = intArrayOf(RED, BLUE, GREEN)

    private val replyIntent = Intent()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_diary)

        MobileAds.initialize(this)
        adView3.loadAd(AdRequest.Builder().build())

        val colorChoose = findViewById<MaterialCardView>(R.id.color_change_view)

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkPermission(this, permissionsList)) {
                    makePhotoChooseIntent()
                } else {
                    ActivityCompat.requestPermissions(this, permissionsList, permissionRequestCode)
                }
            } else {
                makePhotoChooseIntent()
            }

        }
        // color button
        change_color_button.setOnClickListener {
            val colorDialog = MaterialDialog(this)
            colorDialog.show {
                title(R.string.dialog_item_name)
                colorChooser(
                    colors = colorArray,
                    allowCustomArgb = true,
                    showAlphaSelector = true
                ) { _, color ->
                    replyIntent.putExtra(EXTRA_NEW_DIARY_COLOR, color)
                    colorChoose.setCardBackgroundColor(color)
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
            val uriImage = data?.data
            contentResolver.takePersistableUriPermission(uriImage!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            Glide.with(this).load(uriImage).into(imageView_new_diary)
            replyIntent.putExtra(EXTRA_NEW_DIARY_IMAGE, uriImage.toString())
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
        const val EXTRA_NEW_DIARY = "EXTRA_NEW_DIARY"
        const val EXTRA_NEW_DIARY_IMAGE = "EXTRA_NEW_DIARY_IMAGE"
        const val EXTRA_NEW_DIARY_COLOR = "EXTRA_NEW_DIARY_COLOR"
    }
}