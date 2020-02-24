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

    private val newWordActivityRequestCode = 1
    private lateinit var wordViewModel: WordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview) // объект RecyclerView
        val adapter = WordListAdapter(this)                                   // объект с помощью которого формируется RecycleView

        recyclerView.adapter = adapter                                  // задаем Adapter
        recyclerView.layoutManager = LinearLayoutManager(this)  // задаем LinearLayoutManager (пока не знаю зачем, скорее всего одинаково для всех RecyclerView)

        wordViewModel = ViewModelProvider(this).get(WordViewModel::class.java)     // Создаем провайдер (одинаково для всех ViewModel)
        wordViewModel.allWords.observe(this, Observer {
            adapter.setWords(it)            // следит за изменением данных и при наличии таковых обновляет данные в RecyclerView
            adapter.notifyDataSetChanged()  // Для наглядности: notifyDataSetChanged() как бы "привязан" к RecyclerView и когда данные обновятся, то они обновятся и во вью

        })                                  // Если какие-либо изменения были, обсервер это заметит и даст сигнал, который задействует setWords и обновит
                                            // данные в RecycleVIew. notifyDataSetChanged() даст сигнал о том, что данные изменились и нужно их обновить и во вью

        val fab = findViewById<FloatingActionButton>(R.id.fab) // кнопка для запуска активити для добавления записи
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewWordActivity::class.java)
            startActivityForResult(intent, newWordActivityRequestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // когда идет возврат со второй активити на первую
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK) {
            data?.getStringExtra(NewWordActivity.EXTRA_REPLY)?.let {
                val word = Word(it)
                wordViewModel.insert(word)
            }
        }
        else { // Если было пустое поле
            Toast.makeText(
                applicationContext,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG).show()
        }
    }
} // TODO: comment MainActivity class code
