package fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project3.*
import kotlinx.android.synthetic.main.fragment_note.*
import kotlinx.android.synthetic.main.fragment_note.view.*
import recyclerviewadapter.NoteListAdapter
import roomdatabase.ExtendedDiary
import roomdatabase.Note
import viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NoteFragment : Fragment() {

    private val newNoteActivityRequestCode = 1              // для NewWordActivity (requestCode)
    private val clickedActivityRequestCode = 2              // для ClickedActivity (requestCode)
    private val editNoteActivityRequestCode = 3
    private lateinit var mainViewModel: MainViewModel       // добавляем ViewModel

    private var isFabOpen : Boolean = false                 // по умолч. меню закрыто
    private val translationY = 100f

    private val colors: List<String> = listOf("green", "blue", "grass", "purple", "yellow")

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_note, container, false)

        val extDiaryParent = activity!!.intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(activity)
        if (!(prefs!!.contains("sorted_notes")))
            prefs.edit().putBoolean("sorted_notes", false).apply()

        val adapter = newNoteAdapter()
        layout.recyclerview_note.adapter = adapter
        layout.recyclerview_note.layoutManager = LinearLayoutManager(activity)
        layout.recyclerview_note.addItemDecoration(TopSpacingItemDecoration(20))       // Отступы

        //val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        //itemTouchHelper.attachToRecyclerView(recyclerview1)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        mainViewModel.allExtendedDiaries.observe(viewLifecycleOwner, Observer {
            val noteList = findListOfNotes(it, extDiaryParent)
            if (prefs.getBoolean("sorted_notes", false))
                adapter.setFavoriteNotes(noteList)
            else
                adapter.setNotes(noteList)
        })

        // Кнопки
        layout.fab_new_note.alpha = 0f
        layout.fab_favourite_note.alpha = 0f
        layout.fab_new_note.translationY = translationY
        layout.fab_favourite_note.translationY = translationY
        // Кнопка вызова меню
        layout.fab_menu_note.setOnClickListener {
            if (!isFabOpen)
                showFabMenu()
            else
                closeFabMenu()
        }
        // обработчик нажатий на 1-ую кнопку
        layout.fab_favourite_note.setOnClickListener {
            closeFabMenu()
            if(prefs.getBoolean("sorted_notes", false))
            {
                prefs.edit().putBoolean("sorted_notes", false).apply()
                adapter.setNotes(findListOfNotes(mainViewModel.allExtendedDiaries.value!!, extDiaryParent))
            } else {
                prefs.edit().putBoolean("sorted_notes", true).apply()
                adapter.setFavoriteNotes(findListOfNotes(mainViewModel.allExtendedDiaries.value!!, extDiaryParent))
            }
            layout.recyclerview_note.scrollToPosition(0)
        }
        // обработчик нажатий на 2-ую кнопку (вызывает NewNoteActivity для создания заметки)
        layout.fab_new_note.setOnClickListener {
            closeFabMenu()
            val intent = Intent(activity, NewNoteActivity::class.java)
            // 2-ой аргумент это requestCode по которому определяется откуда был запрос
            startActivityForResult(intent, newNoteActivityRequestCode)
        }
        return layout
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

    override fun onResume() {
        super.onResume()
        recyclerview_note.adapter!!.notifyDataSetChanged()
    }

    private fun newNoteAdapter() : NoteListAdapter
    {
        return NoteListAdapter(activity!!, {
            val intent = Intent(activity, ClickedActivity::class.java)
            // передаем необходимые данные в ClickedActivity
            intent.putExtra("noteSerializable", it)
            // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
            startActivityForResult(intent, clickedActivityRequestCode)
        }, {
            // второй listener, нужен для удаления заметки
            deleteNote(it)
        }, {
            val intent = Intent(activity, EditNoteActivity::class.java)
            intent.putExtra("noteSerializableEdit", it)
            startActivityForResult(intent, editNoteActivityRequestCode)
        }, {
            // 4-ый listener, отвечает за избранные
            it.favorite = !it.favorite
            if(it.favorite) {
                Toast.makeText(activity, resources.getString(R.string.add_favor),
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, resources.getString(R.string.del_favor),
                    Toast.LENGTH_SHORT).show()
            }
            mainViewModel.updateNote(it)
        })
    }

    // Удаление записи
    private fun deleteNote(note : Note)
    {
        // TODO: проверить удаление голосовой заметки!!!
        val fileName = activity!!.getExternalFilesDir(null)!!.absolutePath + "/${note.name}_${note.id}.3gpp"
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

        val extDiaryParent = activity!!.intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

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
            Toast.makeText(activity, resources.getString(R.string.empty_not_saved_note),
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
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actionbar_menu_note, menu)

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(activity)
        //val extDiaryParent = activity!!.intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

        val searchItem = menu.findItem(R.id.search_view)
        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    mainViewModel.allExtendedDiaries.observe(activity!!, Observer {
                        setNotesForSearch((recyclerview_note.adapter as NoteListAdapter), prefs,
                            it, newText)
                    })
                    setNotesForSearch((recyclerview_note.adapter as NoteListAdapter), prefs,
                        mainViewModel.allExtendedDiaries.value!!, newText)
                    return true
                }
            })
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    // когда выбираешь элемент меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.settings -> {
                // открытие окна "Настройки"
                val intentSettings = Intent(activity, SettingsHolderActivity::class.java)
                startActivity(intentSettings)
                return super.onOptionsItemSelected(item)
            }
            R.id.about -> {
                // открытие окна "О нас"
                val aboutIntent = Intent(activity, AboutActivity::class.java)
                startActivity(aboutIntent)
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Функция для вывода поискового запроса (используется только в SearchView)
    private fun setNotesForSearch(adapter : NoteListAdapter, prefs : SharedPreferences?,
                                  extDiaries : List<ExtendedDiary>, newText : String?)
    {
        val extDiaryParent = activity!!.intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

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
            recyclerview_note.scrollToPosition(0)
        }
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