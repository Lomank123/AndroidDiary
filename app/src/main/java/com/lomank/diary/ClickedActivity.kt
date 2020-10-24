package com.lomank.diary

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.bumptech.glide.Glide
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.FishBun.Companion.INTENT_PATH
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import kotlinx.android.synthetic.main.activity_clicked.*
import roomdatabase.ExtendedDiary
import roomdatabase.Note
import java.io.File

class ClickedActivity : AppCompatActivity() {
    private val permissionRequestCode = 145
    private val newNoteRequestCode = 111
    private val openNoteRequestCode = 222
    private val photoActivityRequestCode = 487
    private val cameraActivityRequestCode = 179

    private var primalColor : Int? = null

    private val permissionsList1 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )
    private val permissionsList2 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    // image handle
    private var listOfImages = mutableListOf<String?>()
    private var listOfOrientations = mutableListOf<Int?>()

    private var isVoiceLayoutOpen = false // for layout
    private var isRecording = false // for counting seconds

    private var mediaRecorder : MediaRecorder? = MediaRecorder()    // Запись
    private var mediaPlayer : MediaPlayer? = MediaPlayer()          // Воспроизведение
    private var fileName : String = ""                              // Имя файла

    // Пустышка, которая в завимимости от requestCode будет изменена
    // ВАЖНО!!! Эта заметка еще не в базе данных
    private var note = Note("Input name")

    // Request code
    private var requestCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked)
        setSupportActionBar(materialToolbar_clicked)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        materialToolbar_clicked.overflowIcon = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_baseline_more_vert_32,
            null
        )
        materialToolbar_clicked.setNavigationIcon(R.drawable.ic_baseline_arrow_back_gray_32)

        // getting the request code from parent activity
        requestCode = intent.getIntExtra("requestCode", 0)

        // getting parent diary
        val extDiaryParent = intent.getSerializableExtra("diaryParent") as ExtendedDiary

        // for color
        val colorView = findViewById<View>(R.id.color_indicator)

        if(requestCode == newNoteRequestCode) // request for new note
        {
            // setting filename template
            fileName = this.getExternalFilesDir(null)!!.absolutePath + "/voice_note_empty.3gpp"
            if (File(fileName).exists())
                File(fileName).delete()

            // parentId for new note
            note.parentId = extDiaryParent.diary.id
        }
        else if(requestCode == openNoteRequestCode) // request for open existing note
        {
            // note exist
            note = intent.getSerializableExtra("openNote") as Note
            // setting filename template
            fileName = this.getExternalFilesDir(null)!!.absolutePath + "/voice_note_${note.id}.3gpp"

            // setting name and content of a note
            editText_name.setText(note.name)
            editText_content.setText(note.content)

            // Voice note
            if(File(fileName).exists()) {
                note.voice = true
                playStart()
                setProgressBar()
                showProgressBar()
            } else {
                record_time_dis.text = this.resources.getString(R.string.no_voice_note)
            }

            // setting images
            if(note.images != null) {
                listOfImages = note.images as MutableList<String?>
                listOfOrientations = note.imagesOrientation as MutableList<Int?>
            }

            // color
            primalColor = note.color
            if(note.color != null && note.color != ResourcesCompat.getColor(
                    resources,
                    R.color.white,
                    null
                )){
                colorView.setBackgroundColor(note.color!!)
            }
        }

        // FAB save
        fab_save.setOnClickListener{
            saveNote()
        }

        // color button
        color_choose_button.setOnClickListener {
            val colorWhite = ResourcesCompat.getColor(resources, R.color.white, null)
            val colorPrimaryOrange = ResourcesCompat.getColor(
                resources,
                R.color.primary_color1,
                null
            )
            val colorArray = intArrayOf(
                colorWhite, colorPrimaryOrange,
                ResourcesCompat.getColor(resources, R.color.pink, null),
                ResourcesCompat.getColor(resources, R.color.green, null),
                ResourcesCompat.getColor(resources, R.color.yellow, null),
                ResourcesCompat.getColor(resources, R.color.blue, null),
                ResourcesCompat.getColor(resources, R.color.grass, null),
                ResourcesCompat.getColor(resources, R.color.purple, null)
            )
            val colorDialog = MaterialDialog(this)
            colorDialog.show {
                title(R.string.dialog_color_choose_title)
                colorChooser(
                    colors = colorArray,
                    allowCustomArgb = true,
                    showAlphaSelector = true
                ) { _, color ->
                    if(color == colorWhite)
                        colorView.setBackgroundColor(colorPrimaryOrange)
                    else
                        colorView.setBackgroundColor(color)

                    note.color = color
                }
                positiveButton(R.string.dialog_yes) {
                    colorDialog.dismiss()
                }
                negativeButton(R.string.dialog_no) {
                    colorDialog.dismiss()
                }
            }
        }

        // PHOTO BUTTONS

        // layout appearing button (image button)
        photo_choose_button.setOnClickListener {
            // permission check
            requestForCheckPermission(2)
            if(checkPermission(this, permissionsList2)){
                if(listOfImages.size < 3) {
                    // closing another layout if it was opened
                    if (isVoiceLayoutOpen)
                        animateVoiceLayout()
                    makePhotoChooseIntent()
                } else {
                    Toast.makeText(
                        this,
                        this.resources.getString(R.string.max_image_attached),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        camera_button.setOnClickListener {
            requestForCheckPermission(2)
            if(checkPermission(this, permissionsList2)) {
                if(listOfImages.size < 3) {
                    // closing another layout if it was opened
                    if (isVoiceLayoutOpen)
                        animateVoiceLayout()
                    makeCameraIntent()
                } else {
                    Toast.makeText(
                        this,
                        this.resources.getString(R.string.max_image_attached),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // VOICE BUTTONS

        // layout appearing button (mic icon)
        mic_button.setOnClickListener {
            // permission check
            requestForCheckPermission(1)
            if(checkPermission(this, permissionsList1)) {
                // layout_voice animation
                animateVoiceLayout()
            }
        }

        // if voice note already exist

        play_btn_active.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                pausePlay()
                play_btn_active.setImageResource(R.drawable.ic_baseline_play_arrow_32)
            } else {
                resumePlay()
                play_btn_active.setImageResource(R.drawable.ic_baseline_pause_32)
                progressUpdater()
            }
        }

        delete_btn_active.setOnClickListener {
            val dialog = MaterialDialog(this)
            dialog.show {
                title(R.string.dialog_delete_voice_title)
                message(R.string.dialog_delete_voice_message)
                positiveButton(R.string.dialog_yes) {
                    recordDelete()
                    dialog.dismiss()
                }
                negativeButton(R.string.dialog_no) {
                    dialog.dismiss()
                }
            }
        }

        // if voice note doesn't exist

        // слушатель на кнопку начала записи голоса
        record_voice_dis.setOnClickListener {

            isRecording = true
            recordProgressUpdater(0)
            recordStart()

            record_voice_dis.visibility = INVISIBLE
            stop_recording_voice_dis.visibility = VISIBLE
        }

        // слушатель на кнопку остановки записи голоса
        stop_recording_voice_dis.setOnClickListener {
            note.voice = true
            isRecording = false
            recordStop()
            playStart()
            setProgressBar()
            showProgressBar()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // Back button
    override fun onBackPressed() {
        if(requestCode == newNoteRequestCode) {
            if (!TextUtils.isEmpty(editText_name.text) || !TextUtils.isEmpty(editText_content.text) || (note.color != null) || note.voice) {
                saveDialogShow(requestCode)
            } else {
                super.onBackPressed()
            }
        }
        else if (requestCode == openNoteRequestCode) {
            if((editText_name.text.toString() != note.name) || (editText_content.text.toString() != note.content) || (note.color != primalColor)) {
                saveDialogShow(requestCode)
            } else {
                checkVoiceNote()
            }
        }
        else {
            super.onBackPressed()
        }
    }

    // Сохраняет заметку
    private fun saveNote()
    {
        // обновляем введенный текст
        note.name = editText_name.text.toString()
        note.content = editText_content.text.toString()

        // Проверяем голосовую заметку
        if(!note.voice) {
            val outFile = File(fileName)
            if (outFile.exists())
                outFile.delete()
        }

        val replyIntent = Intent()
        replyIntent.putExtra(EXTRA_REPLY_EDIT, note)
        setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK
        finish()
    }

    private fun saveDialogShow(code: Int) {
        val dialog = MaterialDialog(this@ClickedActivity)
        dialog.show{
            title(R.string.dialog_save)
            message(R.string.dialog_check_save_changes)
            positiveButton(R.string.dialog_yes) {
                saveNote()
                dialog.dismiss()
            }
            if(code == newNoteRequestCode){
                negativeButton(R.string.dialog_no) {
                    // because we need to delete unused voice note
                    if(File(fileName).exists())
                        File(fileName).delete()

                    setResult(Activity.RESULT_CANCELED)
                    dialog.dismiss()
                    finish()
                }
            } else if (code == openNoteRequestCode) {
                negativeButton(R.string.dialog_no) {
                    dialog.dismiss()
                    checkVoiceNote()
                }
            }
        }
    }

    private fun checkVoiceNote(){
        val oldNote = intent.getSerializableExtra("openNote") as Note
        oldNote.voice = File(fileName).exists()
        oldNote.color = primalColor
        val replyIntent = Intent()
        replyIntent.putExtra(EXTRA_REPLY_CANCELED, oldNote)
        setResult(Activity.RESULT_CANCELED, replyIntent)
        finish()
    }

    private fun makePhotoChooseIntent() {
        // FishBun multiple photo choose
        FishBun.with(this@ClickedActivity)
            .setImageAdapter(GlideAdapter())
            .setMaxCount(3 - listOfImages.size)
            .setActionBarColor(
                ResourcesCompat.getColor(resources, R.color.primary_color1, null),
                ResourcesCompat.getColor(resources, R.color.primary_color_dark1, null))
            .textOnImagesSelectionLimitReached(this.resources.getString(R.string.photo_choose_limit_reached))
            .setAllViewTitle(this.resources.getString(R.string.photo_choose_all_views_title))
            .setActionBarTitle(this.resources.getString(R.string.photo_choose_title))
            .startAlbum()

        // old method
        //val choosePhotoIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        //choosePhotoIntent.type = "image/*"
        //startActivityForResult(choosePhotoIntent, choosePhotoRequestCode)
    }

    private fun makeCameraIntent() {
        val cameraIntent = Intent(this, CameraActivity::class.java)
        startActivityForResult(cameraIntent, cameraActivityRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Choosing from the Gallery
        if (requestCode == FishBun.FISHBUN_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            // FishBun multiple photo choose
            val path = data?.getParcelableArrayListExtra<Uri>(INTENT_PATH)

            if (path != null) {
                for(image in path){
                    // setting photo
                    listOfImages.add(image.toString())
                    listOfOrientations.add(0)
                }
            }
            // old method
            //contentResolver.takePersistableUriPermission(
            //    image!!,
            //    Intent.FLAG_GRANT_READ_URI_PERMISSION
            //)

            // check is implemented in button click listener

            note.images = listOfImages
            note.imagesOrientation = listOfOrientations
        }
        // PhotoViewerActivity
        if(requestCode == photoActivityRequestCode && resultCode == Activity.RESULT_OK){
            val newNote = data?.getSerializableExtra(PhotoViewerActivity.EXTRA_REPLY_PHOTO_VIEWER) as Note
            listOfImages = newNote.images as MutableList<String?>
            listOfOrientations = newNote.imagesOrientation as MutableList<Int?>
            note.images = listOfImages
            note.imagesOrientation = listOfOrientations
        }
        // CameraActivity
        if(requestCode == cameraActivityRequestCode && resultCode == Activity.RESULT_OK){
            // full image path
            val image = data?.getStringExtra(CameraActivity.EXTRA_REPLY_CAMERA_INTENT)

            // check is implemented in button click listener
            listOfImages.add(image)
            listOfOrientations.add(0)
            note.images = listOfImages
            note.imagesOrientation = listOfOrientations
        }
    }

    private fun setImage(){
        val viewList = arrayListOf<ImageView>(imageClicked1, imageClicked2, imageClicked3)
        if (note.images != null) {
            if(note.images!!.isNotEmpty()) {
                cardView_images.visibility = VISIBLE
                for(i in note.images!!.indices) {
                    // setting image
                    viewList[i].visibility = VISIBLE
                    Glide.with(this).load(note.images!![i])
                        .override(800, 1000)
                        .into(viewList[i])
                    // open photo listener
                    viewList[i].setOnClickListener {
                        val imageIntent = Intent(this, PhotoViewerActivity::class.java)
                        imageIntent.putExtra("currentPos", i)
                        imageIntent.putExtra("images", note)
                        startActivityForResult(imageIntent, photoActivityRequestCode)
                    }
                }
            } else {
                cardView_images.visibility = GONE
            }
            // hiding other ImageViews without images
            for(i in note.images!!.size until viewList.size){
                viewList[i].visibility = GONE
            }
            // Setting margins
            setLayoutParams()
        } else {
            cardView_images.visibility = GONE
        }
    }

    private fun setLayoutParams(){
        if(note.images!!.size == 1) {
            val params1 = (imageClicked1.layoutParams as ViewGroup.MarginLayoutParams)
            params1.setMargins(0, 0, 0, 0)
            imageClicked1.layoutParams = params1
        } else {
            if(note.images!!.size == 2) {
                val params1 = (imageClicked1.layoutParams as ViewGroup.MarginLayoutParams)
                params1.setMargins(0, 0, 9, 0)
                imageClicked1.layoutParams = params1
                val params2 = (imageClicked2.layoutParams as ViewGroup.MarginLayoutParams)
                params2.setMargins(0, 0, 0, 0)
                imageClicked2.layoutParams = params2
            } else { // 3
                val params1 = (imageClicked1.layoutParams as ViewGroup.MarginLayoutParams)
                params1.setMargins(0, 0, 0, 0)
                imageClicked1.layoutParams = params1
                val params2 = (imageClicked2.layoutParams as ViewGroup.MarginLayoutParams)
                params2.setMargins(9, 0, 9, 0)
                imageClicked2.layoutParams = params2
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        val extDiaryParent = intent.getSerializableExtra("diaryParent") as ExtendedDiary

        // setting image
        setImage()

        // setting image on the background
        if(extDiaryParent.diary.img != null && prefs!!.getBoolean("img_check", true)) {
            imageView_background.visibility = VISIBLE
            Glide.with(this).load(extDiaryParent.diary.img).into(imageView_background)
        } else {
            imageView_background.visibility = GONE
        }

        // resetting progressBar and media player
        seekBar_active.progress = 0
        start_time_active.text = this.resources.getString(R.string.start_time)
        play_btn_active.setImageResource(R.drawable.ic_baseline_play_arrow_32)

        playStart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_opened_note, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.note_settings -> {
                val intentSettings = Intent(this, SettingsHolderActivity::class.java)
                startActivity(intentSettings)
            }
            R.id.share_btn_edit -> {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, editText_content.text.toString())
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    // Below is media player code, animation and permission check

    private fun setProgressBar(){
        // Устанавливаем время начала и конца
        val elapsedTime1 = createTimeLabel(0)
        start_time_active.text = elapsedTime1
        // seekBar
        seekBar_active.visibility = VISIBLE
        seekBar_active.max = mediaPlayer!!.duration
        seekBar_active.progress = 0
        seekBar_active.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayer!!.seekTo(progress)

                        val elapsedTime = createTimeLabel(progress)
                        start_time_active.text = elapsedTime
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            }
        )
    }

    private fun showProgressBar(){
        record_voice_dis.visibility = INVISIBLE
        record_time_dis.visibility = INVISIBLE
        stop_recording_voice_dis.visibility = INVISIBLE

        play_btn_active.setImageResource(R.drawable.ic_baseline_play_arrow_32)
        play_btn_active.visibility = VISIBLE
        delete_btn_active.visibility = VISIBLE
        start_time_active.visibility = VISIBLE
    }

    private fun animateVoiceLayout(){
        isVoiceLayoutOpen = !isVoiceLayoutOpen
        if (isVoiceLayoutOpen) {
            layout_voice.visibility = VISIBLE
            layout_voice.alpha = 0.0f
            layout_voice.animate().translationY(-layout_voice.height.toFloat()).alpha(1.0f).setListener(
                null
            ).start()
        } else {
            // stop playing voice note
            if(mediaPlayer != null)
                if (mediaPlayer!!.isPlaying) {
                    pausePlay()
                    play_btn_active.setImageResource(R.drawable.ic_baseline_play_arrow_32)
                }

            layout_voice.animate().translationY(0f).alpha(0.0f).setListener(object :
                AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    layout_voice.visibility = GONE
                }
            }).start()
        }
    }

    private fun checkPermission(context: Context, permissions: Array<String>) : Boolean {
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

    // 1 for audio, 2 for images
    private fun requestForCheckPermission(requestCode: Int){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(requestCode == 1){
                if (!checkPermission(this, permissionsList1)) {
                    ActivityCompat.requestPermissions(this, permissionsList1, permissionRequestCode)
                }
            } else if (requestCode == 2) {
                if (!checkPermission(this, permissionsList2)) {
                    ActivityCompat.requestPermissions(this, permissionsList2, permissionRequestCode)
                }
            }
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
                var allGranted = true
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        val requestAgain =
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                                permissions[i]
                            )
                        if (requestAgain) {
                            Toast.makeText(
                                this,
                                resources.getString(R.string.perm_denied),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                resources.getString(R.string.perm_denied_again),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        allGranted = false
                    }
                }
                if (allGranted)
                    Toast.makeText(
                        this,
                        this.resources.getString(R.string.perm_one_more),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }

    // Благодаря этой функции ползунок постепено двигается и меняется время
    private fun progressUpdater()
    {
        if (mediaPlayer != null) {
            seekBar_active.progress = mediaPlayer!!.currentPosition

            val elapsedTime = createTimeLabel(mediaPlayer!!.currentPosition)
            start_time_active.text = elapsedTime

            if (mediaPlayer!!.isPlaying) {
                val notify = Runnable {
                    progressUpdater()
                }
                Handler().postDelayed(notify, 0)
            }
            else {
                play_btn_active.setImageResource(R.drawable.ic_baseline_play_arrow_32)
            }
        }
    }

    private fun recordProgressUpdater(time: Int)
    {
        val recordingTime = createTimeLabel(time)
        record_time_dis.text = recordingTime
        val newTime = time + 1000
        if (isRecording) {
            val notify1 = Runnable {
                recordProgressUpdater(newTime)
            }
            Handler().postDelayed(notify1, 1000)
        }
    }

    // Переводит значение Int в формат (min:sec)
    private fun createTimeLabel(time: Int) : String
    {
        var timeLabel: String
        val min = time / 1000 / 60
        val sec = time / 1000 % 60

        timeLabel = "${min}:"
        if (sec < 10)
            timeLabel += "0"
        timeLabel += sec

        return timeLabel
    }

    private fun recordDelete()
    {
        releaseRecorder()
        releasePlayer()
        note.voice = false
        val outFile = File(fileName)
        if (outFile.exists())
            outFile.delete()

        record_voice_dis.visibility = VISIBLE
        record_time_dis.text = this.resources.getString(R.string.no_voice_note)
        record_time_dis.visibility = VISIBLE
        stop_recording_voice_dis.visibility = INVISIBLE

        play_btn_active.visibility = INVISIBLE
        play_btn_active.setImageResource(R.drawable.ic_baseline_pause_32)
        delete_btn_active.visibility = INVISIBLE
        start_time_active.visibility = INVISIBLE
        seekBar_active.visibility = INVISIBLE
    }

    private fun recordStart()
    {
        try {
            releaseRecorder()
            val outFile = File(fileName)
            if (outFile.exists())
                outFile.delete()

            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder?.setOutputFile(fileName)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun recordStop()
    {
        mediaRecorder?.stop()
    }

    private fun playStart()
    {
        try {
            releasePlayer()
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(fileName)
            mediaPlayer?.prepare()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseRecorder() {
        if(mediaRecorder != null)
        {
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    private fun releasePlayer() {
        if(mediaPlayer != null)
        {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    private fun resumePlay()
    {
        mediaPlayer?.start()
    }

    private fun pausePlay()
    {
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        releaseRecorder()
    }

    override fun onPause() {
        super.onPause()
        pausePlay()
        releasePlayer()
        releaseRecorder()
    }

    override fun onStop() {
        super.onStop()
        pausePlay()
        releasePlayer()
        releaseRecorder()
    }

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_REPLY_EDIT = "EXTRA_REPLY_CLICKED_ACTIVITY"
        const val EXTRA_REPLY_CANCELED = "EXTRA_REPLY_CLICKED_CANCELED"
    }
}