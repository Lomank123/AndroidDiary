package com.lomank.diary

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_clicked.*
import roomdatabase.Note
import java.io.File
import java.lang.Exception

class ClickedActivity : AppCompatActivity() {

    private val permissionRequestCode = 11
    private val permissionsList = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private var isVoice = false
    private var isVoiceExist = false
    private var isRecording = false

    private var mediaRecorder : MediaRecorder? = MediaRecorder()    // Запись
    private var mediaPlayer : MediaPlayer? = MediaPlayer()          // Воспроизведение
    private var fileName : String = ""                              // Имя файла


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked)

        val note = intent.getSerializableExtra("noteSerializable") as? Note

        fileName = this.getExternalFilesDir(null)!!.absolutePath + "/${note!!.name}_${note.id}.3gpp"

        // получаем экстра данные из NoteActivity
        textView_name.text = note.name
        editText1.setText(note.content)

        // Если голосовая заметка найдена
        if(File(fileName).exists())
        {
            isVoiceExist = true
            record_voice_dis.visibility = GONE
            record_time_dis.visibility = GONE
            stop_recording_voice_dis.visibility = GONE

            play_btn_active.visibility = VISIBLE
            delete_btn_active.visibility = VISIBLE
            end_time_active.visibility = VISIBLE
            start_time_active.visibility = VISIBLE
            // seekBar
            seekBar_active.visibility = VISIBLE

            playStart()

            seekBar_active.max = mediaPlayer!!.duration
            seekBar_active.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            mediaPlayer!!.seekTo(progress)
                            val elapsedTime = createTimeLabel(progress)
                            start_time_active.text = elapsedTime
                            val remainingTime = createTimeLabel(mediaPlayer!!.duration - progress)
                            end_time_active.text = remainingTime
                        }
                    }
                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }
                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }
                }
            )
        } else {
            record_time_dis.text = this.resources.getString(R.string.no_voice_note)
        }

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
            recordDelete()
        }

        // слушатель на кнопку начала записи голоса
        record_voice_dis.setOnClickListener {

            isRecording = true
            recordProgressUpdater(0)
            record_voice_dis.visibility = GONE
            stop_recording_voice_dis.visibility = VISIBLE

            recordStart()

            // слушатель на кнопку остановки записи голос. заметки
            stop_recording_voice_dis.setOnClickListener {

                isRecording = false
                // Если VISIBLE, можно увидеть что recordProgressUpdater успевает насчитать линшнюю секунду
                record_time_dis.visibility = GONE
                stop_recording_voice_dis.visibility = GONE

                play_btn_active.setImageResource(android.R.drawable.ic_media_play)
                play_btn_active.visibility = VISIBLE
                delete_btn_active.visibility = VISIBLE
                start_time_active.visibility = VISIBLE
                end_time_active.visibility = VISIBLE

                recordStop()
                isVoiceExist = true
                playStart()

                seekBar_active.visibility = VISIBLE
                seekBar_active.max = mediaPlayer!!.duration
                seekBar_active.progress = 0

                // Устанавливаем время начала и конца
                val elapsedTime1 = createTimeLabel(0)
                start_time_active.text = elapsedTime1
                val remainingTime1 = createTimeLabel(mediaPlayer!!.duration)
                end_time_active.text = remainingTime1

                seekBar_active.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                mediaPlayer!!.seekTo(progress)

                                val elapsedTime = createTimeLabel(progress)
                                start_time_active.text = elapsedTime
                                val remainingTime = createTimeLabel(mediaPlayer!!.duration - progress)
                                end_time_active.text = remainingTime
                            }
                        }
                        override fun onStartTrackingTouch(p0: SeekBar?) {
                        }
                        override fun onStopTrackingTouch(p0: SeekBar?) {
                        }
                    }
                )
                play_btn_active.setOnClickListener{
                    if(mediaPlayer!!.isPlaying) {
                        pausePlay()
                        play_btn_active.setImageResource(android.R.drawable.ic_media_play)
                    }
                    else {
                        resumePlay()
                        play_btn_active.setImageResource(android.R.drawable.ic_media_pause)
                        progressUpdater()
                    }
                }

                delete_btn_active.setOnClickListener{
                    recordDelete()
                }
            }
        }
        // FAB save
        fab_save.setOnClickListener{
            saveNote(note)
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

    override fun onBackPressed() {
        //super.onBackPressed()
        val note = intent.getSerializableExtra("noteSerializable") as? Note

        if(note!!.content != editText1.text.toString())
            saveDialogShow(note)
        else
            super.onBackPressed()

    }

    // Сохраняет заметку
    private fun saveNote(note : Note)
    {
        val replyIntent = Intent()
        // обновляем введенный текст
        note.content = editText1.text.toString()
        // обновляем файл голосовой заметки
        if(!isVoiceExist)
        {
            val outFile = File(fileName)
            if (outFile.exists())
                outFile.delete()
        }
        replyIntent.putExtra(EXTRA_REPLY_EDIT, note)
        setResult(Activity.RESULT_OK, replyIntent) // resultCode будет RESULT_OK
        // Завершаем работу с активити
        Toast.makeText(this, resources.getString(R.string.saved),
            Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun saveDialogShow(note : Note)
    {
        val dialog = MaterialDialog(this@ClickedActivity)
        dialog.show{
            title(R.string.dialog_save)
            message(R.string.dialog_check_save_changes)
            positiveButton(R.string.dialog_yes) {
                saveNote(note)
                dialog.dismiss()
            }
            negativeButton(R.string.dialog_no) {
                setResult(Activity.RESULT_CANCELED)
                Toast.makeText(this@ClickedActivity, resources.getString(R.string.canceled), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                finish()
            }
        }
    }

    override fun onResume()
    {
        super.onResume()
        val note = intent.getSerializableExtra("noteSerializable") as? Note
        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)

        if (prefs!!.getBoolean("img_check", false)) {
            if (note?.img != null && note.img != ""){
                Glide.with(this).load(note.img).into(imageView_clicked)
            }
            else
                imageView_clicked.setImageResource(R.drawable.blank_sheet)
        }

        // TODO: change this to materialDialog choose
        when(prefs.getString("list_preference_1", "0"))
        {
            "Default" ->
            {
                textView_name.typeface = Typeface.DEFAULT
                editText1.typeface = Typeface.DEFAULT
            }
            "Serif" ->
            {
                textView_name.typeface = Typeface.SERIF
                editText1.typeface = Typeface.SERIF
            }
            "Sans Serif" ->
            {
                textView_name.typeface = Typeface.SANS_SERIF
                editText1.typeface = Typeface.SANS_SERIF
            }
            "Default Bald" ->
            {
                textView_name.typeface = Typeface.DEFAULT_BOLD
                editText1.typeface = Typeface.DEFAULT_BOLD
            }
            "Monospace" ->
            {
                textView_name.typeface = Typeface.MONOSPACE
                editText1.typeface = Typeface.MONOSPACE
            }
        }

        seekBar_active.progress = 0
        start_time_active.text = this.resources.getString(R.string.start_time)
        end_time_active.text = createTimeLabel(seekBar_active.max)
        play_btn_active.setImageResource(android.R.drawable.ic_media_play)

        playStart()
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // когда выбираешь элемент меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val note = intent.getSerializableExtra("noteSerializable") as? Note

        when(item.itemId){

            R.id.save_btn_edit -> { // Кнопка Save
                saveNote(note!!)
            }
            R.id.share_btn_edit -> {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, editText1.text.toString())
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
            R.id.cancel_btn_edit -> { // Кнопка Cancel
                if (note!!.content != editText1.text.toString())
                    saveDialogShow(note)
                else {
                    setResult(Activity.RESULT_CANCELED)
                    Toast.makeText(
                        this, resources.getString(R.string.canceled), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            R.id.voice_btn_edit -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if(!checkPermission(this, permissionsList)) {
                        ActivityCompat.requestPermissions(this, permissionsList, permissionRequestCode)
                    }
                isVoice = !isVoice
                if (isVoice) {
                    layout_voice.visibility = VISIBLE
                } else {
                    layout_voice.visibility = GONE
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Благодаря этой функции ползунок постепено двигается и меняется время
    private fun progressUpdater()
    {
        if (mediaPlayer != null) {
            seekBar_active.progress = mediaPlayer!!.currentPosition

            val elapsedTime = createTimeLabel(mediaPlayer!!.currentPosition)
            start_time_active.text = elapsedTime
            val remainingTime = createTimeLabel(mediaPlayer!!.duration - mediaPlayer!!.currentPosition)
            end_time_active.text = remainingTime

            if (mediaPlayer!!.isPlaying) {
                val notify = Runnable {
                    progressUpdater()
                }
                Handler().postDelayed(notify, 200)
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
        isVoiceExist = false
        val outFile = File(fileName)
        if (outFile.exists())
            outFile.delete()

        record_voice_dis.visibility = VISIBLE
        record_time_dis.text = this.resources.getString(R.string.no_voice_note)
        record_time_dis.visibility = VISIBLE
        stop_recording_voice_dis.visibility = GONE

        play_btn_active.visibility = GONE
        play_btn_active.setImageResource(android.R.drawable.ic_media_pause)
        delete_btn_active.visibility = GONE
        start_time_active.visibility = GONE
        end_time_active.visibility = GONE
        seekBar_active.visibility = GONE
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

    // TODO: change to string resources
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
        const val EXTRA_REPLY_EDIT = "reply_edit"
    }
}