package recyclerviewadapter

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project3.R
import roomdatabase.Note

class NoteListAdapter internal constructor(
    context: Context,
    private val listenerOpen : (Note) -> Unit,  // похоже на какой-то template для функций
    private val listenerDelete : (Note) -> Unit,
    private val listenerEdit : (Note) -> Unit
) : RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context // NoteActivity

    private val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(mContext)

    private var notes = emptyList<Note>()   // Сохраненная копия заметок
    //private var notesMapped = mutableMapOf<Long, List<Note>>()

    // передаем сюда образец одного элемента списка
    // этот класс ХРАНИТ в себе то самое вью, в котором будут что-то менять
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val layoutItemView : RelativeLayout = itemView.findViewById(R.id.rellayout)
        // textView1 - вью из файла recyclerview_layout.xml, отвечает за название
        private val noteItemView : TextView = itemView.findViewById(R.id.textView1)
        // textView - вью из файла recyclerview_layout.xml, отвечает за описание
        private val noteDescriptionView : TextView = itemView.findViewById(R.id.textView)

        private val noteImageView : ImageView = itemView.findViewById(R.id.imageView)

        private val noteDateView : TextView = itemView.findViewById(R.id.date_text)

        // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
        fun bindView(note: Note, listener : (Note) -> Unit) {

            // устанавливаем значения во вью
            noteItemView.text = note.note // название заметки

            // Лимит на кол-во символов в тексте заметки для отображения: 12
            var count = 0
            var str = ""
            for(i in note.text) { // записываем в строку первые 12 символов
                if (count == 12) {
                    str += ".." // если их > 12, добавляем многоточие и завершаем цикл
                    break
                }
                str += i
                count++
            }
            // в RecyclerView будут видны первые 16 символов текста заметки
            noteDescriptionView.text = str // текст заметки
            noteDateView.text = note.dateNote // дата

            if (note.imgNote != null)
            {
                val uriImage = Uri.parse(note.imgNote)
                noteImageView.setImageURI(uriImage)
            }
            else
                noteImageView.setImageResource(R.mipmap.ic_launcher_round)

            if (prefs!!.getBoolean("color_check_note", false)) {
                when (note.colorNote)
                {
                    "green" ->
                        layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext,
                            R.color.green))
                    "yellow" ->
                        layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext,
                            R.color.yellow))
                    "blue" ->
                        layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext,
                            R.color.blue))
                    "grass" ->
                        layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext,
                            R.color.grass))
                    "purple" ->
                        layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext,
                            R.color.purple))
                }
            }
            else
                layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white))

            // Устанавливаем обработчик нажатий на элемент RecyclerView, при нажатии
            // будет вызываться первый listener (listenerOpen), который открывает заметку
            itemView.setOnClickListener {
                listener(note)
            }

            // обработчик долгих нажатий для вызова контекстного меню
            itemView.setOnLongClickListener{

                // Устанавливаем контекстное меню
                val popupMenu = PopupMenu(mContext, it)
                // TODO: Изменить menu_notes.xml
                // TODO: Либо "спрятать" кнопку "Add star" во всплывающем меню
                // Устанавливаем обработчик нажатий на пункты контекстного меню
                popupMenu.setOnMenuItemClickListener { item ->
                    when(item.itemId) {     // сколько пунктов меню - столько и вариантов в when()
                        R.id.delete -> {
                            val deleteDialog = AlertDialog.Builder(mContext)
                            deleteDialog.setTitle(mContext.resources.
                            getString(R.string.dialog_delete))
                            deleteDialog.setMessage(mContext.resources.
                            getString(R.string.dialog_check_delete_note))
                            deleteDialog.setPositiveButton(mContext.resources.
                            getString(R.string.dialog_yes))
                            { _, _ ->
                                listenerDelete(note)  // удаление записи
                                notifyDataSetChanged()
                                Toast.makeText(mContext,
                                    mContext.resources.getString(R.string.dialog_deleted),
                                    Toast.LENGTH_SHORT).show()
                            }
                            deleteDialog.setNegativeButton(mContext.resources.
                            getString(R.string.dialog_no))
                            { dialog, _ ->
                                dialog.dismiss()
                            }
                            deleteDialog.show()
                            true
                        }
                        R.id.open -> {
                            listener(note)  // открытие записи
                            // Т.к. в этом обработчике нужно вернуть boolean, возвращаем true
                            true
                        }
                        R.id.edit -> {
                            listenerEdit(note)
                            notifyDataSetChanged()
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

    //private fun setAnimation(viewToAnimate : View)
    //{
    //    if(viewToAnimate.animation == null)
    //    {
    //        val animation = AnimationUtils.loadAnimation(viewToAnimate.context,
    //            android.R.anim.slide_in_left)
    //        viewToAnimate.animation = animation
    //    }
    //}

    // создание ViewHolder (одинаково для всех RecyclerView)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский)
        val itemView = inflater.inflate(R.layout.recyclerview_layout, parent,
            false)
        return NoteViewHolder(itemView)
    }

    // Устанавливает значение для каждого элемента RecyclerView
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bindView(notes[position], listenerOpen)
    }

    internal fun setNotes(notes: List<Note>) {
        this.notes = notes      // обновляем внутренний список
        notifyDataSetChanged()  // даем понять адаптеру, что были внесены изменения
    }

    override fun getItemCount() = notes.size // сколько эл-тов будет в списке
}