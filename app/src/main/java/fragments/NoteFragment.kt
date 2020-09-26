package fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
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

    private val newNoteRequestCode = 111
    private val openNoteRequestCode = 222

    private lateinit var mainViewModel: MainViewModel       // добавляем ViewModel
    private lateinit var mainNoteList : List<Note>

    private lateinit var extDiaryParent : ExtendedDiary

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_note, container, false)

        extDiaryParent = requireActivity().intent.getSerializableExtra("extDiaryParent") as ExtendedDiary

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
        mainViewModel.allExtendedDiaries.observe(viewLifecycleOwner, { obj->
            // getting list of notes
            mainNoteList = findListOfNotes(obj, extDiaryParent)

            // renaming voice note filename of a new note
            if(mainNoteList.lastOrNull() != null) {
                val oldFilename = requireActivity().getExternalFilesDir(null)!!.absolutePath + "/voice_note_empty.3gpp"
                val newFilename = requireActivity().getExternalFilesDir(null)!!.absolutePath + "/voice_note_${mainNoteList.last().id}.3gpp"
                if(File(oldFilename).exists()) {
                    File(oldFilename).renameTo(File(newFilename))
                }
            }

            // setting notes in the adapter
            if (prefs.getBoolean("sorted_notes", false))
                adapter.setNotes(mainNoteList.sortedBy { !it.favorite }, obj)
            else
                adapter.setNotes(mainNoteList, obj)
        })

        // FAB new note
        layout.fab_new_note.setOnClickListener {

            val intent = Intent(activity, ClickedActivity::class.java)
            intent.putExtra("requestCode", newNoteRequestCode)
            intent.putExtra("diaryParent", extDiaryParent)

            // 2-ой аргумент это requestCode по которому определяется откуда был запрос
            startActivityForResult(intent, newNoteRequestCode)
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
            intent.putExtra("openNote", it)
            intent.putExtra("requestCode", openNoteRequestCode)
            intent.putExtra("diaryParent", extDiaryParent)
            startActivityForResult(intent, openNoteRequestCode)
        }, {
            // listenerDelete
            deleteNote(view, it)
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

        // Результат для добавления заметки
        if (requestCode == newNoteRequestCode && resultCode == Activity.RESULT_OK) {
            val newNote = data?.getSerializableExtra(ClickedActivity.EXTRA_REPLY_EDIT) as Note
            newNote.lastEditDate = currentDate()
            newNote.creationDate = currentDate()
            insertNote(newNote)
        }
        // Результат для обновления заметки
        if (requestCode == openNoteRequestCode && resultCode == Activity.RESULT_OK) {
            val note = data?.getSerializableExtra(ClickedActivity.EXTRA_REPLY_EDIT) as Note
            note.lastEditDate = currentDate()   // обновляем дату
            updateNote(note)                    // обновляем заметку
        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actionbar_menu, menu)

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(activity)
        val starItem = menu.findItem(R.id.star)
        val searchItem = menu.findItem(R.id.search_view)

        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (mainViewModel.allExtendedDiaries.hasActiveObservers())
                        mainViewModel.allExtendedDiaries.removeObservers(activity!!)

                    mainViewModel.allExtendedDiaries.observe(activity!!, {
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
            }
            R.id.about -> {
                // открытие окна "О нас"
                val aboutIntent = Intent(activity, AboutActivity::class.java)
                startActivity(aboutIntent)
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
        mainNoteList = if (newText!!.isNotEmpty()) {
            val noteList = mutableListOf<Note>()
            val search = newText.toLowerCase(Locale.ROOT)
            mainNoteList.forEach{notes ->
                if(notes.name.toLowerCase(Locale.ROOT).contains(search))
                    noteList.add(notes)
            }
            noteList
        } else { // Если строка поиска пуста
            findListOfNotes(extDiaries, extDiaryParent)
        }
        if (prefs!!.getBoolean("sorted_notes", false)) {
            adapter.setNotes(mainNoteList.sortedBy { !it.favorite }, extDiaries)
            recyclerview_note.scrollToPosition(0)
        }
        else
            adapter.setNotes(mainNoteList, extDiaries)
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
                    val fileName = requireActivity().getExternalFilesDir(null)!!.absolutePath + "/voice_note_${note.id}.3gpp"
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