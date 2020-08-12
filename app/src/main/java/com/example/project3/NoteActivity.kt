package com.example.project3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_note.*
import recyclerviewadapter.NoteListAdapter
import roomdatabase.ExtendedDiary
import roomdatabase.Note
import viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NoteActivity : AppCompatActivity() {

    private val newNoteActivityRequestCode = 1              // для NewWordActivity (requestCode)
    private val clickedActivityRequestCode = 2              // для ClickedActivity (requestCode)
    private val editNoteActivityRequestCode = 3
    private lateinit var mainViewModel: MainViewModel       // добавляем ViewModel

    private var isFabOpen : Boolean = false                 // по умолч. меню закрыто
    private val translationY = 100f

    private val colors: List<String> = listOf("green", "blue", "grass", "purple", "yellow")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        val extDiaryParent = intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        if (!(prefs!!.contains("sorted_notes")))
            prefs.edit().putBoolean("sorted_notes", false).apply()

        val adapter = newNoteAdapter()
        recyclerview_note.adapter = adapter
        recyclerview_note.layoutManager = LinearLayoutManager(this)
        recyclerview_note.addItemDecoration(TopSpacingItemDecoration(20))       // Отступы

        //val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        //itemTouchHelper.attachToRecyclerView(recyclerview1)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainViewModel.allExtendedDiaries.observe(this, Observer {
            val noteList = findListOfNotes(it, extDiaryParent)
            if (prefs.getBoolean("sorted_notes", false))
                adapter.setFavoriteNotes(noteList)
            else
                adapter.setNotes(noteList)
        })

        // Кнопки
        fab_new_note.alpha = 0f
        fab_favourite_note.alpha = 0f
        fab_new_note.translationY = translationY
        fab_favourite_note.translationY = translationY
        // Кнопка вызова меню
        fab_menu_note.setOnClickListener {
            if (!isFabOpen)
                showFabMenu()
            else
                closeFabMenu()
        }
        // обработчик нажатий на 1-ую кнопку
        fab_favourite_note.setOnClickListener {
            closeFabMenu()
            if(prefs.getBoolean("sorted_notes", false))
            {
                prefs.edit().putBoolean("sorted_notes", false).apply()
                adapter.setNotes(findListOfNotes(mainViewModel.allExtendedDiaries.value!!, extDiaryParent))
                recyclerview_note.scrollToPosition(0)
            } else {
                prefs.edit().putBoolean("sorted_notes", true).apply()
                adapter.setFavoriteNotes(findListOfNotes(mainViewModel.allExtendedDiaries.value!!, extDiaryParent))
                recyclerview_note.scrollToPosition(0)
            }
        }
        // обработчик нажатий на 2-ую кнопку (вызывает NewNoteActivity для создания заметки)
        fab_new_note.setOnClickListener {
            closeFabMenu()
            val intent = Intent(this, NewNoteActivity::class.java)
            // 2-ой аргумент это requestCode по которому определяется откуда был запрос
            startActivityForResult(intent, newNoteActivityRequestCode)
        }
    }

    // Поиск нужного списка заметок
    private fun findListOfNotes(extDiaries : List<ExtendedDiary>, extDiaryParent : ExtendedDiary?) : List<Note>
    {
        var noteList = emptyList<Note>()
        for(extDiary in extDiaries) {
            if(extDiary.diary.id == extDiaryParent!!.diary.id) // находим запись с нужным нам id дневника
            {
                noteList = extDiary.notes   // получаем список заметок этого дневника
                break
            }
        }
        return noteList
    }

    // Поскольку передается позиция в списке адаптера, невозможно удалить нужный нам объект,
    // т.к. позиции при поиске не совпадают с позициями в главном списке
    //private val itemTouchHelperCallback=
    //    object :
    //        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
    //        override fun onMove(
    //            recyclerView: RecyclerView,
    //            viewHolder: RecyclerView.ViewHolder,
    //            target: RecyclerView.ViewHolder
    //        ): Boolean {
    //            return false
    //        }
    //
    //        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    //            val extDiaryParent = intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary
    //            var getList = emptyList<Note>()
    //            for(extDiary in mainViewModel.allExtendedDiaries.value!!)
    //            {
    //                if(extDiary.diary.id == extDiaryParent!!.diary.id) // находим запись с нужным нам id дневника
    //                {
    //                    getList = extDiary.notes   // получаем список заметок этого дневника
    //                    break
    //                }
    //            }
    //            val note = getList[viewHolder.adapterPosition]
    //            deleteNote(note)
    //            recyclerview1.adapter!!.notifyDataSetChanged()
    //        }
    //
    //    }

    override fun onResume() {
        super.onResume()
        recyclerview_note.adapter!!.notifyDataSetChanged()
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
            val intent = Intent(this, EditNoteActivity::class.java)
            intent.putExtra("noteSerializableEdit", it)
            startActivityForResult(intent, editNoteActivityRequestCode)
        }, {
            // 4-ый listener, отвечает за избранные
            it.favorite = !it.favorite
            if(it.favorite) {
                Toast.makeText(this, resources.getString(R.string.add_favor),
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, resources.getString(R.string.del_favor),
                    Toast.LENGTH_SHORT).show()
            }
            mainViewModel.updateNote(it)
        })
    }

    // Удаление записи
    private fun deleteNote(note : Note)
    {
        val fileName = this@NoteActivity.getExternalFilesDir(null)!!.absolutePath + "/${note.name}_${note.id}.3gpp"
        if (File(fileName).exists())
            File(fileName).delete()
        mainViewModel.deleteNote(note)
    }

    // Возвращает текущую дату
    @SuppressLint("SimpleDateFormat")
    private fun currentDate() : String
    {
        val pattern = "\t\t\tHH:mm\n\ndd.MM.yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date())
    }

    // TODO: редактировать или убрать за ненадобностью
    // Создает объект Note
    private fun createNote(name : String, content : String, parent_id : Long, date : String,
    img : String?=null, color : String?=null) : Note
    {
        val note = Note(name, content, parent_id, date)
        note.img = img
        note.color = color
        return note
    }

    // функция для обработки результата после вызова startActivityForResult()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val extDiaryParent = intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

        // Результат для добавления заметки
        if (requestCode == newNoteActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            data?.getStringArrayListExtra(NewNoteActivity.EXTRA_NEW_NOTE)?.let {
                // Из data достаем информацию о картинке, была ли она
                // Если картинку не выбрали, установится та, что была на "обложке" дневника
                var imgNote = data.getStringExtra(NewNoteActivity.EXTRA_NEW_NOTE_IMAGE)
                if (imgNote == null || imgNote == "")
                    imgNote = extDiaryParent!!.diary.img
                // С помощью ф-ии создаем объект заметки
                // получаем из экстра данных массив с названием и текстом
                val newNote = createNote(it[0], it[1], extDiaryParent!!.diary.id, currentDate(),
                    img = imgNote, color = colors.random())
                newNote.creationDate = currentDate()
                mainViewModel.insertNote(newNote)
            }
        }
        if ((requestCode == newNoteActivityRequestCode || requestCode == editNoteActivityRequestCode) &&
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
                note.lastEditDate = currentDate()   // обновляем дату
                mainViewModel.updateNote(note)  // обновляем заметку
            }
        }
        // Результат изменения заметки
        if (requestCode == editNoteActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            val noteEdit = data?.getSerializableExtra(EditNoteActivity.EXTRA_EDIT_NOTE) as? Note
            val imgNoteEdit = data?.getStringExtra(EditNoteActivity.EXTRA_IMAGE_EDIT_NOTE)
            if (noteEdit != null)
            {
                // Обновляем дату
                noteEdit.lastEditDate = currentDate()
                if (imgNoteEdit == null || imgNoteEdit == "")
                    noteEdit.img = extDiaryParent!!.diary.img
                else
                    noteEdit.img = imgNoteEdit
                mainViewModel.updateNote(noteEdit)
            }
        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_menu_note, menu)

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        val extDiaryParent = intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

        if (extDiaryParent!!.diary.favorite)
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
                    mainViewModel.allExtendedDiaries.observe(this@NoteActivity, Observer {
                        setNotesForSearch((recyclerview_note.adapter as NoteListAdapter), prefs,
                            it, newText)
                    })
                    setNotesForSearch((recyclerview_note.adapter as NoteListAdapter), prefs,
                        mainViewModel.allExtendedDiaries.value!!, newText)
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    // Функция для вывода поискового запроса (используется только в SearchView)
    private fun setNotesForSearch(adapter : NoteListAdapter, prefs : SharedPreferences?,
                                  extDiaries : List<ExtendedDiary>, newText : String?)
    {
        val extDiaryParent = intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

        val getListNotes = findListOfNotes(extDiaries, extDiaryParent) // Список заметок дневника
        val noteList = mutableListOf<Note>()

        if (newText!!.isNotEmpty()) {
            val search = newText.toLowerCase(Locale.ROOT)

            getListNotes.forEach{notes ->
                if(notes.name.toLowerCase(Locale.ROOT).contains(search))
                    noteList.add(notes)
            }
            if (prefs!!.getBoolean("sorted_notes", false))
                adapter.setFavoriteNotes(noteList)
            else
                adapter.setNotes(noteList)
        } else { // Если строка поиска пуста
            if (prefs!!.getBoolean("sorted_notes", false))
                adapter.setFavoriteNotes(getListNotes)
            else
                adapter.setNotes(getListNotes)
        }
        // Слушатель на кнопку для правильной сортировки
        fab_favourite_note.setOnClickListener {
            closeFabMenu()
            if (prefs.getBoolean("sorted_notes", false)) {
                prefs.edit().putBoolean("sorted_notes", false).apply()
                if (newText.isNotEmpty())
                    (recyclerview_note.adapter as NoteListAdapter).setNotes(noteList)
                else
                    (recyclerview_note.adapter as NoteListAdapter).setNotes(getListNotes)
            } else {
                prefs.edit().putBoolean("sorted_notes", true).apply()
                if (newText.isNotEmpty())
                    (recyclerview_note.adapter as NoteListAdapter).setFavoriteNotes(noteList)
                else
                    (recyclerview_note.adapter as NoteListAdapter).setFavoriteNotes(getListNotes)
            }
        }
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

    // закрывает выдвиг. меню
    private fun closeFabMenu() {
        isFabOpen = !isFabOpen

        fab_menu_note.animate().rotation(0f).setDuration(300).start()

        fab_favourite_note.animate().translationY(translationY).alpha(0f).setDuration(300).start()
        fab_new_note.animate().translationY(translationY).alpha(0f).setDuration(300).start()
    }

    // открывает выдвиг. меню
    private fun showFabMenu() {
        isFabOpen = !isFabOpen

        fab_menu_note.animate().rotation(90f).setDuration(300).start()

        fab_favourite_note.animate().translationY(0f).alpha(1f).setDuration(300).start()
        fab_new_note.animate().translationY(0f).alpha(1f).setDuration(300).start()
    }
}
