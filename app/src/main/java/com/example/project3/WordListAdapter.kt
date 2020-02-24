package com.example.project3

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WordListAdapter internal constructor( // internal constructor means, that a constructor of an internal class is only visible within the same module
    context: Context                        // module means this full project
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {    // RecyclerView.Adapter - тип, который дает понять, что весь класс - адаптер

    private val inflater: LayoutInflater = LayoutInflater.from(context) // По сути переменная inflater используется как метка на родительский XML, которая используется в onCreateViewHolder

    private var words = emptyList<Word>()   // Cached copy of words


    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { // передаем сюда образец одного элемента списка
        val wordItemView: TextView = itemView.findViewById(R.id.textView1)           // textView1 - вью из файла recyclerview_layout
    }                                                                                // этот класс ХРАНИТ в себе то самое вью, в котором будут что-то менять


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_layout,    // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский), 3-ий аргумент false (т.к. RecyclerView)
            parent,
            false)
        return WordViewHolder(itemView)     // не совсем понятно, но работает (одинакого для всех RecyclerView)
    }


    override fun onBindViewHolder(holder: WordViewHolder, position: Int) { // Устанавливает значение для каждого элемента RecyclerView
        val current = words[position]                               // конкретно тут устанавливает название в TextView для каждого из элементов RecyclerView
        holder.wordItemView.text = current.word                            // current - объект класса Word из файла Word.kt, оттуда и .word property
    }


    internal fun setWords(words: List<Word>) { // используется при изменении списка
        this.words = words                     // обновляем внутренний список

    }                                          // ВАЖНО: setWords вызывается в момент того, когда обсервер заметил изменения в записях
                                               // и чтобы зафиксировать эти изменения в RecyclerView, нужно передавать новый список сюда

    override fun getItemCount() = words.size // сколько эл-тов будет в списке
}