package recyclerviewadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project3.R
import repository.NotesAndWords
import roomdatabase.Note
import roomdatabase.Word

class NoteListAdapter internal constructor(
    context: Context,
    private val wordId: Long,
    private val listener : (Note) -> Unit   // похоже на какой-то template для функций
) : RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    //private val mContext = context

    private var notes = emptyList<Note>()   // Cached copy of words
    //private var notesMapped = mutableMapOf<Long, List<Note>>()

    // передаем сюда образец одного элемента списка
    // этот класс ХРАНИТ в себе то самое вью, в котором будут что-то менять
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // textView1 - вью из файла recyclerview_layout.xml, отвечает за название
        private val noteItemView: TextView = itemView.findViewById(R.id.textView1)
        // textView - вью из файла recyclerview_layout.xml, отвечает за описание
        private val noteDescriptionView: TextView = itemView.findViewById(R.id.textView)

        // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
        fun bindView(note: Note, listener : (Note) -> Unit) {

            // устанавливаем значения во вью

                noteItemView.text = note.note

                noteDescriptionView.text = note.text


            // возможно он применяет то, что описано в фигурных скобках в MainActivity
            itemView.setOnClickListener {   // Устанавливаем обработчик нажатий
                listener(note)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {

        // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский)
        val itemView = inflater.inflate(R.layout.recyclerview_layout, parent,
            false)

        return NoteViewHolder(itemView)     // (одинаково для всех RecyclerView)
    }

    // Устанавливает значение для каждого элемента RecyclerView
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bindView(notes[position], listener)
    }

    // ВАЖНО: setWords вызывается в момент того, когда обсервер заметил изменения в записях
    // и чтобы зафиксировать эти изменения в RecyclerView, нужно передавать новый список сюда
    internal fun setNotes(notes: List<Note>) {
        this.notes = notes      // обновляем внутренний список

        notifyDataSetChanged()  // даем понять адаптеру, что были внесены изменения
    }

    // сортирует список слов по алфавиту
//    internal fun setNewNotes(notes: List<Note>)
//    {
//        this.notes = notes.sortedBy { it.note }
//        notifyDataSetChanged()
//    }


    override fun getItemCount() = notes.size // сколько эл-тов будет в списке
}