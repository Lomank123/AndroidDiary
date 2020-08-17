package recyclerviewadapter

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.project3.R
import roomdatabase.Note

class NoteListAdapter internal constructor(
    context: Context,
    private val listenerOpen : (Note) -> Unit,
    private val listenerDelete : (Note) -> Unit,
    private val listenerEdit : (Note) -> Unit,
    private val listenerBookmark : (Note) -> Unit
) : RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context // NoteActivity

    private val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(mContext)

    private var notes = emptyList<Note>()   // Сохраненная копия заметок

    class NoteItemDiffCallBack(
        private var oldNoteList : List<Note>,
        private var newNoteList : List<Note>
    ): DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return (oldNoteList[oldItemPosition].id == newNoteList[newItemPosition].id)
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldNoteList[oldItemPosition] == newNoteList[newItemPosition]
        }
        override fun getOldListSize(): Int {
            return oldNoteList.size
        }
        override fun getNewListSize(): Int {
            return newNoteList.size
        }
    }

    // передаем сюда образец одного элемента списка
    // этот класс ХРАНИТ в себе то самое вью, в котором будут что-то менять
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val layoutItemView : RelativeLayout = itemView.findViewById(R.id.relative_layout)
        // textView1 - вью из файла recyclerview_layout.xml, отвечает за название
        private val noteItemView : TextView = itemView.findViewById(R.id.textView_name)
        // textView - вью из файла recyclerview_layout.xml, отвечает за описание
        private val noteDescriptionView : TextView = itemView.findViewById(R.id.textView_content)
        private val noteImageView : ImageView = itemView.findViewById(R.id.imageView)
        private val noteDateView : TextView = itemView.findViewById(R.id.date_text)
        private val noteStarView : ImageView = itemView.findViewById(R.id.imageView_star)
        private val noteImageButtonView : ImageButton = itemView.findViewById(R.id.imageButtonOptions)

        // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
        fun bindView(note: Note) {
            // устанавливаем значения во вью
            noteItemView.text = note.name // название заметки
            // Лимит на кол-во символов в тексте заметки для отображения: 12
            var count = 0
            var str = ""
            for(i in note.content) { // записываем в строку первые 12 символов
                if (count == 12) {
                    str += ".." // если их > 12, добавляем многоточие и завершаем цикл
                    break
                }
                str += i
                count++
            }
            // в RecyclerView будут видны первые 16 символов текста заметки
            noteDescriptionView.text = str // текст заметки
            noteDateView.text = note.lastEditDate // дата
            if (note.img != null)
            {
                noteImageView.visibility = VISIBLE
                val uriImage = Uri.parse(note.img)
                noteImageView.setImageURI(uriImage)
            }
            else
                noteImageView.setImageResource(R.drawable.blank_sheet)
                //noteImageView.visibility = GONE

            // иконка со звездочкой (избранное)
            if (note.favorite)
                noteStarView.visibility = VISIBLE
            else
                noteStarView.visibility = GONE

            if (prefs!!.getBoolean("color_check_note", false)) {
                when (note.color)
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
                listenerOpen(note)
            }
            // обработчик долгих нажатий для вызова контекстного меню
            noteImageButtonView.setOnClickListener{
                // Устанавливаем контекстное меню
                val popupMenu = PopupMenu(mContext, it)
                popupMenu.inflate(R.menu.menu_notes)
                // установка нужной надписи на пункт меню
                if(note.favorite) {
                    popupMenu.menu.findItem(R.id.bookmark).title = mContext.resources.
                    getString(R.string.remove_bookmark)
                }
                else {
                    popupMenu.menu.findItem(R.id.bookmark).title = mContext.resources.
                    getString(R.string.bookmark)
                }
                // Делает кнопку избранного невидимой (не нужно)
                //popupMenu.menu.findItem(R.id.bookmark).isVisible = false

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
                            listenerOpen(note)  // открытие записи
                            // Т.к. в этом обработчике нужно вернуть boolean, возвращаем true
                            true
                        }
                        R.id.edit -> {
                            listenerEdit(note)
                            true
                        }
                        R.id.bookmark -> {
                            listenerBookmark(note)
                            if(note.favorite)
                                noteStarView.visibility = VISIBLE
                            else
                                noteStarView.visibility = GONE
                            true
                        }
                        // Иначе вернем false (если when не сработал ни разу)
                        else -> false
                    }
                }
                popupMenu.show() // показываем меню
                // Т.к. в LongClickListener нужно вернуть boolean, возвращаем его
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский)
        val itemView = inflater.inflate(R.layout.recyclerview_layout, parent,
            false)
        return NoteViewHolder(itemView)
    }

    // Устанавливает значение для каждого элемента RecyclerView
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bindView(notes[position])
    }

    internal fun setNotes(notes: List<Note>) {
        val oldList = this.notes
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(
            NoteItemDiffCallBack(oldList, notes))
        this.notes = notes      // обновляем внутренний список
        diffResult.dispatchUpdatesTo(this)
        //notifyDataSetChanged()  // даем понять адаптеру, что были внесены изменения
    }

    internal fun setFavoriteNotes(notes: List<Note>){
        val oldList = this.notes
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(
            NoteItemDiffCallBack(oldList, notes.sortedBy { !it.favorite }))
        this.notes = notes.sortedBy { !it.favorite }
        diffResult.dispatchUpdatesTo(this)
        //notifyDataSetChanged()
    }

    override fun getItemCount() = notes.size // сколько эл-тов будет в списке
}