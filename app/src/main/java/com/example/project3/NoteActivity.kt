package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
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

class NoteActivity : AppCompatActivity() {

    private val newNoteActivityRequestCode = 1              // для NewWordActivity (requestCode)
    private val clickedActivityRequestCode = 2              // для ClickedActivity (requestCode)
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
            intent.putExtra("note_idNote", it.idNote)
            intent.putExtra("note_name", it.note)
            intent.putExtra("note_text", it.text)

            // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
            startActivityForResult(intent, clickedActivityRequestCode)
        }, {
            // второй listener, нужен для удаления заметки
            deleteNote(it)
        })

        // Аналогично, как и в MainActivity
        recyclerview1.adapter = adapter
        recyclerview1.layoutManager = LinearLayoutManager(this)
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        // чтобы получить список заметок выбранного дневника

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

        // обработчик нажатий на 1-ую кнопку (пока ничего не делает)
        fab1.setOnClickListener {
            closeFabMenu()
            Toast.makeText(this, "Message", Toast.LENGTH_SHORT).show()
        }

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
        fab1.animate().translationY(0f).rotation(90f)
        fab2.animate().translationY(0f).rotation(90f)

        // ставит задержку на исчезновение элементов меню (250 мс)
        Handler().postDelayed({fab1.visibility = GONE}, 250)
        Handler().postDelayed({fab2.visibility = GONE}, 250)
        Handler().postDelayed({bg_fab_menu.visibility = GONE}, 250)
    }

    // открывает выдвиг. меню
    private fun showFabMenu() {
        isFabOpen = true

        // показывает элементы
        fab1.visibility = VISIBLE
        fab2.visibility = VISIBLE
        bg_fab_menu.visibility = VISIBLE

        // "выдвигает" элементы
        fab.animate().rotation(135f)
        bg_fab_menu.animate().alpha(1f)
        fab1.animate().translationY(-300f).rotation(0f)
        fab2.animate().translationY(-165f).rotation(0f)
    }

    private fun deleteNote(note : Note) // удаление записи
    {
        noteViewModel.deleteNote(note)
    }

    // функция для обработки результата после вызова startActivityForResult()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Результат для добавления заметки
        if (requestCode == newNoteActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            data?.getStringArrayListExtra(NewNoteActivity.EXTRA_REPLY_NOTE)?.let {

                // получаем из экстра данных массив с названием и текстом
                // и создаем объект Note с этими данными, причем устанавливаем diaryId такой же
                // как и id у дневника, из которого происходил вызов
                val note = Note(it[0], it[1], intent.getLongExtra("word_id", -1))
                noteViewModel.insertNote(note) // добавляем запись в БД
            }
        }
        if (requestCode == newNoteActivityRequestCode && resultCode == Activity.RESULT_CANCELED)
        {
            Toast.makeText(this, "Canceled or note name is empty",
                Toast.LENGTH_SHORT).show()
        }

        // Результат для обновления заметки
        if (requestCode == clickedActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            data?.getStringArrayListExtra(ClickedActivity.EXTRA_REPLY_EDIT)?.let {
                // получаем массив с новыми данными, создаем объект
                // 0-ой элемент - название, 1-ый - текст заметки
                // 3-ий параметр - id дневника, к которому заметка привязана
                val note = Note(it[0], it[1], intent.getLongExtra("word_id", -1))
                // устанавливаем первичный ключ как у заметки, в которой что-то меняли
                // чтобы корректно обновить данные
                note.idNote = data.getLongExtra("noteId", -1)
                noteViewModel.updateNote(note) // обновляем заметку
            }
        }
        if(requestCode == clickedActivityRequestCode && resultCode == Activity.RESULT_CANCELED)
        {
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
                return super.onOptionsItemSelected(item)
            }
            R.id.about -> {
                // открытие окна "О нас"
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }


}
