package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val newWordActivityRequestCode = 1              // для onActivityResult
    private lateinit var wordViewModel: WordViewModel       // добавляем ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview) // объект RecyclerView

        val adapter = WordListAdapter(this) {
            val intent = Intent(this, ClickedActivity::class.java)   // TODO: Непонятно как работает listener внутри адаптера, выяснить
            intent.putExtra("tag", it.word)                                  // TODO: исп. startActivityForResult



            startActivity(intent)                                                       // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
                                                                                        // то, что в фигурных скобках это и есть аргумент listener : (Word) -> Unit в адаптере (догадки)
        }    // адаптер для RecyclerView

        recyclerView.adapter = adapter                                  // задаем Adapter
        recyclerView.layoutManager = LinearLayoutManager(this)  // задаем LinearLayoutManager (одинаково для всех RecyclerView)

        wordViewModel = ViewModelProvider(this).get(WordViewModel::class.java)     // Создаем провайдер, связывая с соотв. классом ViewModel (одинаково для всех ViewModel)

        wordViewModel.allWords.observe(this, Observer {
            adapter.setWords(it)            // следит за изменением данных и при наличии таковых обновляет данные в RecyclerView
        })                                  // Если какие-либо изменения были, обсервер это заметит и даст сигнал, который задействует setWords и обновит
                                            // данные в списке внутри адаптера. notifyDataSetChanged() даст сигнал о том, что данные изменились и нужно их обновить и в самом RecycleView

        val fab = findViewById<FloatingActionButton>(R.id.fab) // кнопка для запуска активити для добавления записи
        fab.setOnClickListener {
            val intent = Intent(this, NewWordActivity::class.java)
            startActivityForResult(intent, newWordActivityRequestCode) // 2-ой аргумент это requestCode по которому определяется откуда был запрос (в нашем случае из MainActivity)
        } // TODO: вынести создание другого окна в отдельный метод для кнопки

        val fab1 = findViewById<FloatingActionButton>(R.id.fab1) // кнопка, которая будет выводить отсортированный список (если добавить запись все собьется)
        fab1.setOnClickListener { // переделать
            adapter.setNewWords(wordViewModel.allWords.value!!)
            adapter.notifyDataSetChanged()

        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // когда идет возврат со второй активити на первую
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK) {
            data?.getStringExtra(NewWordActivity.EXTRA_REPLY)?.let {
                val word = Word(it)                          // получаем из экстра данных нашу строку и создаем объект Word с той же строкой
                wordViewModel.insert(word)
            }                                                // добавляем слово в БД
        }
        else {                      // Если было пустое поле
            Toast.makeText(         // выводим сообщение о том что поле пустое, ничего не меняя в БД
                applicationContext,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG).show()
        }
    }
}
