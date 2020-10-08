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
import androidx.core.content.res.ResourcesCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.activity_new_diary.*
import roomdatabase.Diary

class EditDiaryActivity : AppCompatActivity() {

    private val permissionRequestCode = 11
    private val permissionsList = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val choosePhotoRequestCode = 1
    private var isPhotoChanged = false
    private var isPhotoExist = false
    private val replyIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_diary)

        MobileAds.initialize(this)
        adView3.loadAd(AdRequest.Builder().build())

        val diary = intent.getSerializableExtra("diaryEdit") as? Diary

        val colorChoose = findViewById<MaterialCardView>(R.id.color_change_view)

        if(diary!!.color != null && diary.color != 0)
            colorChoose.setCardBackgroundColor(diary.color!!)

        edit_diary.setText(diary.name)
        edit_content.setText(diary.content)

        // photo
        if (diary.img != null && diary.img != "") {
            isPhotoExist = true
            Glide.with(this).load(diary.img).into(imageView_new_diary)
        } else
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

        val colorArray = intArrayOf(ResourcesCompat.getColor(resources, R.color.white, null),
            ResourcesCompat.getColor(resources, R.color.pink, null),
            ResourcesCompat.getColor(resources, R.color.green, null),
            ResourcesCompat.getColor(resources, R.color.yellow, null),
            ResourcesCompat.getColor(resources, R.color.blue, null),
            ResourcesCompat.getColor(resources, R.color.grass, null),
            ResourcesCompat.getColor(resources, R.color.purple, null))

        change_color_button.setOnClickListener {
            val colorDialog = MaterialDialog(this)
            colorDialog.show {
                title(R.string.dialog_color_choose_title)
                colorChooser(
                    colors = colorArray,
                    allowCustomArgb = true,
                    showAlphaSelector = true
                ) { _, color ->
                    replyIntent.putExtra(EXTRA_EDIT_DIARY_COLOR, color)
                    colorChoose.setCardBackgroundColor(color)
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
            // setting photo
            val uriImage = data?.data
            contentResolver.takePersistableUriPermission(uriImage!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            Glide.with(this).load(uriImage).into(imageView_new_diary)

            replyIntent.putExtra(EXTRA_EDIT_DIARY_IMAGE, uriImage.toString())
            isPhotoChanged = diary!!.img != uriImage.toString()
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
        const val EXTRA_EDIT_DIARY = "EXTRA_EDIT_DIARY"
        const val EXTRA_EDIT_DIARY_IMAGE = "EXTRA_EDIT_DIARY_IMAGE"
        const val EXTRA_EDIT_DIARY_COLOR = "EXTRA_EDIT_DIARY_COLOR"
    }
}