package com.example.project3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project3.NewWordActivity.Companion.EXTRA_IMAGE
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import recyclerviewadapter.WordListAdapter
import roomdatabase.Word
import viewmodel.TopSpacingItemDecoration
import viewmodel.WordViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val newWordActivityRequestCode = 1  // для NewWordActivity
    private val editActivityRequestCode = 2

    private lateinit var wordViewModel: WordViewModel       // добавляем ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // адаптер для RecyclerView
        val adapter = WordListAdapter(this,
            {
                // Первый listener, отвечает за удаление дневника
                deleteWord(it)
            }, {
                // Второй listener
                // отсюда будет запускаться новый RecyclerView для отображения списка заметок
                val intent = Intent(this, NoteActivity::class.java)
                intent.putExtra("word_id", it.id) // передаем id дневника
                intent.putExtra("word_img", it.img) // передаем картинку
                // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
                startActivity(intent)
            }, {

                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra("wordSerializableEdit", it)
                startActivityForResult(intent, editActivityRequestCode)
            }) // то, что в фигурных скобках это и есть аргумент listener : (Word) -> Unit в адаптере

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
        // notifyDataSetChanged() даст сигнал о том, что данные изменились
        // и нужно их обновить и в самом RecycleView
        wordViewModel.allWords.observe(this, Observer {
            adapter.setWords(it)

        })

        // кнопка для запуска активити для добавления записи
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, NewWordActivity::class.java)

            // 2-ой аргумент это requestCode по которому определяется откуда был запрос
            startActivityForResult(intent, newWordActivityRequestCode)
        }

    }

    // Удаляет дневник. Вызов происходит через ViewModel
    private fun deleteWord(word : Word)
    {
        wordViewModel.deleteWord(word)
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
            Toast.makeText(this, "Canceled or diary name is empty",
                Toast.LENGTH_SHORT).show()
        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_menu, menu)
        val searchItem = menu!!.findItem(R.id.search_view)
        if (searchItem != null)
        {

            val searchView = searchItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

                val adapter = WordListAdapter(this@MainActivity,
                    {
                        // Первый listener, отвечает за удаление дневника
                        deleteWord(it)
                    }, {
                        // Второй listener
                        // отсюда будет запускаться новый RecyclerView для отображения списка заметок
                        val intent = Intent(this@MainActivity, NoteActivity::class.java)
                        intent.putExtra("word_id", it.id) // передаем id дневника
                        intent.putExtra("word_img", it.img) // передаем картинку
                        // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
                        startActivity(intent)
                    }, {

                        val intent = Intent(this@MainActivity, EditActivity::class.java)
                        intent.putExtra("wordSerializableEdit", it)
                        startActivityForResult(intent, editActivityRequestCode)
                    })

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    recyclerview.adapter = adapter
                    wordViewModel.allWords.observe(this@MainActivity, Observer {

                        if (newText!!.isNotEmpty())
                        {
                            val wordList1 = mutableListOf<Word>()
                            val search = newText.toLowerCase(Locale.ROOT)
                            it.forEach{words ->
                                if(words.word.toLowerCase(Locale.ROOT).contains(search))
                                    wordList1.add(words)
                            }
                            adapter.setWords(wordList1)
                        }
                        else
                            adapter.setWords(it)
                    })
                    if (newText!!.isNotEmpty())
                    {
                        val wordList1 = mutableListOf<Word>()
                        val search = newText.toLowerCase(Locale.ROOT)
                        wordViewModel.allWords.value!!.forEach{
                            if(it.word.toLowerCase(Locale.ROOT).contains(search))
                                wordList1.add(it)
                        }
                        adapter.setWords(wordList1)
                    }
                    else
                        adapter.setWords(wordViewModel.allWords.value!!)
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