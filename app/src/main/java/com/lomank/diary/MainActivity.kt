package com.lomank.diary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import other.TopSpacingItemDecoration
import recyclerviewadapter.DiaryListAdapter
import roomdatabase.Diary
import roomdatabase.ExtendedDiary
import viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val newDiaryActivityRequestCode = 1
    private val editDiaryActivityRequestCode = 2

    private lateinit var mainViewModel: MainViewModel
    private lateinit var extDiaryList : List<ExtendedDiary>

    private var flagLastOpened = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(materialToolbar)
        materialToolbar.overflowIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_more_vert_32, null)

        MobileAds.initialize(this)
        adView.loadAd(AdRequest.Builder().build())

        if (!(prefs!!.contains("sorted")))
            prefs.edit().putBoolean("sorted", true).apply()

        // Создаем провайдер, связывая с соотв. классом ViewModel (одинаково для всех ViewModel)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val adapter = newDiaryAdapter()
        recyclerview.adapter = adapter
        recyclerview.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        recyclerview.addItemDecoration(TopSpacingItemDecoration(20)) // отступы

        // Следит за изменением списка записей(дневников) и обновляет данные в RecyclerView
        mainViewModel.allExtendedDiaries.observe(this, { objects ->
            extDiaryList = objects

            if(prefs.getBoolean("open_last_opened_diary", false)) {
                if (flagLastOpened == 0) {
                    val lastOpenedDiaryId = prefs.getLong("lastOpenedDiaryId", (-1).toLong())
                    if (lastOpenedDiaryId != (-1).toLong()) {
                        for (extDiary in extDiaryList) {
                            if (extDiary.diary.id == lastOpenedDiaryId) {
                                flagLastOpened = 1
                                val intent = Intent(this, NoteActivity::class.java)
                                intent.putExtra("extDiaryParent", extDiary) // Передаем ExtendedDiary
                                prefs.edit().putLong("lastOpenedDiaryId", extDiary.diary.id).apply()
                                startActivity(intent)
                                break
                            }
                        }
                    }
                    flagLastOpened = 1
                }
            } else {
                flagLastOpened = 1
            }

            if (prefs.getBoolean("sorted", true))
                adapter.setDiaries(extDiaryList.sortedBy { !it.diary.favorite })
            else
                adapter.setDiaries(extDiaryList)
        })

        // Кнопка для добавления записи
        fab_new_diary.setOnClickListener {
            val intent = Intent(this, NewDiaryActivity::class.java)
            startActivityForResult(intent, newDiaryActivityRequestCode)
        }
    }

    override fun onResume()
    {
        super.onResume()
        // Конкретно сейчас влияет на мгновенное появление цветов записей
        // если изменить опцию в настройках эффект будет мгновенным
        recyclerview.adapter!!.notifyDataSetChanged()
    }


    override fun onPause() {
        super.onPause()

        val list = arrayListOf<Diary>()
        for(diary in extDiaryList) {
            diary.diary.isExpanded = false
            list.add(diary.diary)
        }
        updateListOfDiaries(list)
    }

    // Возвращает текущую дату
    @SuppressLint("SimpleDateFormat")
    private fun currentDate() : String
    {
        val pattern = "\t\t\tHH:mm dd.MM.yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date())
    }

    // адаптер для RecyclerView
    // то, что в фигурных скобках это и есть аргумент listener : (ExtendedDiary) -> Unit в адаптере
    private fun newDiaryAdapter() : DiaryListAdapter
    {
        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)

        return DiaryListAdapter(this,
            {
                // listenerDelete
                deleteDiary(it)
            }, {
                // listenerOpen
                val intent = Intent(this, NoteActivity::class.java)
                intent.putExtra("extDiaryParent", it) // Передаем ExtendedDiary

                prefs!!.edit().putLong("lastOpenedDiaryId", it.diary.id).apply()
                Log.e("err", "opened diary with id = ${it.diary.id}")

                startActivity(intent)
            }, {
                // listenerEdit
                val intent = Intent(this, EditDiaryActivity::class.java)
                intent.putExtra("diaryEdit", it.diary)
                startActivityForResult(intent, editDiaryActivityRequestCode)
            }, {
               // listenerUpdate
                updateDiary(it)
            })
    }

    // функция для обработки результата после вызова startActivityForResult()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newDiaryActivityRequestCode && resultCode == Activity.RESULT_OK) {
            data?.getStringArrayListExtra(NewDiaryActivity.EXTRA_NEW_DIARY)?.let {
                val diary = Diary(it[0])
                val diaryImg = data.getStringExtra(NewDiaryActivity.EXTRA_NEW_DIARY_IMAGE)
                if (diaryImg != null && diaryImg != "")
                    diary.img = diaryImg
                else
                    diary.img = null
                diary.color = data.getIntExtra(NewDiaryActivity.EXTRA_NEW_DIARY_COLOR, 0)
                if(diary.color == 0)
                    diary.color = null
                diary.creationDate = currentDate()
                diary.lastEditDate = currentDate()
                diary.content = it[1]
                mainViewModel.insertDiary(diary) // добавляем запись в БД
            }
        }
        if (requestCode == editDiaryActivityRequestCode && resultCode == Activity.RESULT_OK) {
            val diaryEdit = data?.getSerializableExtra(EditDiaryActivity.EXTRA_EDIT_DIARY) as? Diary
            val imgDiaryEdit = data?.getStringExtra(EditDiaryActivity.EXTRA_EDIT_DIARY_IMAGE)
            val colorDiaryEdit = data?.getIntExtra(EditDiaryActivity.EXTRA_EDIT_DIARY_COLOR, 0)
            if (diaryEdit != null) {
                if (imgDiaryEdit != null)
                    diaryEdit.img = imgDiaryEdit
                if (colorDiaryEdit != null && colorDiaryEdit != 0)
                    diaryEdit.color = colorDiaryEdit
                diaryEdit.lastEditDate = currentDate()
                diaryEdit.isExpanded = false
                mainViewModel.updateDiary(diaryEdit) // обновляем запись в БД
            }
        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        val starItem = menu!!.findItem(R.id.star)
        val searchItem = menu.findItem(R.id.search_view)

        if (searchItem != null) {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    // будет обновлять список когда будут добавляться/удаляться записи
                    // Перед установкой нового Observer предварительно удаляем старый,
                    // чтобы избежать мигания элементов списка
                    if (mainViewModel.allExtendedDiaries.hasActiveObservers())
                        mainViewModel.allExtendedDiaries.removeObservers(this@MainActivity)
                    mainViewModel.allExtendedDiaries.observe(this@MainActivity, {
                        extDiaryList = it
                        setDiariesForSearch(recyclerview.adapter as DiaryListAdapter, prefs,
                            newText)
                    })
                    setDiariesForSearch(recyclerview.adapter as DiaryListAdapter, prefs, newText)
                    return true
                }
            })
        }

        if(starItem != null){
            if (prefs!!.getBoolean("sorted", true)) {
                starItem.setIcon(R.drawable.ic_baseline_star_32)
            } else {
                starItem.setIcon(R.drawable.ic_baseline_star_border_32)
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    // Функция для вывода поискового запроса (используется только в SearchView)
    private fun setDiariesForSearch(adapter : DiaryListAdapter, prefs : SharedPreferences?, newText : String?)
    {
        extDiaryList = if (newText!!.isNotEmpty()) {
            // diariesSearchList - список записей, удовлетворяющих поисковому запросу
            val diariesSearchList = mutableListOf<ExtendedDiary>()
            val search = newText.toLowerCase(Locale.ROOT)
            mainViewModel.allExtendedDiaries.value!!.forEach{
                if(it.diary.name.toLowerCase(Locale.ROOT).contains(search))
                    diariesSearchList.add(it)
            }
            diariesSearchList
        } else { // Если строка поиска пуста
            mainViewModel.allExtendedDiaries.value!!
        }
        if (prefs!!.getBoolean("sorted", true)) {
            adapter.setDiaries(extDiaryList.sortedBy { !it.diary.favorite })

        }
        else
            adapter.setDiaries(extDiaryList)
    }

    // когда выбираешь элемент меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        when(item.itemId){
            R.id.settings -> {
                // открытие окна "Настройки"
                val intentSettings = Intent(this, SettingsHolderActivity::class.java)
                startActivity(intentSettings)
            }
            R.id.about -> {
                // открытие окна "О нас"
                val intentAbout = Intent(this, AboutActivity::class.java)
                startActivity(intentAbout)
            }
            R.id.star -> {
                val adapter = recyclerview.adapter as DiaryListAdapter
                if (prefs!!.getBoolean("sorted", true)) {
                    prefs.edit().putBoolean("sorted", false).apply()
                    adapter.setDiaries(extDiaryList)
                    item.setIcon(R.drawable.ic_baseline_star_border_32)
                } else {
                    prefs.edit().putBoolean("sorted", true).apply()
                    adapter.setDiaries(extDiaryList.sortedBy { !it.diary.favorite })
                    item.setIcon(R.drawable.ic_baseline_star_32)
                    recyclerview.scrollToPosition(0)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // SnackBar creation (with UNDO button)
    private fun createUndoSnackBar(view : View, extDiary : ExtendedDiary){
        val snackBar = Snackbar.make(view, resources.getString(R.string.snackbar_diary_delete), Snackbar.LENGTH_LONG)
        snackBar.setAnchorView(R.id.fab_new_diary)
        var isUndo = false
        snackBar.setAction(resources.getString(R.string.snackbar_undo_btn)){
            insertDiary(extDiary.diary)
            mainViewModel.insertListNote(extDiary.notes)
            mainViewModel.insertListItems(extDiary.dailyListItems)
            isUndo = true
        }
        snackBar.addCallback(object : Snackbar.Callback(){
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                if(!isUndo) {
                    // Удаляем все файлы с голосовыми заметками из дневника
                    for(note in extDiary.notes) {
                        val fileName = this@MainActivity.getExternalFilesDir(null)!!.absolutePath + "/voice_note_${note.id}.3gpp"
                        if(File(fileName).exists())
                            File(fileName).delete()
                    }
                }
            }
        })
        snackBar.show()
    }

    // Database queries

    // Delete diary
    private fun deleteDiary(extDiary : ExtendedDiary)
    {
        mainViewModel.deleteDiary(extDiary.diary)
        // SnackBar undo
        createUndoSnackBar(findViewById(android.R.id.content), extDiary)
    }

    private fun insertDiary(diary : Diary){
        mainViewModel.insertDiary(diary)
    }
    private fun updateDiary(extDiary: ExtendedDiary){
        mainViewModel.updateDiary(extDiary.diary)
    }
    private fun updateListOfDiaries(list : List<Diary>){
        mainViewModel.updateListOfDiaries(list)
    }
}

