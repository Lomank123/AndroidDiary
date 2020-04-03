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
import com.example.project3.NewWordActivity.Companion.EXTRA_IMAGE
import kotlinx.android.synthetic.main.activity_main.*
import recyclerviewadapter.WordListAdapter
import roomdatabase.Word
import viewmodel.TopSpacingItemDecoration
import viewmodel.WordViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val newWordActivityRequestCode = 1          // для NewWordActivity
    private val editActivityRequestCode = 2             // для EditActivity
    private lateinit var wordViewModel: WordViewModel   // добавляем ViewModel
    private var isFabOpen : Boolean = false             // по умолч. меню закрыто
    private val colors: List<String> = listOf("green", "blue", "grass", "purple", "yellow")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        if (!(prefs!!.contains("sorted")))
            prefs.edit().putBoolean("sorted", false).apply()

        // адаптер для RecyclerView
        // то, что в фигурных скобках это и есть аргумент listener : (Word) -> Unit в адаптере
        val adapter = WordListAdapter(this,
            {
                // Первый listener, отвечает за удаление дневника
                deleteWord(it)
            }, {
                // Второй listener
                // отсюда будет запускаться новый RecyclerView для отображения списка заметок
                val intent = Intent(this, NoteActivity::class.java)
                intent.putExtra("word_id", it.id) // передаем id дневника
                intent.putExtra("wordSelf", it)
                intent.putExtra("word_img", it.img) // передаем картинку
                // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
                startActivity(intent)
            }, {
                // 3-ий listener, отвечает за изменение дневника
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra("wordSerializableEdit", it)
                startActivityForResult(intent, editActivityRequestCode)
            })

        // задаем Adapter (одинаково для всех RecyclerView)
        recyclerview.adapter = adapter

        // задаем LinearLayoutManager (одинаково для всех RecyclerView)
        recyclerview.layoutManager = LinearLayoutManager(this)

        //присваиваем переменной тот самый *отступ*
        val topSpacingDecoration = TopSpacingItemDecoration(20)
        //добавляет в декорацию элемента дневника и заметки этот отступ
        recyclerview.addItemDecoration(topSpacingDecoration)

        // Создаем провайдер, связывая с соотв. классом ViewModel (одинаково для всех ViewModel)
        wordViewModel = ViewModelProvider(this).get(WordViewModel::class.java)

        // следит за изменением данных и при наличии таковых обновляет данные в RecyclerView
        // Если какие-либо изменения были, обсервер это заметит и даст сигнал, который задействует
        // setWords и обновит данные в списке внутри адаптера.
        wordViewModel.allWords.observe(this, Observer {

            if (prefs.getBoolean("sorted", false))
                adapter.setFavoriteWords(it)
            else
                adapter.setWords(it)
        })

        fab.setOnClickListener {
            if (!isFabOpen)
                showFabMenu()
            else
                closeFabMenu()
        }

        // кнопка для запуска активити для добавления записи
        fab2.setOnClickListener {
            closeFabMenu()
            val intent = Intent(this, NewWordActivity::class.java)
            // 2-ой аргумент это requestCode по которому определяется откуда был запрос
            startActivityForResult(intent, newWordActivityRequestCode)
        }

        // кнопка сортировки по избранным
        fab1.setOnClickListener {
            closeFabMenu()
            if(prefs.getBoolean("sorted", false))
            {
                prefs.edit().putBoolean("sorted", false).apply()
                adapter.setWords(wordViewModel.allWords.value!!)
            }
            else {
                prefs.edit().putBoolean("sorted", true).apply()
                adapter.setFavoriteWords(wordViewModel.allWords.value!!)
            }
        }
        bg_fab_menu.setOnClickListener {
            // т.е. если нажать на затемненный фон меню закроется
            closeFabMenu()
        }
    }

    override fun onResume()
    {
        super.onResume()
        recyclerview.adapter!!.notifyDataSetChanged()
    }

    // Удаляет дневник. Вызов происходит через ViewModel
    private fun deleteWord(word : Word)
    {
        wordViewModel.deleteWord(word)
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

    // функция для обработки результата после вызова startActivityForResult()
    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK) {
            data?.getStringArrayListExtra(NewWordActivity.EXTRA_REPLY)?.let {

                // получаем текущую дату и вставляем в объект дневника
                val pattern = "\t\t\tHH:mm\n\ndd.MM.yyyy"
                val simpleDateFormat =
                    SimpleDateFormat(pattern)
                val currentDate = simpleDateFormat.format(Date())
                // получаем из экстра данных нашу строку и создаем объект Word с той же строкой
                val word = Word(it[0], it[1], currentDate)
                if (data.getStringExtra(EXTRA_IMAGE) != "" && data.getStringExtra(EXTRA_IMAGE) != null)
                    word.img = data.getStringExtra(EXTRA_IMAGE)
                word.color = colors.random()
                wordViewModel.insertWord(word) // добавляем запись в БД
            }
        }

        if (requestCode == editActivityRequestCode && resultCode == Activity.RESULT_OK) {
            val wordEdit = data?.getSerializableExtra(EditActivity.EXTRA_EDIT_WORD) as? Word
            val imgWordEdit = data?.getStringExtra(EditActivity.EXTRA_IMAGE_EDIT_WORD)
            if (imgWordEdit != null && imgWordEdit != "")
                wordEdit!!.img = imgWordEdit
            if (wordEdit != null)
                wordViewModel.updateWord(wordEdit)
        }

        if ((requestCode == newWordActivityRequestCode || requestCode == editActivityRequestCode) &&
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

        val adapter = WordListAdapter(this@MainActivity,
            {
                deleteWord(it)
            }, {
                val intent = Intent(this@MainActivity, NoteActivity::class.java)
                intent.putExtra("word_id", it.id)
                intent.putExtra("wordSelf", it)
                intent.putExtra("word_img", it.img)
                startActivity(intent)
            }, {
                val intent = Intent(this@MainActivity, EditActivity::class.java)
                intent.putExtra("wordSerializableEdit", it)
                startActivityForResult(intent, editActivityRequestCode)
            })

        val searchItem = menu!!.findItem(R.id.search_view)
        if (searchItem != null)
        {
            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    recyclerview.adapter = adapter

                    fab1.setOnClickListener {
                        closeFabMenu()
                        if (prefs!!.getBoolean("sorted", false)) {
                            prefs.edit().putBoolean("sorted", false).apply()
                            adapter.setWords(wordViewModel.allWords.value!!)
                        } else {
                            prefs.edit().putBoolean("sorted", true).apply()
                            adapter.setFavoriteWords(wordViewModel.allWords.value!!)
                        }
                    }

                    wordViewModel.allWords.observe(this@MainActivity, Observer {

                        if (newText!!.isNotEmpty())
                        {
                            val wordList1 = mutableListOf<Word>()
                            val search = newText.toLowerCase(Locale.ROOT)
                            it.forEach{words ->
                                if(words.word.toLowerCase(Locale.ROOT).contains(search))
                                    wordList1.add(words)
                            }
                            if (prefs!!.getBoolean("sorted", false))
                                adapter.setFavoriteWords(wordList1)
                            else
                                adapter.setWords(wordList1)
                        }
                        else
                        {
                            if (prefs!!.getBoolean("sorted", false))
                                adapter.setFavoriteWords(it)
                            else
                                adapter.setWords(it)
                        }
                    })
                    if (newText!!.isNotEmpty())
                    {
                        val wordList1 = mutableListOf<Word>()
                        val search = newText.toLowerCase(Locale.ROOT)
                        wordViewModel.allWords.value!!.forEach{
                            if(it.word.toLowerCase(Locale.ROOT).contains(search))
                                wordList1.add(it)
                        }
                        if (prefs!!.getBoolean("sorted", false))
                            adapter.setFavoriteWords(wordList1)
                        else
                            adapter.setWords(wordList1)
                    }
                    else
                    {
                        if (prefs!!.getBoolean("sorted", false))
                            adapter.setFavoriteWords(wordViewModel.allWords.value!!)
                        else
                            adapter.setWords(wordViewModel.allWords.value!!)
                    }
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
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
}