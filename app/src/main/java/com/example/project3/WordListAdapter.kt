package com.example.project3

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WordListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() { // RecyclerView.Adapter - тип, который дает понять, что весь класс - адаптер

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var words = emptyList<Word>() // Cached copy of words

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { // передаем сюда образец одного элемента списка
        val wordItemView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {  // Создаем
        val itemView = inflater.inflate(R.layout.recyclerview_layout, parent, false) // parent, 3-ий арг. - не знаю, 1-ый аргумент - путь до XML файла с образцом
        return WordViewHolder(itemView) // возвращает созданный экземпляр WordViewHolder (скорее всего возвращает готовый обработанный образец)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) { //
        val current = words[position]
        holder.wordItemView.text = current.word
    }

    internal fun setWords(words: List<Word>) { // видимо используется при добавлении новой записи
        this.words = words                     // обновляем внутренний список
        notifyDataSetChanged()                 // means whenever you called notifyDataSetChanged() it call onBindViewHolder()
                                               // т.е. когда данные добавились, обзерверы будут уведомлены (+ вызовется onBindViewHolder(), не факт!!!)
    }

    override fun getItemCount() = words.size // сколько эл-тов будет в списке
}