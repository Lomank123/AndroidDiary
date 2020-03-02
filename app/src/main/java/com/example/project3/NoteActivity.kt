package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_note.*
import recyclerviewadapter.NoteListAdapter
import roomdatabase.Note
import viewmodel.NoteViewModel

class NoteActivity : AppCompatActivity() {

    private val newWordActivityRequestCode = 1              // для onActivityResult
    private lateinit var noteViewModel: NoteViewModel       // добавляем ViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        val wordId = intent.getLongExtra("tag", -1)

        val adapter = NoteListAdapter(this, wordId ){

            val intent = Intent(this, ClickedActivity::class.java)

            intent.putExtra("tag1", it.note)

            // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
            startActivity(intent)

        }


        recyclerview1.adapter = adapter
        recyclerview1.layoutManager = LinearLayoutManager(this)
        noteViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)


        noteViewModel.allNotes.observe(this, Observer {
            var getList = emptyList<Note>()
            for(i in it)
            {
                if(i.word.id == wordId)
                {
                    getList = i.notes
                    break
                }
            }

            adapter.setNotes(getList)
        })

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {

            val intent = Intent(this, NewWordActivity::class.java)

            // 2-ой аргумент это requestCode по которому определяется откуда был запрос
            startActivityForResult(intent, newWordActivityRequestCode)

        } // TODO: вынести создание другого окна в отдельный метод для кнопки

        // кнопка, которая будет выводить отсортированный список (если добавить запись все собьется)
        val fab1 = findViewById<FloatingActionButton>(R.id.fab1)
        fab1.setOnClickListener {
         //   adapter.setNewNotes(noteViewModel.allNotes.value!!)
         //   adapter.notifyDataSetChanged()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            data?.getStringArrayListExtra(NewWordActivity.EXTRA_REPLY)?.let {

                // получаем из экстра данных нашу строку и создаем объект Word с той же строкой
                val note = Note(it[0], it[1], intent.getLongExtra("tag", -1))
                noteViewModel.insertNote(note) // добавляем запись в БД
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
