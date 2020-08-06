package com.example.project3

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.SeekBar
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_clicked.*
import roomdatabase.Note
import java.io.File
import java.lang.Exception

class ClickedActivity : AppCompatActivity() {

    private var isVoice = false
    private var isVoiceExist = false

    private var mediaRecorder : MediaRecorder? = MediaRecorder()    // Запись
    private var mediaPlayer : MediaPlayer? = MediaPlayer()          // Воспроизведение
    private var fileName : String = ""                              // Имя файла

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked)

        val note = intent.getSerializableExtra("noteSerializable") as? Note

        fileName = this.getExternalFilesDir(null)!!.absolutePath + "/${note!!.note}_${note.idNote}.3gpp"

        // получаем экстра данные из NoteActivity
        textView1.text = note.note
        editText1.setText(note.text)

        // Если голосовая заметка найдена
            if(File(fileName).exists())
            {
                isVoiceExist = true
                record_voice_dis.visibility = GONE
                record_time_dis.visibility = GONE
                stop_recording_voice_dis.visibility = GONE

                play_btn_active.visibility = VISIBLE
                delete_btn_active.visibility = VISIBLE

                // seekBar
                seekBar_active.visibility = VISIBLE

                playStart()

                seekBar_active.max = mediaPlayer!!.duration

                seekBar_active.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                mediaPlayer!!.seekTo(progress)
                            }
                        }
                        override fun onStartTrackingTouch(p0: SeekBar?) {
                        }
                        override fun onStopTrackingTouch(p0: SeekBar?) {
                        }
                    }
                )
            }

        play_btn_active.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                pausePlay()
                //play_btn_active.setImageResource(android.R.drawable.ic_media_play)
            } else {
                resumePlay()
                //play_btn_active.setImageResource(android.R.drawable.ic_media_pause)
                progressUpdater()
            }
        }

        delete_btn_active.setOnClickListener {
            recordDelete()
        }

        // слушатель на кнопку начала записи голоса
        record_voice_dis.setOnClickListener {

            record_voice_dis.visibility = GONE
            stop_recording_voice_dis.visibility = VISIBLE

            recordStart()

            // слушатель на кнопку остановки записи голос. заметки
            stop_recording_voice_dis.setOnClickListener {

                record_time_dis.visibility = GONE
                stop_recording_voice_dis.visibility = GONE

                play_btn_active.visibility = VISIBLE
                delete_btn_active.visibility = VISIBLE

                recordStop()
                isVoiceExist = true
                playStart()

                seekBar_active.visibility = VISIBLE
                seekBar_active.max = mediaPlayer!!.duration
                seekBar_active.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                mediaPlayer!!.seekTo(progress)
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
                        //play_btn_active.setImageResource(android.R.drawable.ic_media_play)
                    }
                    else {
                        resumePlay()
                        //play_btn_active.setImageResource(android.R.drawable.ic_media_pause)
                        progressUpdater()
                    }
                }

                delete_btn_active.setOnClickListener{
                    recordDelete()
                }
            }
        }
    }

    override fun onResume()
    {
        super.onResume()
        val note = intent.getSerializableExtra("noteSerializable") as? Note

        // получаем наши настройки
        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)

        if (prefs!!.getBoolean("img_check", false))
        {
            if (note!!.imgNote != null)
            {
                val uriImage = Uri.parse(note.imgNote)
                imageView_clicked.setImageURI(uriImage)
            }
        }

        when(prefs.getString("list_preference_1", "0"))
        {
            "Default" ->
            {
                textView1.typeface = Typeface.DEFAULT
                editText1.typeface = Typeface.DEFAULT
            }
            "Serif" ->
            {
                textView1.typeface = Typeface.SERIF
                editText1.typeface = Typeface.SERIF
            }
            "Sans Serif" ->
            {
                textView1.typeface = Typeface.SANS_SERIF
                editText1.typeface = Typeface.SANS_SERIF
            }
            "Default Bald" ->
            {
                textView1.typeface = Typeface.DEFAULT_BOLD
                editText1.typeface = Typeface.DEFAULT_BOLD
            }
            "Monospace" ->
            {
                textView1.typeface = Typeface.MONOSPACE
                editText1.typeface = Typeface.MONOSPACE
            }
        }

        seekBar_active.progress = 0
        playStart()
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // когда выбираешь элемент меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.save_btn_edit -> { // Кнопка Save
                val note = intent.getSerializableExtra("noteSerializable") as? Note
                val replyIntent = Intent()
                // обновляем введенный текст
                note!!.text = editText1.text.toString()
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
                Toast.makeText(this, resources.getString(R.string.saved), Toast.LENGTH_SHORT).show()
                finish()
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
                setResult(Activity.RESULT_CANCELED)
                Toast.makeText(this, resources.getString(R.string.canceled), Toast.LENGTH_SHORT).show()
                finish()
            }
            R.id.voice_btn_edit -> {
                isVoice = !isVoice
                if (isVoice)
                    layout_voice.visibility = VISIBLE
                else
                    layout_voice.visibility = GONE
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun progressUpdater()
    {
        if (mediaPlayer != null) {
            seekBar_active.progress = mediaPlayer!!.currentPosition

            if (mediaPlayer!!.isPlaying) {
                val notif = Runnable {
                    progressUpdater()
                }
                Handler().postDelayed(notif, 500)
            }
        }
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
        record_time_dis.visibility = VISIBLE
        stop_recording_voice_dis.visibility = GONE

        play_btn_active.visibility = GONE
        delete_btn_active.visibility = GONE
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

    // тег для распознавания именно этого запроса
    companion object {
        const val EXTRA_REPLY_EDIT = "reply_edit"
    }
}