package com.example.project3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_note.*
import kotlinx.android.synthetic.main.activity_note.bg_fab_menu
import kotlinx.android.synthetic.main.activity_note.fab
import kotlinx.android.synthetic.main.activity_note.fab2
import recyclerviewadapter.NoteListAdapter
import repository.NotesAndWords
import roomdatabase.Note
import roomdatabase.Word
import viewmodel.NoteViewModel
import viewmodel.TopSpacingItemDecoration
import java.text.SimpleDateFormat
import java.util.*

class NoteActivity : AppCompatActivity() {

    private val newNoteActivityRequestCode = 1              // для NewWordActivity (requestCode)
    private val clickedActivityRequestCode = 2              // для ClickedActivity (requestCode)
    private val editActivityRequestCode = 3
    private lateinit var noteViewModel: NoteViewModel       // добавляем ViewModel
    private val colors: List<String> = listOf("green", "blue", "grass", "purple", "yellow")
    private var isFabOpen : Boolean = false                 // по умолч. меню закрыто

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        val wordSelf = intent.getSerializableExtra("wordSelf") as? Word

        val adapter = newNoteAdapter()
        recyclerview1.adapter = adapter
        recyclerview1.layoutManager = LinearLayoutManager(this)
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        // Отступы
        recyclerview1.addItemDecoration(TopSpacingItemDecoration(20))

