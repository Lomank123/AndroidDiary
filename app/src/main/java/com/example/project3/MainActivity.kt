package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import recyclerviewadapter.WordListAdapter
import roomdatabase.Word
import viewmodel.WordViewModel

class MainActivity : AppCompatActivity() {

    private val newWordActivityRequestCode = 1              // для onActivityResult
    private lateinit var wordViewModel: WordViewModel       // добавляем ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("info", "Created") // запись в логи

        // адаптер для RecyclerView
        val adapter = WordListAdapter(this) {

            // отсюда будет запускаться новый RecyclerView для отображения списка заметок

            val intent = Intent(this, NoteActivity::class.java)

            intent.putExtra("tag", it.id)

            // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
            startActivity(intent)

        }
        // то, что в фигурных скобках это и есть аргумент listener : (Word) -> Unit в адаптере


        // задаем Adapter (одинаково для всех RecyclerView)
        recyclerview.adapter = adapter

        // задаем LinearLayoutManager (одинаково для всех RecyclerView)
        recyclerview.layoutManager = LinearLayoutManager(this)

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

        } // TODO: вынести создание другого окна в отдельный метод для кнопки

        // кнопка, которая будет выводить отсортированный список (если добавить запись все собьется)
        val fab1 = findViewById<FloatingActionButton>(R.id.fab1)
        fab1.setOnClickListener {

            adapter.setNewWords(wordViewModel.allWords.value!!)
            adapter.notifyDataSetChanged()

        }

    }

    // когда идет возврат со второй активити на первую
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            data?.getStringArrayListExtra(NewWordActivity.EXTRA_REPLY)?.let {

                // получаем из экстра данных нашу строку и создаем объект Word с той же строкой
                val word = Word(it[0], it[1])

                wordViewModel.insert(word) // добавляем запись в БД

            }

        }
        else    // Если было пустое поле
        {
            // выводим сообщение о том что поле пустое, ничего не меняя в БД
            Toast.makeText(applicationContext, R.string.empty_not_saved,
                Toast.LENGTH_LONG).show()

        }

    }

}
