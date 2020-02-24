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

    private var words = emptyList<Word>()   // Cached copy of words (TODO: почему список сразу заполнен?!)


    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { // передаем сюда образец одного элемента списка
        val wordItemView: TextView = itemView.findViewById(R.id.textView1)           // textView1 - вью из файла recyclerview_layout
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_layout,    // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский), 3-ий аргумент false (т.к. RecyclerView)
            parent,
            false)
        return WordViewHolder(itemView)     // возвращает созданный экземпляр WordViewHolder (скорее всего возвращает готовый обработанный образец)
    }


    override fun onBindViewHolder(holder: WordViewHolder, position: Int) { // Устанавливает значение для каждого элемента RecyclerView
        val current = words[position]                               // конкретно тут устанавливает название в TextView для каждого из элементов RecyclerView
        holder.wordItemView.text = current.word                            //
    }


    internal fun setWords(words: List<Word>) { // используется при изменении списка
        this.words = words                     // обновляем внутренний список
        notifyDataSetChanged()                 // means whenever you called notifyDataSetChanged() it call onBindViewHolder()
                                               // т.е. когда данные добавились, обзерверы будут уведомлены (+ вызовется onBindViewHolder(), не факт!!!)
    }


    override fun getItemCount() = words.size // сколько эл-тов будет в списке
}