        // полученный список заметок передаем в RecyclerView для отображения
        noteViewModel.allNotes.observe(this, Observer {
            var getList = emptyList<Note>()
            // перебираем весь список объектов NotesAndWords
            for(i in it)
            {
                if(i.word.id == wordSelf!!.id) // находим запись с нужным нам id дневника
                {
                    getList = i.notes   // получаем список заметок этого дневника
                    break
                }
            }
            adapter.setNotes(getList)   // передаем полученный список в RecyclerView
        })
        // Кнопки
        // Кнопка вызова меню
        fab.setOnClickListener {
            if (!isFabOpen)
                showFabMenu()
            else
                closeFabMenu()
        }
        // обработчик нажатий на 1-ую кнопку
     //   fab1.setOnClickListener {
     //       closeFabMenu()
     //       Toast.makeText(this, "Message", Toast.LENGTH_SHORT).show()
     //   }
        // обработчик нажатий на 2-ую кнопку (вызывает NewWordActivity для создания заметки)
        fab2.setOnClickListener {
            closeFabMenu()
            val intent = Intent(this, NewNoteActivity::class.java)
            // 2-ой аргумент это requestCode по которому определяется откуда был запрос
            startActivityForResult(intent, newNoteActivityRequestCode)
        }
        // обработчик нажатий на ФОН (когда вызывается фон затемняется)
        bg_fab_menu.setOnClickListener {
            // т.е. если нажать на затемненный фон меню закроется
            closeFabMenu()
        }
    }

    override fun onResume() {
        super.onResume()
        recyclerview1.adapter!!.notifyDataSetChanged()
    }

    private fun newNoteAdapter() : NoteListAdapter
    {
        return NoteListAdapter(this, {
            val intent = Intent(this, ClickedActivity::class.java)
            // передаем необходимые данные в ClickedActivity
            intent.putExtra("noteSerializable", it)
            // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
            startActivityForResult(intent, clickedActivityRequestCode)
        }, {
            // второй listener, нужен для удаления заметки
            deleteNote(it)
        }, {
            val intent = Intent(this, EditActivityNote::class.java)
            intent.putExtra("noteSerializableEdit", it)
            startActivityForResult(intent, editActivityRequestCode)
        })
    }

    // Удаление записи
    private fun deleteNote(note : Note)
    {
        noteViewModel.deleteNote(note)
    }

    // Возвращает текущую дату
    @SuppressLint("SimpleDateFormat")
    private fun currentDate() : String
    {
        val pattern = "\t\t\tHH:mm\n\ndd.MM.yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date())
    }

    // Создает объект Note
    private fun createNote(name : String, text : String, diaryId : Long, date : String,
    imgNote : String?=null, colorNote : String?=null) : Note
    {
        val note = Note(name, text, diaryId, date)
        note.imgNote = imgNote
        note.colorNote = colorNote
        return note
    }

    // функция для обработки результата после вызова startActivityForResult()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val wordSelf = intent.getSerializableExtra("wordSelf") as? Word

        // Результат для добавления заметки
        if (requestCode == newNoteActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            data?.getStringArrayListExtra(NewNoteActivity.EXTRA_REPLY_NOTE)?.let {
                // Из data достаем информацию о картинке, была ли она
                // Если картинку не выбрали, установится та, что была на "обложке" дневника
                val imgNote = data.getStringExtra(NewNoteActivity.EXTRA_IMAGE_NOTE) ?: wordSelf!!.img
                // С помощью ф-ии создаем объект заметки
                // получаем из экстра данных массив с названием и текстом
                val newNote = createNote(it[0], it[1], wordSelf!!.id, currentDate(),
                    imgNote = imgNote, colorNote = colors.random())
                noteViewModel.insertNote(newNote)
            }
        }
        if ((requestCode == newNoteActivityRequestCode || requestCode == editActivityRequestCode) &&
            resultCode == Activity.RESULT_CANCELED)
        {
            Toast.makeText(this, resources.getString(R.string.empty_not_saved_note),
                Toast.LENGTH_SHORT).show()
        }
        // Результат для обновления заметки
        if (requestCode == clickedActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            // получаем с помощью Serializable наш объект класса Note из ClickedActivity
            val note = data?.getSerializableExtra(ClickedActivity.EXTRA_REPLY_EDIT) as? Note
            if (note != null) {
                note.dateNote = currentDate()   // обновляем дату
                noteViewModel.updateNote(note)  // обновляем заметку
            }
        }

        // Результат изменения заметки
        if (requestCode == editActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            val noteEdit = data?.getSerializableExtra(EditActivityNote.EXTRA_EDIT_NOTE) as? Note
            val imgNoteEdit = data?.getStringExtra(EditActivityNote.EXTRA_IMAGE_EDIT_NOTE)
            if (noteEdit != null)
            {
                // Обновляем дату
                noteEdit.dateNote = currentDate()
                if (imgNoteEdit != null && imgNoteEdit != "")
                    noteEdit.imgNote = imgNoteEdit
                noteViewModel.updateNote(noteEdit)
            }
        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_menu_note, menu)

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        val wordSelf = intent.getSerializableExtra("wordSelf") as? Word

        if (wordSelf!!.isFavorite)
        {
            menu!!.findItem(R.id.favorite_view)
                .setIcon(android.R.drawable.btn_star_big_on)
        } else {
            menu!!.findItem(R.id.favorite_view)
                .setIcon(android.R.drawable.btn_star_big_off)
        }
        val searchItem = menu.findItem(R.id.search_view)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    noteViewModel.allNotes.observe(this@NoteActivity, Observer {
                        setNotesForSearch((recyclerview1.adapter as NoteListAdapter), prefs,
                            it, newText)
                    })
                    setNotesForSearch((recyclerview1.adapter as NoteListAdapter), prefs,
                        noteViewModel.allNotes.value!!, newText)
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    // Функция для вывода поискового запроса (используется только в SearchView)
    private fun setNotesForSearch(adapter : NoteListAdapter, prefs : SharedPreferences?,
                                  all_objects_list : List<NotesAndWords>, newText : String?)
    {
        val wordSelf = intent.getSerializableExtra("wordSelf") as? Word

        var getListNotes = emptyList<Note>()
        for (i in all_objects_list) {
            if (i.word.id == wordSelf!!.id) {
                getListNotes = i.notes
                break
            }
        }
        if (newText!!.isNotEmpty()) {
            val noteList = mutableListOf<Note>()
            val search = newText.toLowerCase(Locale.ROOT)

            getListNotes.forEach{notes ->
                if(notes.note.toLowerCase(Locale.ROOT).contains(search))
                    noteList.add(notes)
            }
            adapter.setNotes(noteList)
        }
        else
            adapter.setNotes(getListNotes)
        // Слушатель на кнопку для правильной сортировки
        //fab1.setOnClickListener {
        //    closeFabMenu()
        //    if (prefs!!.getBoolean("sorted", false)) {
        //        prefs.edit().putBoolean("sorted", false).apply()
        //        if (newText.isNotEmpty())
        //            (recyclerview.adapter as WordListAdapter).setWords(wordList1)
        //        else
        //            (recyclerview.adapter as WordListAdapter).setWords(all_words_list)
        //    } else {
        //        prefs.edit().putBoolean("sorted", true).apply()
        //        if (newText.isNotEmpty())
        //            (recyclerview.adapter as WordListAdapter).setFavoriteWords(wordList1)
        //        else
        //            (recyclerview.adapter as WordListAdapter).setFavoriteWords(all_words_list)
        //    }
        //}
    }

    // когда выбираешь элемент меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val wordSelf = intent.getSerializableExtra("wordSelf") as? Word

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
            R.id.favorite_view -> {
                for (words in noteViewModel.allNotes.value!!)
                    if (words.word.id == wordSelf!!.id) {
                        words.word.isFavorite = !words.word.isFavorite
                        if(words.word.isFavorite) {
                            item.setIcon(android.R.drawable.btn_star_big_on)
                            Toast.makeText(this, resources.getString(R.string.add_favor),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else {
                            item.setIcon(android.R.drawable.btn_star_big_off)
                            Toast.makeText(this, resources.getString(R.string.del_favor),
                                Toast.LENGTH_SHORT).show()
                        }
                        noteViewModel.updateWord(words.word)
                        break
                    }
            }
        }
        return super.onOptionsItemSelected(item)
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
        fab.animate().rotation(180f)
        bg_fab_menu.animate().alpha(1f)
        //fab1.animate().translationY(-300f).rotation(0f)
        fab2.animate().translationY(-165f).rotation(0f)
    }
}
