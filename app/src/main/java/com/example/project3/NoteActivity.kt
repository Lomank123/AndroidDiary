package com.example.project3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View.*
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_note.*
import recyclerviewadapter.NoteListAdapter
import roomdatabase.Note
import viewmodel.NoteViewModel
import viewmodel.TopSpacingItemDecoration
import java.text.SimpleDateFormat
import java.util.*

class NoteActivity : AppCompatActivity() {

    private val newNoteActivityRequestCode = 1              // для NewWordActivity (requestCode)
    private val clickedActivityRequestCode = 2              // для ClickedActivity (requestCode)
    private val editActivityRequestCode = 3
    private lateinit var noteViewModel: NoteViewModel       // добавляем ViewModel


    private var isFabOpen : Boolean = false                 // по умолч. меню закрыто

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        // id дневника
        val wordId = intent.getLongExtra("word_id", -1)

        // адаптер для NoteActivity, при нажатии на элемент будет вызывать ClickedActivity
        val adapter = NoteListAdapter(this, {

            val intent = Intent(this, ClickedActivity::class.java)

            // передаем необходимые данные в ClickedActivity
            intent.putExtra("noteSerializable", it)

            // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
            startActivityForResult(intent, clickedActivityRequestCode)
        }, {
            // второй listener, нужен для удаления заметки
            deleteNote(it)
        } , {

            val intent = Intent(this, EditActivityNote::class.java)
            intent.putExtra("noteSerializableEdit", it)
            startActivityForResult(intent, editActivityRequestCode)

        })

        // Аналогично, как и в MainActivity
        recyclerview1.adapter = adapter
        recyclerview1.layoutManager = LinearLayoutManager(this)
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        //любимые отступы
        val topSpacingDecoration = TopSpacingItemDecoration(20)
        recyclerview1.addItemDecoration(topSpacingDecoration)

        // полученный список заметок передаем в RecyclerView для отображения
        noteViewModel.allNotes.observe(this, Observer {
            var getList = emptyList<Note>()

            // перебираем весь список объектов NotesAndWords
            for(i in it)
            {
                if(i.word.id == wordId) // находим запись с нужным нам id дневника
                {
                    getList = i.notes   // получаем список заметок этого дневника
                    break
                }
            }
            adapter.setNotes(getList)   // передаем полученный список в RecyclerView
        })

        // обработчик нажатий на кнопку вызова popupMenu
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            if (!isFabOpen)
                showFabMenu()
            else
                closeFabMenu()
        }

        // здесь находится fab1 (floating action button, в целях показа ее убрали)
        // обработчик нажатий на 1-ую кнопку
        //fab1.setOnClickListener {
        //    closeFabMenu()
        //    Toast.makeText(this, "Message", Toast.LENGTH_SHORT).show()
        //}

        // обработчик нажатий на 2-ую кнопку (вызывает NewWordActivity для создания заметки)
        fab2.setOnClickListener {
            closeFabMenu()
            val intent = Intent(this, NewNoteActivity::class.java)
            // 2-ой аргумент это requestCode по которому определяется откуда был запрос
            startActivityForResult(intent, newNoteActivityRequestCode)
        }
        // обработчик нажатий на ФОН (когда вызывается popupMenu фон затемняется)
        bg_fab_menu.setOnClickListener {
            // т.е. если нажать на затемненный фон меню закроется
            closeFabMenu()
        }
    }

    // закрывает выдвиг. меню
    private fun closeFabMenu() {
        isFabOpen = false

        // возвращает элементы на исходные позиции
        fab.animate().rotation(0f)
        bg_fab_menu.animate().alpha(0f)
        //fab1.animate().translationY(0f).rotation(90f)
        fab2.animate().translationY(0f).rotation(90f)

        // ставит задержку на исчезновение элементов меню (250 мс)
        //Handler().postDelayed({fab1.visibility = GONE}, 250)
        Handler().postDelayed({fab2.visibility = GONE}, 250)
        Handler().postDelayed({bg_fab_menu.visibility = GONE}, 250)
    }

    // открывает выдвиг. меню
    private fun showFabMenu() {
        isFabOpen = true

        // показывает элементы
        //fab1.visibility = VISIBLE
        fab2.visibility = VISIBLE
        bg_fab_menu.visibility = VISIBLE

        // "выдвигает" элементы
        fab.animate().rotation(135f)
        bg_fab_menu.animate().alpha(1f)
        //fab1.animate().translationY(-300f).rotation(0f)
        fab2.animate().translationY(-165f).rotation(0f)
    }

    private fun deleteNote(note : Note) // удаление записи
    {
        noteViewModel.deleteNote(note)
    }

    // функция для обработки результата после вызова startActivityForResult()
    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Результат для добавления заметки
        if (requestCode == newNoteActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            data?.getStringArrayListExtra(NewNoteActivity.EXTRA_REPLY_NOTE)?.let {

                // получаем из экстра данных массив с названием и текстом
                // и создаем объект Note с этими данными, причем устанавливаем diaryId такой же
                // как и id у дневника, из которого происходил вызов

                // получаем текущую дату и вставляем в объект дневника
                val pattern = "\t\t\tHH:mm\n\ndd.MM.yyyy"
                val simpleDateFormat =
                    SimpleDateFormat(pattern)
                val currentDate = simpleDateFormat.format(Date())

                val noteImg = data.getStringExtra(NewNoteActivity.EXTRA_IMAGE_NOTE)

                if(noteImg != null)
                {
                    val note = Note(it[0], it[1], intent.getLongExtra("word_id",
                        -1), currentDate)
                    note.imgNote = noteImg
                    noteViewModel.insertNote(note)
                }
                else
                {
                    val note = Note(it[0], it[1], intent.getLongExtra("word_id",
                        -1), currentDate)
                    note.imgNote = intent.getStringExtra("word_img")
                    noteViewModel.insertNote(note)
                }
            }
        }
        if ((requestCode == newNoteActivityRequestCode || requestCode == editActivityRequestCode) &&
            resultCode == Activity.RESULT_CANCELED)
        {
            Toast.makeText(this, "Canceled or diary name is empty",
                Toast.LENGTH_SHORT).show()
        }

        // Результат для обновления заметки
        if (requestCode == clickedActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            // получаем с помощью Serializable наш объект класса Note из ClickedActivity
            val note = data?.getSerializableExtra(ClickedActivity.EXTRA_REPLY_EDIT) as? Note
            if (note != null)
            {
                noteViewModel.updateNote(note)  // обновляем заметку
            }
        }

        // Результат изменения заметки
        if (requestCode == editActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            val noteEdit = data?.getSerializableExtra(EditActivityNote.EXTRA_EDIT_NOTE) as? Note
            val imgNoteEdit = data?.getStringExtra(EditActivityNote.EXTRA_IMAGE_EDIT_NOTE)
            if (imgNoteEdit != null && imgNoteEdit != "")
                noteEdit!!.imgNote = imgNoteEdit
            if (noteEdit != null)
                noteViewModel.updateNote(noteEdit)
        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // когда выбираешь элемент меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.settings -> {
                // открытие окна "Настройки"
                val intentSettings = Intent(this, SettingsHolderActivity::class.java)
                startActivity(intentSettings)
                return super.onOptionsItemSelected(item)
            }
            R.id.about -> {
                // открытие окна "О нас"
                val aboutIntent = Intent(this, AboutActivity::class.java)
                startActivity(aboutIntent)
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}