package com.example.project3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import recyclerviewadapter.DiaryListAdapter
import roomdatabase.ExtendedDiary
import roomdatabase.Diary
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
        // добавляет в декорацию элемента дневника и заметки этот отступ
        recyclerview.addItemDecoration(TopSpacingItemDecoration(20))

        // Следит за изменением списка записей(дневников) и обновляет данные в RecyclerView
        mainViewModel.allExtendedDiaries.observe(this, Observer {
            if (prefs.getBoolean("sorted", false))
                adapter.setFavoriteDiaries(it)
            else
                adapter.setDiaries(it)
        })
        // Кнопки
        // Кнопка вызова выдвиг. меню
        fab.setOnClickListener {
            if (!isFabOpen)
                showFabMenu()
            else
                closeFabMenu()
        }
        // Кнопка для добавления записи
        fab2.setOnClickListener {
            closeFabMenu()
            val intent = Intent(this, NewDiaryActivity::class.java)
            // 2-ой аргумент это requestCode по которому определяется откуда был запрос
            startActivityForResult(intent, newDiaryActivityRequestCode)
        }
        // Кнопка сортировки по избранным
        fab1.setOnClickListener {
            closeFabMenu()
            if(prefs.getBoolean("sorted", false))
            {
                prefs.edit().putBoolean("sorted", false).apply()
                adapter.setDiaries(mainViewModel.allExtendedDiaries.value!!)
            }
            else {
                prefs.edit().putBoolean("sorted", true).apply()
                adapter.setFavoriteDiaries(mainViewModel.allExtendedDiaries.value!!)
            }
        }
        // Темный фон во время открытого меню
        bg_fab_menu.setOnClickListener {
            // т.е. если нажать на затемненный фон меню закроется
            closeFabMenu()
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
            val fileName = this.getExternalFilesDir(null)!!.absolutePath + "/${note.name}_${note.id}.3gpp"
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
                if (data.getStringExtra(NewDiaryActivity.EXTRA_NEW_DIARY_IMAGE) != "" && data.getStringExtra(NewDiaryActivity.EXTRA_NEW_DIARY_IMAGE) != null)
                    diary.img = data.getStringExtra(NewDiaryActivity.EXTRA_NEW_DIARY_IMAGE)
                diary.color = colors.random()
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
                diaryEdit.date = currentDate()
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
                    mainViewModel.allExtendedDiaries.observe(this@MainActivity, Observer {
                        setDiariesForSearch(recyclerview.adapter as DiaryListAdapter, prefs, it,
                            newText)
                    })
                    setDiariesForSearch(recyclerview.adapter as DiaryListAdapter, prefs,
                        mainViewModel.allExtendedDiaries.value!!, newText)
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    // Функция для вывода поискового запроса (используется только в SearchView)
    private fun setDiariesForSearch(adapter : DiaryListAdapter, prefs : SharedPreferences?,
                                  allDiariesList : List<ExtendedDiary>, newText : String?)
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
            if (prefs!!.getBoolean("sorted", false))
                adapter.setFavoriteDiaries(diariesSearchList)
            else
                adapter.setDiaries(diariesSearchList)
        }
        else // Если строка поиска пуста
        {
            if (prefs!!.getBoolean("sorted", false))
                adapter.setFavoriteDiaries(allDiariesList)
            else
                adapter.setDiaries(allDiariesList)
        }
        // Слушатель на кнопку для правильной сортировки
        fab1.setOnClickListener {
            closeFabMenu()
            if (prefs.getBoolean("sorted", false)) {
                prefs.edit().putBoolean("sorted", false).apply()
                if (newText.isNotEmpty())
                    (recyclerview.adapter as DiaryListAdapter).setDiaries(diariesSearchList)
                else
                    (recyclerview.adapter as DiaryListAdapter).setDiaries(allDiariesList)
            } else {
                prefs.edit().putBoolean("sorted", true).apply()
                if (newText.isNotEmpty())
                    (recyclerview.adapter as DiaryListAdapter).setFavoriteDiaries(diariesSearchList)
                else
                    (recyclerview.adapter as DiaryListAdapter).setFavoriteDiaries(allDiariesList)
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

    // закрывает меню
    private fun closeFabMenu() {
        isFabOpen = false

        // возвращает элементы на исходные позиции
        fab.animate().rotation(0f)
        bg_fab_menu.animate().alpha(0f)
        fab1.animate().translationY(0f).rotation(90f)
        fab2.animate().translationY(0f).rotation(90f)

        // ставит задержку на исчезновение элементов меню (250 мс)
        Handler().postDelayed({fab1.visibility = View.GONE }, 250)
        Handler().postDelayed({fab2.visibility = View.GONE }, 250)
        Handler().postDelayed({bg_fab_menu.visibility = View.GONE }, 250)
    }

    // открывает выдвиг. меню
    private fun showFabMenu() {
        isFabOpen = true

        // показывает элементы
        fab1.visibility = View.VISIBLE
        fab2.visibility = View.VISIBLE
        bg_fab_menu.visibility = View.VISIBLE

        // "выдвигает" элементы
        fab.animate().rotation(180f)
        bg_fab_menu.animate().alpha(1f)
        fab1.animate().translationY(-350f).rotation(0f)
        fab2.animate().translationY(-165f).rotation(0f)
    }
}

