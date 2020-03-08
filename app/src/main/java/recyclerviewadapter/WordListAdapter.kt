package recyclerviewadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.project3.R
import roomdatabase.Word

// internal constructor means, that a constructor of an internal class is only
// visible within the same module, module means this full project

class WordListAdapter internal constructor(
    context: Context,
    private val listenerDeleteWord : (Word) -> Unit,
    private val listenerOpenWord : (Word) -> Unit
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context // откуда было запущено активити

    private var words = emptyList<Word>()   // Сюда будут сохраняться дневники


    // передаем сюда образец одного элемента списка
    // этот класс ХРАНИТ в себе то самое вью, в котором будут что-то менять
    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // textView1 - вью из файла recyclerview_layout.xml, отвечает за название
        private val wordItemView: TextView = itemView.findViewById(R.id.textView1)
        // textView - вью из файла recyclerview_layout.xml, отвечает за описание
        private val wordDescriptionView: TextView = itemView.findViewById(R.id.textView)

        // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
        fun bindView(word: Word, listener : (Word) -> Unit) {

            // устанавливаем значения во вью
            wordItemView.text = word.word

            // Лимит на кол-во символов в описании: 16
            var count = 0
            var str = ""
            for(i in word.description) { // записываем в строку первые 16 символов
                if (count == 16) {
                    str += "..." // если их > 16, добавляем многоточие и завершаем цикл
                    break
                }
                str += i
                count++
            }
            wordDescriptionView.text = str // записываем в TextView строку (описание)

            // Устанавливаем обработчик нажатий на элемент RecyclerView, при нажатии
            // будет вызываться первый listener, который открывает дневник
            itemView.setOnClickListener {
                listener(word)
            }

            // обработчик долгих нажатий для вызова контекстного меню
            itemView.setOnLongClickListener{
                Toast.makeText(mContext, "Long Click", Toast.LENGTH_SHORT).show()

                // Устанавливаем контекстное меню
                val popupMenu = PopupMenu(mContext, it)

                // Устанавливаем обработчик нажатий на пункты контекстного меню
                popupMenu.setOnMenuItemClickListener { item ->
                    when(item.itemId) {     // сколько пунктов меню - столько и вариантов в when()
                        R.id.delete -> {    // удаление дневника
                            listenerDeleteWord(word) // вызываем listener, описанный в NoteActivity
                            Toast.makeText(mContext, "Delete diary", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.open -> { // открытие дневника
                            listener(word) // listener, описанный в NoteActivity
                            Toast.makeText(mContext, "Open diary", Toast.LENGTH_SHORT).show()
                            // Т.к. в этом обработчике нужно вернуть boolean, возвращаем true
                            true
                        }
                        // Иначе вернем false (если when не сработал ни разу)
                        else -> false
                    }
                }
                // Связываем XML-файл menu_notes и показываем меню
                popupMenu.inflate(R.menu.menu_notes)
                popupMenu.show()
                // Т.к. в LongClickListener нужно вернуть boolean, возвращаем его
                return@setOnLongClickListener true
            }
        }
    }

    // создание ViewHolder (одинаково для всех RecyclerView)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский)
        val itemView = inflater.inflate(R.layout.recyclerview_layout, parent,
            false)
        return WordViewHolder(itemView)
    }

    // Устанавливает значение для каждого элемента RecyclerView
    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bindView(words[position], listenerOpenWord)
    }

    // ВАЖНО: setWords вызывается в момент того, когда обсервер заметил изменения в записях
    // и чтобы зафиксировать эти изменения в RecyclerView, нужно передавать новый список сюда
    internal fun setWords(words: List<Word>) {
        this.words = words      // обновляем внутренний список
        notifyDataSetChanged()  // даем понять адаптеру, что были внесены изменения
    }

    // сортирует список слов по алфавиту
    internal fun setNewWords(words: List<Word>)
    {
        this.words = words.sortedBy { it.word }
        notifyDataSetChanged()  // даем понять адаптеру, что были внесены изменения
    }

    override fun getItemCount() = words.size // сколько эл-тов будет в списке
}