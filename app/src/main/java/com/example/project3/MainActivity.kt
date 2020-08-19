package com.example.project3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import recyclerviewadapter.DiaryListAdapter
import roomdatabase.Diary
import roomdatabase.ExtendedDiary
import viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val newDiaryActivityRequestCode = 1              // для NewWordActivity
    private val editDiaryActivityRequestCode = 2             // для EditActivity
    private lateinit var mainViewModel: MainViewModel        // добавляем ViewModel
    private var isFabOpen : Boolean = false                  // по умолч. меню закрыто
    private val colors: List<String> = listOf("green", "blue", "grass", "purple", "yellow")
    private lateinit var extDiaryList : List<ExtendedDiary>

    private val translationY = 100f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        if (!(prefs!!.contains("sorted")))
            prefs.edit().putBoolean("sorted", false).apply()

        // Создаем провайдер, связывая с соотв. классом ViewModel (одинаково для всех ViewModel)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val adapter = newDiaryAdapter()
        // задаем Adapter (одинаково для всех RecyclerView)
        recyclerview.adapter = adapter
        // задаем LinearLayoutManager (одинаково для всех RecyclerView)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.addItemDecoration(TopSpacingItemDecoration(20)) // отступы

        // TODO: Использовать это в Списке Дел
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerview)
        
        // Следит за изменением списка записей(дневников) и обновляет данные в RecyclerView
        mainViewModel.allExtendedDiaries.observe(this, Observer {
            extDiaryList = it
            if (prefs.getBoolean("sorted", false))
                adapter.setFavoriteDiaries(it)
            else
                adapter.setDiaries(it)
        })

        // Кнопки
        // Кнопка вызова выдвиг. меню
        fab_menu.setOnClickListener {
            if (!isFabOpen)
                showFabMenu()
            else
                closeFabMenu()
        }
        // Кнопка для добавления записи
        fab_new_diary.setOnClickListener {
            closeFabMenu()
            val intent = Intent(this, NewDiaryActivity::class.java)
            // 2-ой аргумент это requestCode по которому определяется откуда был запрос
            startActivityForResult(intent, newDiaryActivityRequestCode)
        }
        // Кнопка сортировки по избранным
        fab_favourite.setOnClickListener {
            closeFabMenu()
            if (prefs.getBoolean("sorted", false)) {
                prefs.edit().putBoolean("sorted", false).apply()
                adapter.setDiaries(extDiaryList)
            } else {
                prefs.edit().putBoolean("sorted", true).apply()
                adapter.setFavoriteDiaries(extDiaryList)
            }
            recyclerview.scrollToPosition(0)
        }
        fab_new_diary.alpha = 0f
        fab_favourite.alpha = 0f
        fab_new_diary.translationY = translationY
        fab_favourite.translationY = translationY
    }

    // TODO: Использовать это в Списке Дел
    // Реализует удаление свайпом вправо
    private val itemTouchHelperCallback=
        object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                val objToDelete : ExtendedDiary
                objToDelete = if (prefs!!.getBoolean("sorted", false))
                    extDiaryList.sortedBy{ !it.diary.favorite }[viewHolder.adapterPosition]
                else {
                    extDiaryList[viewHolder.adapterPosition]
                }
                deleteDiary(objToDelete)
            }
        }

    override fun onResume()
    {
        super.onResume()
        // Конкретно сейчас влияет на мгновенное появление цветов записей
        // если изменить опцию в настройках эффект будет мгновенным
        recyclerview.adapter!!.notifyDataSetChanged()
    }

    // Возвращает текущую дату
    @SuppressLint("SimpleDateFormat")
    private fun currentDate() : String
    {
        val pattern = "\t\t\tHH:mm\n\ndd.MM.yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date())
    }

    // Удаляет дневник. Вызов происходит через ViewModel
    private fun deleteDiary(extDiary : ExtendedDiary)
    {
        // Удаляем все файлы с голосовыми заметками из дневника
        for(note in extDiary.notes)
        {
            val fileName = this@MainActivity.getExternalFilesDir(null)!!.absolutePath + "/${note.name}_${note.id}.3gpp"
            if(File(fileName).exists())
                File(fileName).delete()
        }
        // Затем удаляем сам дневник
        mainViewModel.deleteDiary(extDiary.diary)
    }

    // адаптер для RecyclerView
    // то, что в фигурных скобках это и есть аргумент listener : (ExtendedDiary) -> Unit в адаптере
    private fun newDiaryAdapter() : DiaryListAdapter
    {
        return DiaryListAdapter(this,
            {
                // Первый listener, отвечает за удаление дневника
                deleteDiary(it)
            }, {
                // Второй listener. Открывает список заметок (NoteActivity)
                val intent = Intent(this, NoteActivity::class.java)
                intent.putExtra("extDiaryParent", it) // Передаем ExtendedDiary
                startActivity(intent)
            }, {
                // 3-ий listener, отвечает за изменение дневника
                val intent = Intent(this, EditDiaryActivity::class.java)
                intent.putExtra("diaryEdit", it.diary)
                startActivityForResult(intent, editDiaryActivityRequestCode)
            }, {
               // 4-ый listener. Добавление в избранные
                it.diary.favorite = !it.diary.favorite
                if(it.diary.favorite) {
                    Toast.makeText(this, resources.getString(R.string.add_favor),
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, resources.getString(R.string.del_favor),
                        Toast.LENGTH_SHORT).show()
                }
                mainViewModel.updateDiary(it.diary)
            })
    }

    // функция для обработки результата после вызова startActivityForResult()
    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newDiaryActivityRequestCode && resultCode == Activity.RESULT_OK) {
            data?.getStringArrayListExtra(NewDiaryActivity.EXTRA_NEW_DIARY)?.let {
                // получаем из экстра данных нашу строку и создаем объект Word с той же строкой
                val diary = Diary(it[0], it[1], currentDate())
                val diaryImg = data.getStringExtra(NewDiaryActivity.EXTRA_NEW_DIARY_IMAGE)
                if (diaryImg != null && diaryImg != "")
                    diary.img = diaryImg
                else
                    diary.img = null
                diary.color = colors.random()
                diary.creationDate = currentDate()
                mainViewModel.insertDiary(diary) // добавляем запись в БД
            }
        }
        if (requestCode == editDiaryActivityRequestCode && resultCode == Activity.RESULT_OK) {
            val diaryEdit = data?.getSerializableExtra(EditDiaryActivity.EXTRA_EDIT_DIARY) as? Diary
            val imgDiaryEdit = data?.getStringExtra(EditDiaryActivity.EXTRA_EDIT_DIARY_IMAGE)
            if (diaryEdit != null)
            {
                if (imgDiaryEdit != null && imgDiaryEdit != "")
                    diaryEdit.img = imgDiaryEdit
                else
                    diaryEdit.img = null
                diaryEdit.lastEditDate = currentDate()
                mainViewModel.updateDiary(diaryEdit) // обновляем запись в БД
            }
        }
        if ((requestCode == newDiaryActivityRequestCode || requestCode == editDiaryActivityRequestCode) &&
            resultCode == Activity.RESULT_CANCELED)
        {
            Toast.makeText(this, resources.getString(R.string.empty_not_saved),
                Toast.LENGTH_SHORT).show()
        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_menu, menu)

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        val searchItem = menu!!.findItem(R.id.search_view)

        if (searchItem != null)
        {
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
                    mainViewModel.allExtendedDiaries.observe(this@MainActivity, Observer {
                        extDiaryList = it
                        setDiariesForSearch(recyclerview.adapter as DiaryListAdapter, prefs,
                            newText)
                    })
                    setDiariesForSearch(recyclerview.adapter as DiaryListAdapter, prefs, newText)
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    // Функция для вывода поискового запроса (используется только в SearchView)
    private fun setDiariesForSearch(adapter : DiaryListAdapter, prefs : SharedPreferences?, newText : String?)
    {
        // diariesSearchList - список записей, удовлетворяющих поисковому запросу
        val diariesSearchList = mutableListOf<ExtendedDiary>()

        if (newText!!.isNotEmpty())
        {
            val search = newText.toLowerCase(Locale.ROOT)
            mainViewModel.allExtendedDiaries.value!!.forEach{
                if(it.diary.name.toLowerCase(Locale.ROOT).contains(search))
                    diariesSearchList.add(it)
            }
            extDiaryList = diariesSearchList
            if (prefs!!.getBoolean("sorted", false))
                adapter.setFavoriteDiaries(extDiaryList)
            else
                adapter.setDiaries(extDiaryList)
        } else { // Если строка поиска пуста
            extDiaryList = mainViewModel.allExtendedDiaries.value!!
            if (prefs!!.getBoolean("sorted", false))
                adapter.setFavoriteDiaries(extDiaryList)
            else
                adapter.setDiaries(extDiaryList)
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

    // закрывает меню
    private fun closeFabMenu() {
        isFabOpen = !isFabOpen

        // возвращает элементы на исходные позиции
        fab_menu.animate().rotation(0f).setDuration(300).start()


        fab_favourite.animate().translationY(translationY).alpha(0f).setDuration(300).start()
        fab_new_diary.animate().translationY(translationY).alpha(0f).setDuration(300).start()
        Handler().postDelayed({
            fab_new_diary.visibility = GONE
            fab_favourite.visibility = GONE
        }, 300)

    }

    // открывает выдвиг. меню
    private fun showFabMenu() {
        isFabOpen = !isFabOpen

        fab_menu.animate().rotation(90f).setDuration(300).start()

        fab_new_diary.visibility = VISIBLE
        fab_favourite.visibility = VISIBLE
        fab_favourite.animate().translationY(0f).alpha(1f).setDuration(300).start()
        fab_new_diary.animate().translationY(0f).alpha(1f).setDuration(300).start()
    }
}

