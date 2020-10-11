package recyclerviewadapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.bumptech.glide.Glide
import com.lomank.diary.R
import other.MaterialDialogAdapter
import roomdatabase.ExtendedDiary
import roomdatabase.Note

class NoteListAdapter internal constructor(
    context: Context,
    private val listenerOpen : (Note) -> Unit,
    private val listenerDelete : (Note) -> Unit,
    private val listenerUpdate : (Note) -> Unit
) : RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context // NoteActivity

    private val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(mContext)

    private var notes = emptyList<Note>()   // Сохраненная копия заметок

    private var extDiaryList = emptyList<ExtendedDiary>()

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

        private val layoutItemView : ConstraintLayout = itemView.findViewById(R.id.constraint_layout)
        private val noteItemView : TextView = itemView.findViewById(R.id.editText_name)
        private val noteDescriptionView : TextView = itemView.findViewById(R.id.textView_content)
        private val noteImageView : ImageView = itemView.findViewById(R.id.imageView)
        private val noteStarView : ImageView = itemView.findViewById(R.id.imageView_star)

        private val noteImageButtonViewDelete : ImageButton = itemView.findViewById(R.id.imageButton_delete)
        // expandable layout
        private val expandableLayoutItemView : ConstraintLayout = itemView.findViewById(R.id.expandable_layout)
        private val creationDateItemView : TextView = itemView.findViewById(R.id.creation_date_text2)
        private val lastEditDateItemView : TextView = itemView.findViewById(R.id.last_edit_date_text2)
        private val imageViewMic : ImageView = itemView.findViewById(R.id.imageView_mic)

        // images
        private val imageLayout : TableLayout = itemView.findViewById(R.id.tableLayout_images)
        private val image1 : ImageView = itemView.findViewById(R.id.image1)
        private val image2 : ImageView = itemView.findViewById(R.id.image2)
        private val image3 : ImageView = itemView.findViewById(R.id.image3)

        // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
        fun bindView(note: Note) {
            noteImageView.setImageResource(R.drawable.empty_note_photo)
            noteItemView.text = note.name
            noteDescriptionView.text = cutString(note.content)
            creationDateItemView.text = note.creationDate.toString()
            lastEditDateItemView.text = note.lastEditDate

            // Устанавливаем обработчик нажатий на элемент RecyclerView, при нажатии
            // будет вызываться первый listener (listenerOpen), который открывает заметку
            itemView.setOnClickListener {
                listenerOpen(note)
            }

            // showing images
            if(prefs!!.getBoolean("images_show_note", false)){
                val viewList = arrayListOf(image1, image2, image3)
                if (note.images != null) {
                    if(note.images!!.isNotEmpty()) {
                        imageLayout.visibility = VISIBLE
                        for(i in note.images!!.indices) {
                            // setting image
                            viewList[i].visibility = VISIBLE
                            Glide.with(mContext).load(note.images!![i]).override(800, 1000).into(viewList[i])
                        }
                    } else {
                        imageLayout.visibility = GONE
                    }
                    for(i in note.images!!.size until viewList.size){
                        viewList[i].visibility = GONE
                    }
                    setLayoutParams(note, image1, image2)
                } else {
                    imageLayout.visibility = GONE
                }
            } else {
                imageLayout.visibility = GONE
            }



            // иконка со звездочкой (избранное)
            if (note.favorite)
                noteStarView.visibility = VISIBLE
            else
                noteStarView.visibility = GONE

            // if there is a voice note inside
            if(note.voice)
                imageViewMic.visibility = VISIBLE
            else
                imageViewMic.visibility = GONE

            // color
            if (note.color != null && prefs.getBoolean("color_check_note", false)) {
                layoutItemView.setBackgroundColor(note.color!!)
            }
            else
                layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white))

            noteImageButtonViewDelete.setOnClickListener {
                val dialog = MaterialDialog(mContext)
                dialog.show{
                    title(R.string.dialog_delete)
                    message(R.string.dialog_check_delete_note)
                    positiveButton(R.string.dialog_yes) {
                        listenerDelete(note)
                        dialog.dismiss()
                    }
                    negativeButton(R.string.dialog_no) {
                        dialog.dismiss()
                    }
                }
            }

            // Проверяем нужно ли показать информацию
            if (note.isExpanded) {
                //noteDescriptionView.visibility = GONE
                expandableLayoutItemView.visibility = VISIBLE
            } else {
                //noteDescriptionView.visibility = VISIBLE
                expandableLayoutItemView.visibility = GONE
            }

            itemView.setOnLongClickListener {
                // Устанавливаем контекстное меню
                val popupMenu = PopupMenu(mContext, it)
                popupMenu.inflate(R.menu.menu_context)

                // установка нужной надписи на пункт меню
                if(note.favorite)
                    popupMenu.menu.findItem(R.id.bookmark).title = mContext.resources.
                    getString(R.string.remove_bookmark)
                else
                    popupMenu.menu.findItem(R.id.bookmark).title = mContext.resources.
                    getString(R.string.bookmark)

                if(note.isExpanded)
                    popupMenu.menu.findItem(R.id.info).title = mContext.resources.
                    getString(R.string.hide_info)
                else
                    popupMenu.menu.findItem(R.id.info).title = mContext.resources.
                    getString(R.string.show_info)

                popupMenu.menu.findItem(R.id.edit).isVisible = false
                popupMenu.setOnMenuItemClickListener { item ->
                    when(item.itemId) {
                        R.id.open -> {
                            listenerOpen(note)
                            // Т.к. в этом обработчике нужно вернуть boolean, возвращаем true
                            true
                        }
                        R.id.bookmark -> {
                            note.favorite = !note.favorite
                            if(note.favorite) {
                                noteStarView.visibility = VISIBLE
                                Toast.makeText(mContext, mContext.resources.getString(R.string.add_favor),
                                    Toast.LENGTH_SHORT).show()
                            } else {
                                noteStarView.visibility = GONE
                                Toast.makeText(mContext, mContext.resources.getString(R.string.del_favor),
                                    Toast.LENGTH_SHORT).show()
                            }
                            listenerUpdate(note)
                            true
                        }
                        R.id.move -> {
                            val dialog = MaterialDialog(mContext)
                            val anotherAdapter = MaterialDialogAdapter(extDiaryList
                            ) {diary ->
                                note.parentId = diary.id
                                note.isExpanded = false
                                listenerUpdate(note)
                                dialog.dismiss()
                            }
                            dialog.show{
                                title(R.string.move_btn)
                                customListAdapter(anotherAdapter, LinearLayoutManager(mContext))
                                negativeButton {
                                    dialog.dismiss()
                                }
                            }
                            true
                        }
                        R.id.info -> {
                            note.isExpanded = !note.isExpanded
                            if (note.isExpanded) {
                                //noteDescriptionView.visibility = GONE
                                expandableLayoutItemView.visibility = VISIBLE
                            } else {
                                //noteDescriptionView.visibility = VISIBLE
                                expandableLayoutItemView.visibility = GONE
                            }
                            listenerUpdate(note)
                            true
                        }
                        // Иначе вернем false (если when не сработал ни разу)
                        else -> false
                    }
                }
                popupMenu.show() // показываем меню
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский)
        val itemView = inflater.inflate(R.layout.recyclerview_layout_main, parent,
            false)
        return NoteViewHolder(itemView)
    }

    // Устанавливает значение для каждого элемента RecyclerView
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bindView(notes[position])
    }

    internal fun setNotes(notes: List<Note>, extDiariesList : List<ExtendedDiary>) {
        this.extDiaryList = extDiariesList
        val oldList = this.notes
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(
            NoteItemDiffCallBack(oldList, notes))
        this.notes = notes      // обновляем внутренний список
        diffResult.dispatchUpdatesTo(this)
        //notifyDataSetChanged()  // даем понять адаптеру, что были внесены изменения
    }

    // cuts string to num symbols
    private fun cutString(str : String, num : Int = NUMBER_OF_SYMBOLS) : String {
        var count = 0
        var nCount = 0
        var newStr = ""
        for(i in str) {
            if (count == num) {
                newStr += "..."
                break
            }
            // if number of "\n" symbols >= 3 we cut the string
            if(i.toString() == "\n") {
                nCount++
                if(nCount >= 3) {
                    newStr += "..."
                    break
                }
            }
            newStr += i
            count++
        }
        return newStr
    }

    private fun setLayoutParams(note : Note, image1 : ImageView, image2 : ImageView) {
        if(note.images!!.size == 1) {
            val params1 = (image1.layoutParams as ViewGroup.MarginLayoutParams)
            params1.setMargins(0, 0, 0, 0)
            image1.layoutParams = params1
        } else {
            if(note.images!!.size == 2) {
                val params1 = (image1.layoutParams as ViewGroup.MarginLayoutParams)
                params1.setMargins(0, 0, 9, 0)
                image1.layoutParams = params1
                val params2 = (image2.layoutParams as ViewGroup.MarginLayoutParams)
                params2.setMargins(0, 0, 0, 0)
                image2.layoutParams = params2
            } else { // 3
                val params1 = (image1.layoutParams as ViewGroup.MarginLayoutParams)
                params1.setMargins(0, 0, 0, 0)
                image1.layoutParams = params1
                val params2 = (image2.layoutParams as ViewGroup.MarginLayoutParams)
                params2.setMargins(9, 0, 9, 0)
                image2.layoutParams = params2
            }
        }
    }

    override fun getItemCount() = notes.size // сколько эл-тов будет в списке

    companion object {
        const val NUMBER_OF_SYMBOLS = 32
    }
}