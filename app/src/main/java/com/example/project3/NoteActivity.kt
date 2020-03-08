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
import com.example.project3.ClickedActivity.Companion.EXTRA_REPLY_EDIT
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_note.*
import recyclerviewadapter.NoteListAdapter
import roomdatabase.Note
import roomdatabase.Word
import viewmodel.NoteViewModel

class NoteActivity : AppCompatActivity() {

    private val newWordActivityRequestCode = 1              // для onActivityResult
    private val clickedActivityRequestCode = 2
    private lateinit var noteViewModel: NoteViewModel       // добавляем ViewModel


    private var isFabOpen : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        val wordId = intent.getLongExtra("tag", -1)

        val adapter = NoteListAdapter(this, {

            val intent = Intent(this, ClickedActivity::class.java)

            intent.putExtra("idTag", it.idNote)
            intent.putExtra("tag1", it.note)
            intent.putExtra("tag2", it.text)

            // запускает ClickedActivity из MainActivity путем нажатия на элемент RecyclerView
            startActivityForResult(intent, clickedActivityRequestCode)

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
                showFabMenu()
            else
                closeFabMenu()
        }

        fab1.setOnClickListener {
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


    private fun closeFabMenu() { // закрытие выдвиг. меню
        isFabOpen = false

        fab.animate().rotation(0f)
        bg_fab_menu.animate().alpha(0f)
        fab1.animate().translationY(0f).rotation(90f)
        fab2.animate().translationY(0f).rotation(90f)

        Handler().postDelayed({fab1.visibility = GONE}, 250)
        Handler().postDelayed({fab2.visibility = GONE}, 250)
        Handler().postDelayed({bg_fab_menu.visibility = GONE}, 250)
    }

    private fun showFabMenu() { // открытие выдвиг. меню
        isFabOpen = true
        fab1.visibility = VISIBLE
        fab2.visibility = VISIBLE
        bg_fab_menu.visibility = VISIBLE

        fab.animate().rotation(135f)
        bg_fab_menu.animate().alpha(1f)
        fab1.animate().translationY(-300f).rotation(0f)
        fab2.animate().translationY(-165f).rotation(0f)
    }

    private fun deleteNote(note : Note) // удаление записи
    {
        noteViewModel.deleteNote(note)
    }

    private fun deleteBoth(word: Word)
    {
        noteViewModel.deleteWord(word)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Результат для добавления заметки
        if (requestCode == newWordActivityRequestCode && resultCode == Activity.RESULT_OK)
        {
            data?.getStringArrayListExtra(NewWordActivity.EXTRA_REPLY)?.let {

                // получаем из экстра данных нашу строку и создаем объект Word с той же строкой
                val note = Note(it[0], it[1], intent.getLongExtra("tag", -1))
                noteViewModel.insertNote(note) // добавляем запись в БД
            }
        }

        // Результат для обновления заметки
        if (requestCode == clickedActivityRequestCode && resultCode == Activity.RESULT_OK) {

            data?.getStringArrayListExtra(EXTRA_REPLY_EDIT)?.let {
                val note1 = Note(it[0], it[1], intent.getLongExtra("tag", -1))
                note1.idNote = data.getLongExtra("noteId", -1)
                noteViewModel.updateNote(note1)
            }

        }

    }




}
