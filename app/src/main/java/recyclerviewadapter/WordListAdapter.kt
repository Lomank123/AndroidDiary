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

// RecyclerView.Adapter - тип, который дает понять, что весь класс - адаптер
class WordListAdapter internal constructor(
    context: Context,
    private val listenerDeleteWord : (Word) -> Unit,
    private val listener : (Word) -> Unit   // похоже на какой-то template для функций
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context

    private var words = emptyList<Word>()   // Cached copy of words


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

            var count : Int = 0
            var str : String = ""
            for(i in word.description) {
                if (count == 16) {
                    str += "..."
                    break
                }
                str += i
                count++
            }

            wordDescriptionView.text = str

            // возможно он применяет то, что описано в фигурных скобках в MainActivity
            itemView.setOnClickListener {   // Устанавливаем обработчик нажатий
                listener(word)
            }
            itemView.setOnLongClickListener{ // При long click'е будет срабатывать контекстное меню
                Toast.makeText(mContext, "Long Click", Toast.LENGTH_SHORT).show()

                val popupMenu = PopupMenu(mContext, it)

                popupMenu.setOnMenuItemClickListener { item ->
                    when(item.itemId) {
                        R.id.delete -> { // удаление дневника
                            listenerDeleteWord(word)
                            Toast.makeText(mContext, "Delete diary", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.open -> { // открытие дневника
                            listener(word)
                            Toast.makeText(mContext, "Open diary", Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.inflate(R.menu.menu_notes)
                popupMenu.show()
                return@setOnLongClickListener true
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {

        // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский)
        val itemView = inflater.inflate(R.layout.recyclerview_layout, parent,
            false)

        return WordViewHolder(itemView)     // (одинаково для всех RecyclerView)
    }

    // Устанавливает значение для каждого элемента RecyclerView
    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bindView(words[position], listener)
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
        notifyDataSetChanged()
    }

    override fun getItemCount() = words.size // сколько эл-тов будет в списке
}