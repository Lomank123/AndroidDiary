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
import com.google.android.material.snackbar.Snackbar
import com.lomank.diary.*
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
    private lateinit var mainNoteList : List<Note>

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_note, container, false)

        val extDiaryParent = requireActivity().intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(activity)
        if (!(prefs!!.contains("sorted_notes")))
            prefs.edit().putBoolean("sorted_notes", false).apply()

        val adapter = newNoteAdapter(layout)
        layout.recyclerview_note.adapter = adapter
        layout.recyclerview_note.layoutManager = LinearLayoutManager(activity)
        layout.recyclerview_note.addItemDecoration(TopSpacingItemDecoration(20))       // Отступы

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        if (mainViewModel.allExtendedDiaries.hasActiveObservers())
            mainViewModel.allExtendedDiaries.removeObservers(requireActivity())
        mainViewModel.allExtendedDiaries.observe(viewLifecycleOwner, Observer { obj ->
            mainNoteList = findListOfNotes(obj, extDiaryParent)
            if (prefs.getBoolean("sorted_notes", false))
                adapter.setNotes(mainNoteList.sortedBy { !it.favorite }, obj)
            else
                adapter.setNotes(mainNoteList, obj)

        })
        // обработчик нажатий на 2-ую кнопку (вызывает NewNoteActivity для создания заметки)
        layout.fab_new_note.setOnClickListener {
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

    private fun newNoteAdapter(view : View) : NoteListAdapter
    {
        return NoteListAdapter(requireActivity(), {
            // listenerOpen
            val intent = Intent(activity, ClickedActivity::class.java)
            // передаем необходимые данные в ClickedActivity
            intent.putExtra("noteSerializable", it)
            // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
            startActivityForResult(intent, clickedActivityRequestCode)
        }, {
            // второй listener, нужен для удаления заметки
            deleteNote(view, it)
        }, {
            // listenerEdit
            val intent = Intent(activity, EditNoteActivity::class.java)
            intent.putExtra("noteSerializableEdit", it)
            startActivityForResult(intent, editNoteActivityRequestCode)
        }, {
            // listenerUpdate
            updateNote(it)
        })
    }

    // Возвращает текущую дату
    @SuppressLint("SimpleDateFormat")
    private fun currentDate() : String
    {
        val pattern = "\t\t\tHH:mm dd.MM.yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date())
    }

    // функция для обработки результата после вызова startActivityForResult()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val extDiaryParent = requireActivity().intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

        // Результат для добавления заметки
        if (requestCode == newNoteActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            data?.getStringArrayListExtra(NewNoteActivity.EXTRA_NEW_NOTE)?.let {
                // Из data достаем информацию о картинке, была ли она
                // Если картинку не выбрали, установится та, что была на "обложке" дневника
                var imgNote = data.getStringExtra(NewNoteActivity.EXTRA_NEW_NOTE_IMAGE)
                if (imgNote == null || imgNote == "")
                    imgNote = extDiaryParent!!.diary.img
                val newNote = Note(it[0], it[1], extDiaryParent!!.diary.id, currentDate())
                newNote.color = data.getIntExtra(NewNoteActivity.EXTRA_NEW_NOTE_COLOR, 0)
                if (newNote.color == 0)
                    newNote.color = null
                newNote.img = imgNote
                newNote.creationDate = currentDate()
                insertNote(newNote)
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
                updateNote(note)  // обновляем заметку
            }
        }
        // Результат изменения заметки
        if (requestCode == editNoteActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            val noteEdit = data?.getSerializableExtra(EditNoteActivity.EXTRA_EDIT_NOTE) as? Note
            val imgNoteEdit = data?.getStringExtra(EditNoteActivity.EXTRA_IMAGE_EDIT_NOTE)
            val colorNoteEdit = data?.getIntExtra(EditNoteActivity.EXTRA_COLOR_EDIT_NOTE, 0)
            if (noteEdit != null)
            {
                noteEdit.lastEditDate = currentDate()
                if (colorNoteEdit != null && colorNoteEdit != 0)
                    noteEdit.color = colorNoteEdit
                if (imgNoteEdit != null)
                    noteEdit.img = imgNoteEdit
                updateNote(noteEdit)
            }
        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actionbar_menu, menu)

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(activity)
        val extDiaryParent = requireActivity().intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary
        val starItem = menu.findItem(R.id.star)
        val searchItem = menu.findItem(R.id.search_view)

        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    // Удаляем лишние обсерверы для плавной анимации
                    if (mainViewModel.allExtendedDiaries.hasActiveObservers())
                        mainViewModel.allExtendedDiaries.removeObservers(activity!!)

                    mainViewModel.allExtendedDiaries.observe(activity!!, Observer {
                        mainNoteList = findListOfNotes(it, extDiaryParent)
                        setNotesForSearch((recyclerview_note.adapter as NoteListAdapter), prefs,
                            it, newText)
                    })
                    setNotesForSearch((recyclerview_note.adapter as NoteListAdapter), prefs,
                        mainViewModel.allExtendedDiaries.value!!, newText)
                    return true
                }
            })
        }

        if(starItem != null){
            if (prefs!!.getBoolean("sorted_notes", false)) {
                starItem.setIcon(android.R.drawable.btn_star_big_on)
            } else {
                starItem.setIcon(android.R.drawable.btn_star_big_off)
            }
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    // когда выбираешь элемент меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(activity)
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
            R.id.star -> {
                val adapter = recyclerview_note.adapter as NoteListAdapter
                if (prefs!!.getBoolean("sorted_notes", false)) {
                    prefs.edit().putBoolean("sorted_notes", false).apply()
                    adapter.setNotes(mainNoteList, mainViewModel.allExtendedDiaries.value!!)
                    item.setIcon(android.R.drawable.btn_star_big_off)
                } else {
                    prefs.edit().putBoolean("sorted_notes", true).apply()
                    adapter.setNotes(mainNoteList.sortedBy { !it.favorite }, mainViewModel.allExtendedDiaries.value!!)
                    item.setIcon(android.R.drawable.btn_star_big_on)
                }
                recyclerview_note.scrollToPosition(0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Функция для вывода поискового запроса (используется только в SearchView)
    private fun setNotesForSearch(adapter : NoteListAdapter, prefs : SharedPreferences?,
                                  extDiaries : List<ExtendedDiary>, newText : String?)
    {
        val extDiaryParent = requireActivity().intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary
        val noteList = mutableListOf<Note>()

        if (newText!!.isNotEmpty()) {
            val search = newText.toLowerCase(Locale.ROOT)

            mainNoteList.forEach{notes ->
                if(notes.name.toLowerCase(Locale.ROOT).contains(search))
                    noteList.add(notes)
            }
            mainNoteList = noteList
            if (prefs!!.getBoolean("sorted_notes", false))
                adapter.setNotes(mainNoteList.sortedBy { !it.favorite }, extDiaries)
            else
                adapter.setNotes(mainNoteList, extDiaries)
        } else { // Если строка поиска пуста
            mainNoteList = findListOfNotes(extDiaries, extDiaryParent)
            if (prefs!!.getBoolean("sorted_notes", false))
                adapter.setNotes(mainNoteList.sortedBy { !it.favorite }, extDiaries)
            else
                adapter.setNotes(mainNoteList, extDiaries)
        }
        recyclerview_note.scrollToPosition(0)
    }

    // SnackBar creation (with UNDO button)
    private fun createUndoSnackBar(view : View, note : Note){
        val snackBar = Snackbar.make(view, resources.getString(R.string.snackbar_note_delete), Snackbar.LENGTH_LONG)
        snackBar.setAnchorView(R.id.fab_new_note)
        var isUndo = false
        snackBar.setAction(resources.getString(R.string.snackbar_undo_btn)){
            insertNote(note)
            isUndo = true
        }
        snackBar.addCallback(object : Snackbar.Callback(){
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if(!isUndo) {
                    // Удаляем все файлы с голосовыми заметками из дневника
                    val fileName = requireActivity().getExternalFilesDir(null)!!.absolutePath + "/${note.name}_${note.id}.3gpp"
                    if (File(fileName).exists())
                        File(fileName).delete()
                }
            }
        })
        snackBar.show()
    }

    // Удаление записи
    private fun deleteNote(view : View, note : Note)
    {
        mainViewModel.deleteNote(note)
        // Creating undo snackBar
        createUndoSnackBar(view, note)
    }
    private fun insertNote(note : Note){
        mainViewModel.insertNote(note)
    }
    private fun updateNote(note: Note){
        mainViewModel.updateNote(note)
    }
}