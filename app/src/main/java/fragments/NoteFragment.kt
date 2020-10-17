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
import other.TopSpacingItemDecoration
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
    private val photoActivityRequestCode = 333

    private var photoPosition = 0

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
            if (prefs!!.getBoolean("sorted_notes", true))
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

    override fun onPause() {
        super.onPause()

        val list = arrayListOf<Note>()
        for(note in mainNoteList) {
            note.isExpanded = false
            list.add(note)
        }
        updateListOfNotes(list)
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
        }, {
            val imageIntent = Intent(activity, PhotoViewerActivity::class.java)
            imageIntent.putExtra("currentPos", photoPosition)
            imageIntent.putExtra("images", it)
            startActivityForResult(imageIntent, photoActivityRequestCode)
        }, {
            photoPosition = it
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
            note.isExpanded = false
            updateNote(note)                    // обновляем заметку
        }
        if(requestCode == openNoteRequestCode && resultCode == Activity.RESULT_CANCELED){
            val note = data?.getSerializableExtra(ClickedActivity.EXTRA_REPLY_CANCELED) as? Note
            if(note != null) {
                note.isExpanded = false
                updateNote(note)
            }
        }
        if(requestCode == photoActivityRequestCode && resultCode == Activity.RESULT_OK){
            val newNote = data?.getSerializableExtra(PhotoViewerActivity.EXTRA_REPLY_PHOTO_VIEWER) as Note
            updateNote(newNote)

        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)

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
            if (prefs!!.getBoolean("sorted_notes", true)) {
                starItem.setIcon(R.drawable.ic_baseline_star_32)
            } else {
                starItem.setIcon(R.drawable.ic_baseline_star_border_32)
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
                if (prefs!!.getBoolean("sorted_notes", true)) {
                    prefs.edit().putBoolean("sorted_notes", false).apply()
                    adapter.setNotes(mainNoteList, mainViewModel.allExtendedDiaries.value!!)
                    item.setIcon(R.drawable.ic_baseline_star_border_32)
                } else {
                    prefs.edit().putBoolean("sorted_notes", true).apply()
                    adapter.setNotes(mainNoteList.sortedBy { !it.favorite }, mainViewModel.allExtendedDiaries.value!!)
                    item.setIcon(R.drawable.ic_baseline_star_32)
                    recyclerview_note.scrollToPosition(0)
                }
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
        if (prefs!!.getBoolean("sorted_notes", true)) {
            adapter.setNotes(mainNoteList.sortedBy { !it.favorite }, extDiaries)

        }
        else
            adapter.setNotes(mainNoteList, extDiaries)
    }

    // SnackBar creation (with UNDO button)
    private fun createUndoSnackBar(view : View, note : Note, fileName : String){
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
        val fileName = requireActivity().getExternalFilesDir(null)!!.absolutePath + "/voice_note_${note.id}.3gpp"
        createUndoSnackBar(view, note, fileName)
    }
    private fun insertNote(note : Note){
        mainViewModel.insertNote(note)
    }
    private fun updateNote(note: Note){
        mainViewModel.updateNote(note)
    }
    private fun updateListOfNotes(list : List<Note>){
        mainViewModel.updateListOfNotes(list)
    }
}