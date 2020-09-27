package com.lomank.diary

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_clicked.*
import roomdatabase.ExtendedDiary
import roomdatabase.Note
import java.io.File
import java.lang.Exception

class ClickedActivity : AppCompatActivity() {

    private val permissionRequestCode = 145
    private val choosePhotoRequestCode = 12
    private val newNoteRequestCode = 111
    private val openNoteRequestCode = 222

    private var primalColor : Int? = null
    private var primalPhoto : String? = null

    private var isPhotoLayoutOpen = false
    private var isPhotoExist = false

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

        // prefs
        //val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)

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
            // setting photo of a diary
            note.img = extDiaryParent.diary.img
            primalPhoto = note.img
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
            // image
            primalPhoto = note.img
            // color
            primalColor = note.color
            if(note.color != null && note.color != ResourcesCompat.getColor(resources, R.color.white, null)){
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
            val colorPrimaryOrange = ResourcesCompat.getColor(resources, R.color.primary_color1, null)
            val colorArray = intArrayOf(Color.RED, Color.BLUE, Color.GREEN, colorPrimaryOrange, colorWhite)
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
            requestForCheckPermission()

            // closing another layout if it was opened
            if(isVoiceLayoutOpen)
                animateVoiceLayout()

            // photo_choose_layout animation
            animatePhotoLayout()

        }
        // photo button
        button_choose_photo.setOnClickListener {
            makePhotoChooseIntent()
        }
        // reset button
        button_reset_photo.setOnClickListener {
            if (isPhotoExist) {
                isPhotoExist = false
                imageView_photo.setImageResource(R.drawable.empty_note_photo)
                imageView_background.setImageDrawable(null)
                note.img = null
            }
        }

        // VOICE BUTTONS

        // layout appearing button (mic icon)
        mic_button.setOnClickListener {
            // permission check
            requestForCheckPermission()

            // closing another layout if it was opened
            if(isPhotoLayoutOpen)
                animatePhotoLayout()
            // layout_voice animation
            animateVoiceLayout()
        }

        // if voice note already exist

        play_btn_active.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                pausePlay()
                play_btn_active.setImageResource(android.R.drawable.ic_media_play)
            } else {
                resumePlay()
                play_btn_active.setImageResource(android.R.drawable.ic_media_pause)
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

    // Back button
    override fun onBackPressed() {
        if(requestCode == newNoteRequestCode) {
            if (!TextUtils.isEmpty(editText_name.text) || !TextUtils.isEmpty(editText_content.text) || note.voice ||
                (note.img != primalPhoto) || (note.color != null)) {
                saveDialogShow(requestCode)
            } else {
                super.onBackPressed()
            }
        }
        else if (requestCode == openNoteRequestCode) {
            if((editText_name.text.toString() != note.name) || (editText_content.text.toString() != note.content) || (note.color != primalColor) ||
                (note.img != primalPhoto)) {
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
        Toast.makeText(this, resources.getString(R.string.saved), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun saveDialogShow(code : Int) {
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
        oldNote.img = primalPhoto
        val replyIntent = Intent()
        replyIntent.putExtra(EXTRA_REPLY_CANCELED, oldNote)
        setResult(Activity.RESULT_CANCELED, replyIntent)
        finish()
    }

    private fun makePhotoChooseIntent() {
        val choosePhotoIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        choosePhotoIntent.type = "image/*"
        startActivityForResult(choosePhotoIntent, choosePhotoRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == choosePhotoRequestCode && resultCode == Activity.RESULT_OK)
        {
            // setting photo
            val uriImage = data?.data
            contentResolver.takePersistableUriPermission(uriImage!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            Glide.with(this).load(uriImage).into(imageView_background)
            Glide.with(this).load(uriImage).into(imageView_photo)
            note.img = uriImage.toString()

            isPhotoExist = true
        }
    }

    private fun setImage(prefs : SharedPreferences){
        if (note.img != null && note.img != "") {
            isPhotoExist = true
            Glide.with(this).load(note.img).into(imageView_photo)
            if (prefs.getBoolean("img_check", false)) {
                Glide.with(this).load(note.img).into(imageView_background)
            } else {
                imageView_background.setImageDrawable(null)
            }
        } else {
            imageView_photo.setImageResource(R.drawable.empty_note_photo)
        }

    }

    override fun onResume() {
        super.onResume()
        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)

        // setting image
        setImage(prefs!!)

        // resetting progressBar and media player
        seekBar_active.progress = 0
        start_time_active.text = this.resources.getString(R.string.start_time)
        play_btn_active.setImageResource(android.R.drawable.ic_media_play)

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
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
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

        play_btn_active.setImageResource(android.R.drawable.ic_media_play)
        play_btn_active.visibility = VISIBLE
        delete_btn_active.visibility = VISIBLE
        start_time_active.visibility = VISIBLE
    }

    private fun animateVoiceLayout(){
        isVoiceLayoutOpen = !isVoiceLayoutOpen
        if (isVoiceLayoutOpen) {
            layout_voice.visibility = VISIBLE
            layout_voice.alpha = 0.0f
            layout_voice.animate().translationY(-layout_voice.height.toFloat()).alpha(1.0f).setListener(null).start()
        } else {
            // stop playing voice note
            if(mediaPlayer != null)
                if (mediaPlayer!!.isPlaying) {
                    pausePlay()
                    play_btn_active.setImageResource(android.R.drawable.ic_media_play)
                }

            layout_voice.animate().translationY(0f).alpha(0.0f).setListener(object :
                AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    layout_voice.visibility = GONE
                }}).start()
        }
    }

    private fun animatePhotoLayout(){
        isPhotoLayoutOpen = !isPhotoLayoutOpen
        if (isPhotoLayoutOpen) {
            photo_choose_layout.visibility = VISIBLE
            photo_choose_layout.alpha = 0.0f
            photo_choose_layout.animate().translationY(-photo_choose_layout.height.toFloat()).alpha(1.0f).setListener(null).start()
        } else {
            photo_choose_layout.animate().translationY(0f).alpha(0.0f).setListener(object :
                AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    photo_choose_layout.visibility = GONE
                }}).start()
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

    private fun requestForCheckPermission(){
        val permissionsList = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if(!checkPermission(this, permissionsList)) {
                ActivityCompat.requestPermissions(this, permissionsList, permissionRequestCode)
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
                for(i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                        if(requestAgain)
                            Toast.makeText(this, resources.getString(R.string.perm_denied), Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(this, resources.getString(R.string.perm_denied_again), Toast.LENGTH_SHORT).show()
                    }
                }
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
                play_btn_active.setImageResource(android.R.drawable.ic_media_play)
            }
        }
    }

    private fun recordProgressUpdater(time : Int)
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
    private fun createTimeLabel(time : Int) : String
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
        play_btn_active.setImageResource(android.R.drawable.ic_media_pause)
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
        catch (e : Exception)
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
        catch (e : Exception) {
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