package com.example.project3

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

class WordListAdapter internal constructor( // internal constructor means, that a constructor of an internal class is only visible within the same module, module means this full project
    context: Context,
    private val listener : (Word) -> Unit   // похоже на какой-то template для функций
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {    // RecyclerView.Adapter - тип, который дает понять, что весь класс - адаптер

    private val inflater: LayoutInflater = LayoutInflater.from(context) // По сути переменная inflater используется как метка на родительский XML, которая используется в onCreateViewHolder

    //private val mContext = context

    private var words = emptyList<Word>()   // Cached copy of words



    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { // передаем сюда образец одного элемента списка

        private val wordItemView: TextView = itemView.findViewById(R.id.textView1)           // textView1 - вью из файла recyclerview_layout.xml

        fun bindView(word: Word, listener : (Word) -> Unit) {   // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
            wordItemView.text = word.word                       // Устанавливаем соотв. слово из списка в TextView
            itemView.setOnClickListener {                       // Устанавливаем обработчик нажатий
                listener(word)                                  // возможно он применяет то, что описано в фигурных скобках в MainActivity
            }
        }
    }                                                                                // этот класс ХРАНИТ в себе то самое вью, в котором будут что-то менять


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_layout,    // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский), 3-ий аргумент false (т.к. RecyclerView)
            parent,
            false)
        return WordViewHolder(itemView)     // не совсем понятно, но работает (одинаково для всех RecyclerView)
    }


    override fun onBindViewHolder(holder: WordViewHolder, position: Int) { // Устанавливает значение для каждого элемента RecyclerView
        holder.bindView(words[position], listener)
    }


    internal fun setWords(words: List<Word>) { // используется при изменении списка
        this.words = words                     // обновляем внутренний список
        notifyDataSetChanged()
    }                                          // ВАЖНО: setWords вызывается в момент того, когда обсервер заметил изменения в записях
                                               // и чтобы зафиксировать эти изменения в RecyclerView, нужно передавать новый список сюда

    internal fun setNewWords(words: List<Word>){ // сортирует список слов по алфавиту
        this.words = words.sortedBy { it.word }
        notifyDataSetChanged()
    }


    override fun getItemCount() = words.size // сколько эл-тов будет в списке
}