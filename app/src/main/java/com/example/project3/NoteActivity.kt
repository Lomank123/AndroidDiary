package com.example.project3

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View.GONE
import android.view.View.VISIBLE
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


    private var isFabOpen : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        val wordId = intent.getLongExtra("tag", -1)

        val adapter = NoteListAdapter(this, wordId, {

            val intent = Intent(this, ClickedActivity::class.java)

            intent.putExtra("tag1", it.note)

            // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
            startActivity(intent)

        }, {
            deleteNote(it)
        })

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


        val fab = findViewById<FloatingActionButton>(R.id.fab) // main button
        fab.setOnClickListener {
            if (!isFabOpen)
            {
                showFabMenu()
            }
            else
            {
                closeFabMenu()
            }
        }

        fab1.setOnClickListener {

        //    val a = noteViewModel.allNotes.value?.get(0) // удаляет заметки в ПЕРВОМ дневнике (для проверки, потом переделать)
        //    if(a != null)
        //    {
        //        noteViewModel.deleteNote(a.notes[0])
        //    }

            closeFabMenu()
            Toast.makeText(this, "Message", Toast.LENGTH_SHORT).show()
        }

        fab2.setOnClickListener {

            closeFabMenu()
            val intent = Intent(this, NewWordActivity::class.java)
            //  // 2-ой аргумент это requestCode по которому определяется откуда был запрос
            startActivityForResult(intent, newWordActivityRequestCode)
        }

        bg_fab_menu.setOnClickListener {
            closeFabMenu()
        }


    }


    private fun closeFabMenu() {
        isFabOpen = false

        fab.animate().rotation(0f)
        bg_fab_menu.animate().alpha(0f)
        fab1.animate().translationY(0f).rotation(90f)
        fab2.animate().translationY(0f).rotation(90f)

        Handler().postDelayed({fab1.visibility = GONE}, 250)
        Handler().postDelayed({fab2.visibility = GONE}, 250)
        Handler().postDelayed({bg_fab_menu.visibility = GONE}, 250)
    }

    private fun showFabMenu() {
        isFabOpen = true
        fab1.visibility = VISIBLE
        fab2.visibility = VISIBLE
        bg_fab_menu.visibility = VISIBLE

        fab.animate().rotation(135f)
        bg_fab_menu.animate().alpha(1f)
        fab1.animate().translationY(-300f).rotation(0f)
        fab2.animate().translationY(-165f).rotation(0f)
    }

    fun deleteNote(note : Note)
    {
        noteViewModel.deleteNote(note)
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
