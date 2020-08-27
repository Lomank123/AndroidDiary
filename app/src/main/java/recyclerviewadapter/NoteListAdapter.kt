package recyclerviewadapter

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.customListAdapter
import com.lomank.diary.R
import roomdatabase.ExtendedDiary
import roomdatabase.Note

class NoteListAdapter internal constructor(
    context: Context,
    private val listenerOpen : (Note) -> Unit,
    private val listenerDelete : (Note) -> Unit,
    private val listenerEdit : (Note) -> Unit,
    private val listenerUpdate : (Note) -> Unit
) : RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context // NoteActivity

    private val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(mContext)

    private var notes = emptyList<Note>()   // Сохраненная копия заметок

    private var extDiaryList = emptyList<ExtendedDiary>()

    private val permissionRequestCode = 11
    private val permissionsList = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

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
        private val noteItemView : TextView = itemView.findViewById(R.id.textView_name)
        private val noteDescriptionView : TextView = itemView.findViewById(R.id.textView_content)
        private val noteImageView : ImageView = itemView.findViewById(R.id.imageView)
        private val noteStarView : ImageView = itemView.findViewById(R.id.imageView_star)
        private val noteImageButtonView : ImageButton = itemView.findViewById(R.id.imageButtonOptions)
        // expandable layout
        private val expandableLayoutItemView : ConstraintLayout = itemView.findViewById(R.id.expandable_layout)
        private val creationDateItemView : TextView = itemView.findViewById(R.id.creation_date_text2)
        private val lastEditDateItemView : TextView = itemView.findViewById(R.id.last_edit_date_text2)

        private var isExpanded = false

        // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
        fun bindView(note: Note) {
            creationDateItemView.text = note.creationDate.toString()
            lastEditDateItemView.text = note.lastEditDate

            // Устанавливаем обработчик нажатий на элемент RecyclerView, при нажатии
            // будет вызываться первый listener (listenerOpen), который открывает заметку
            itemView.setOnClickListener {
                listenerOpen(note)
            }
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

            // photo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if(checkPermission(mContext, permissionsList)) {
                    if (note.img != null)
                    {
                        noteImageView.visibility = VISIBLE
                        val uriImage = Uri.parse(note.img)
                        noteImageView.setImageURI(uriImage)
                    }
                    else
                        noteImageView.setImageResource(R.drawable.blank_sheet)
                        //noteImageView.visibility = GONE
                } else {
                    ActivityCompat.requestPermissions(mContext as Activity, permissionsList, permissionRequestCode)
                }

            // иконка со звездочкой (избранное)
            if (note.favorite)
                noteStarView.visibility = VISIBLE
            else
                noteStarView.visibility = GONE

            if (note.color != null && prefs!!.getBoolean("color_check_note", false)) {
                layoutItemView.setBackgroundColor(note.color!!)
            }
            else
                layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white))

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
                            val dialog = MaterialDialog(mContext)
                            dialog.show{
                                title(R.string.dialog_delete)
                                message(R.string.dialog_check_delete)
                                positiveButton(R.string.dialog_yes) {
                                    listenerDelete(note)
                                    Toast.makeText(mContext, mContext.resources.getString(R.string.dialog_deleted), Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                                negativeButton(R.string.dialog_no) {
                                    dialog.dismiss()
                                }
                            }
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
                            isExpanded = !isExpanded
                            if(isExpanded) {
                                //expandableLayoutItemView.visibility = VISIBLE
                                expandableLayoutItemView.animate()
                                    .alpha(1f).setDuration(500).setListener(object : AnimatorListenerAdapter(){
                                        override fun onAnimationStart(animation: Animator?) {
                                            expandableLayoutItemView.visibility = VISIBLE
                                        }
                                    }).start()
                            } else {
                                //expandableLayoutItemView.visibility = GONE
                                expandableLayoutItemView.animate()
                                    .alpha(0f).setDuration(500).setListener(object : AnimatorListenerAdapter(){
                                        override fun onAnimationEnd(animation: Animator?) {
                                            expandableLayoutItemView.visibility = GONE
                                        }
                                    }).start()
                            }
                            true
                        }
                        // Иначе вернем false (если when не сработал ни разу)
                        else -> false
                    }
                }
                popupMenu.show() // показываем меню
            }
        }
    }

    private fun checkPermission(context : Context, permissions : Array<String>) : Boolean {
        var allSuccess = true
        for(i in permissions.indices) {
            if(PermissionChecker.checkCallingOrSelfPermission(
                    context,
                    permissions[i]
                ) == PermissionChecker.PERMISSION_DENIED) {
                allSuccess = false
            }
        }
        return allSuccess
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

    internal fun setNotes(notes: List<Note>, extDiariesList : List<ExtendedDiary>) {
        this.extDiaryList = extDiariesList
        val oldList = this.notes
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(
            NoteItemDiffCallBack(oldList, notes))
        this.notes = notes      // обновляем внутренний список
        diffResult.dispatchUpdatesTo(this)
        //notifyDataSetChanged()  // даем понять адаптеру, что были внесены изменения
    }

    internal fun setFavoriteNotes(notes: List<Note>, extDiariesList : List<ExtendedDiary>){
        this.extDiaryList = extDiariesList
        val oldList = this.notes
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(
            NoteItemDiffCallBack(oldList, notes.sortedBy { !it.favorite }))
        this.notes = notes.sortedBy { !it.favorite }
        diffResult.dispatchUpdatesTo(this)
        //notifyDataSetChanged()
    }

    override fun getItemCount() = notes.size // сколько эл-тов будет в списке
}