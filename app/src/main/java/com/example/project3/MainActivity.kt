package com.example.project3

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var wordViewModel: WordViewModel       // добавляем ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("info", "Created") // запись в логи

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
            })// то, что в фигурных скобках это и есть аргумент listener : (Word) -> Unit в адаптере

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

        // TODO: Изменить или убрать сортировку (за ненадобностью)
        // кнопка, которая будет выводить отсортированный список (если добавить запись все собьется)
        val fab1 = findViewById<FloatingActionButton>(R.id.fab1)
        fab1.setOnClickListener {
            adapter.setNewWords(wordViewModel.allWords.value!!)
            adapter.notifyDataSetChanged()
        }
    }

    // Удаляет дневник. Вызов происходит через ViewModel
    private fun deleteWord(word : Word)
    {
        wordViewModel.deleteWord(word)
    }

    // функция для обработки результата после вызова startActivityForResult()
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
                word.img = data.getStringExtra(EXTRA_IMAGE)
                wordViewModel.insertWord(word) // добавляем запись в БД
            }
        }
        else    // Если было пустое поле
        {
            // выводим сообщение о том что поле пустое, ничего не меняя в БД
            Toast.makeText(
                applicationContext, R.string.empty_not_saved, Toast.LENGTH_LONG).show()
        }
    }

    // создает OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // когда выбираешь элемент меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.settings -> {
                // открытие окна "Настройки"
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
                return super.onOptionsItemSelected(item)
            }
            R.id.about -> {
                // открытие окна "О нас"
                val aboutIntent = Intent(this, AboutActivity::class.java)
                startActivity(aboutIntent)

                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }
} // TODO: ПОМЕНЯТЬ НАЗВАНИЯ КНОПОК ВО ВСЕХ ФАЙЛАХ ЧТОБЫ БЫЛО ПО СМЫСЛУ (ОПЦИОНАЛЬНО)
