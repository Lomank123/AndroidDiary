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
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.lomank.diary.R
import roomdatabase.ExtendedDiary

class DiaryListAdapter internal constructor(
    context: Context,
    private val listenerDeleteDiary: (ExtendedDiary) -> Unit,
    private val listenerOpenDiary: (ExtendedDiary) -> Unit,
    private val listenerEditDiary: (ExtendedDiary) -> Unit,
    private val listenerUpdateDiary: (ExtendedDiary) -> Unit
) : RecyclerView.Adapter<DiaryListAdapter.DiaryViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context // откуда было запущено активити

    private var diaries = emptyList<ExtendedDiary>()   // Сюда будут сохраняться дневники

    private val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(mContext)

    class DiaryItemDiffCallBack(
        private var oldDiaryList: List<ExtendedDiary>,
        private var newDiaryList: List<ExtendedDiary>
    ): DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return (oldDiaryList[oldItemPosition].diary.id == newDiaryList[newItemPosition].diary.id)
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldDiaryList[oldItemPosition] == newDiaryList[newItemPosition]
        }
        override fun getOldListSize(): Int {
            return oldDiaryList.size
        }
        override fun getNewListSize(): Int {
            return newDiaryList.size
        }
    }

    // передаем сюда образец одного элемента списка
    // этот класс ХРАНИТ в себе то самое вью, в котором будут что-то менять
    inner class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // default layout
        private val layoutItemView : ConstraintLayout = itemView.findViewById(R.id.constraint_layout)
        private val diaryItemView: TextView = itemView.findViewById(R.id.editText_name)
        private val diaryDescriptionView: TextView = itemView.findViewById(R.id.textView_content)
        private val diaryImageView: ImageView = itemView.findViewById(R.id.imageView)
        private val diaryStarView: ImageView = itemView.findViewById(R.id.imageView_star)
        private val diarySettingsButtonView : ImageButton = itemView.findViewById(R.id.imageButton_options)
        private val diaryImageButtonViewDelete : ImageButton = itemView.findViewById(R.id.imageButton_delete)
        // expandable layout
        private val expandableLayoutItemView : ConstraintLayout = itemView.findViewById(R.id.expandable_layout)
        private val creationDateItemView : TextView = itemView.findViewById(R.id.creation_date_text2)
        private val lastEditDateItemView : TextView = itemView.findViewById(R.id.last_edit_date_text2)
        private val fullDescriptionText : TextView = itemView.findViewById(R.id.full_description_text)
        private val fullDescriptionItemView : TextView = itemView.findViewById((R.id.full_description))
        private val amountOfNotesText : TextView = itemView.findViewById(R.id.amount_of_notes_text)
        private val amountOfNotesCount : TextView = itemView.findViewById(R.id.amount_of_notes_count)

        // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
        fun bindView(extDiary: ExtendedDiary) {
            // Amount of notes
            amountOfNotesText.visibility = VISIBLE
            amountOfNotesCount.visibility = VISIBLE
            amountOfNotesCount.text = extDiary.notes.size.toString()
            // full description
            fullDescriptionItemView.visibility = VISIBLE
            fullDescriptionText.visibility = VISIBLE
            fullDescriptionText.text = extDiary.diary.content

            diaryItemView.text = extDiary.diary.name
            if (extDiary.diary.content != null)
                diaryDescriptionView.text = cutString(extDiary.diary.content!!)
            creationDateItemView.text = extDiary.diary.creationDate.toString()
            lastEditDateItemView.text = extDiary.diary.lastEditDate

            itemView.setOnClickListener {
                listenerOpenDiary(extDiary)
            }

            // color
            if(extDiary.diary.color != null && prefs!!.getBoolean("color_check_diary", true))
                layoutItemView.setBackgroundColor(extDiary.diary.color!!)
            else
                layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white))

            // иконка со звездочкой (избранное)
            if (extDiary.diary.favorite)
                diaryStarView.visibility = VISIBLE
            else
                diaryStarView.visibility = GONE

            // photo
            if (extDiary.diary.img != null && extDiary.diary.img != "") {
                // trying to set an image with Glide
                Glide.with(mContext).load(extDiary.diary.img).into(diaryImageView)
            } else {
                diaryImageView.setImageResource(R.drawable.logo)
            }

            diaryImageButtonViewDelete.setOnClickListener {
                val dialog = MaterialDialog(mContext)
                dialog.show {
                    title(R.string.dialog_delete)
                    message(R.string.dialog_check_delete)
                    positiveButton(R.string.dialog_yes) {
                        listenerDeleteDiary(extDiary)
                        dialog.dismiss()
                    }
                    negativeButton(R.string.dialog_no) {
                        dialog.dismiss()
                    }
                }
            }

            // Проверяем нужно ли показать информацию
            if (extDiary.diary.isExpanded) {
                diaryDescriptionView.visibility = GONE
                expandableLayoutItemView.visibility = VISIBLE
            } else {
                diaryDescriptionView.visibility = VISIBLE
                expandableLayoutItemView.visibility = GONE
            }

            fun setOptionsListener(view : View){
                // Устанавливаем контекстное меню
                val popupMenu = PopupMenu(mContext, view)
                popupMenu.inflate(R.menu.menu_context)
                // This button never used here
                popupMenu.menu.findItem(R.id.move).isVisible = false
                // если избранное - меняется текст кнопки добавить/удалить из избранных
                if(extDiary.diary.favorite) {
                    popupMenu.menu.findItem(R.id.bookmark).title = mContext.resources.
                    getString(R.string.remove_bookmark)
                }
                else {
                    popupMenu.menu.findItem(R.id.bookmark).title = mContext.resources.
                    getString(R.string.bookmark)
                }

                if(extDiary.diary.isExpanded)
                    popupMenu.menu.findItem(R.id.info).title = mContext.resources.
                    getString(R.string.hide_info)
                else
                    popupMenu.menu.findItem(R.id.info).title = mContext.resources.
                    getString(R.string.show_info)

                // Устанавливаем обработчик нажатий на пункты контекстного меню
                popupMenu.setOnMenuItemClickListener { item ->
                    when(item.itemId) {
                        R.id.open -> {
                            listenerOpenDiary(extDiary) // listener, описанный в NoteActivity
                            // Т.к. в этом обработчике нужно вернуть boolean, возвращаем true
                            true
                        }
                        R.id.edit -> {
                            listenerEditDiary(extDiary)
                            true
                        }
                        R.id.bookmark -> {
                            extDiary.diary.favorite = !extDiary.diary.favorite
                            if (extDiary.diary.favorite) {
                                diaryStarView.visibility = VISIBLE
                                Toast.makeText(
                                    mContext, mContext.resources.getString(R.string.add_favor),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                diaryStarView.visibility = GONE
                                Toast.makeText(
                                    mContext, mContext.resources.getString(R.string.del_favor),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            listenerUpdateDiary(extDiary)
                            true
                        }
                        R.id.info -> {
                            extDiary.diary.isExpanded = !extDiary.diary.isExpanded
                            if (extDiary.diary.isExpanded) {
                                diaryDescriptionView.visibility = GONE
                                expandableLayoutItemView.visibility = VISIBLE
                            } else {
                                diaryDescriptionView.visibility = VISIBLE
                                expandableLayoutItemView.visibility = GONE
                            }
                            listenerUpdateDiary(extDiary)
                            true
                        }
                        // Иначе вернем false (если when не сработал ни разу)
                        else -> false
                    }
                }
                popupMenu.show() // показываем меню
            }

            diarySettingsButtonView.setOnClickListener {
                setOptionsListener(it)
            }

            itemView.setOnLongClickListener {
                setOptionsListener(it)
                true
            }


        }
    }

    // создание ViewHolder (одинаково для всех RecyclerView)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский)
        val itemView = inflater.inflate(
            R.layout.recyclerview_layout_main, parent,
            false
        )
        return DiaryViewHolder(itemView)
    }

    // Устанавливает значение для каждого элемента RecyclerView
    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bindView(diaries[position])
    }

    // ВАЖНО: setDiaries вызывается в момент того, когда обсервер заметил изменения в записях
    // и чтобы зафиксировать эти изменения в RecyclerView, нужно передавать новый список сюда
    internal fun setDiaries(diaries: List<ExtendedDiary>) {
        val oldList = this.diaries
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(
            DiaryItemDiffCallBack(oldList, diaries))
        this.diaries = diaries      // обновляем внутренний список
        diffResult.dispatchUpdatesTo(this)
        //notifyDataSetChanged()
    }

    private fun cutString(str : String, num : Int = NUMBER_OF_SYMBOLS) : String {
        var count = 0
        var nCount = 0
        var newStr = ""
        for(i in str) {
            if (count == num) {
                newStr += "..."
                break
            }
            if(i.toString() == "\n") {
                nCount++
                if(nCount >= 3)
                    break
            }
            newStr += i
            count++
        }
        return newStr
    }

    override fun getItemCount() = diaries.size // сколько эл-тов будет в списке

    companion object {
        const val NUMBER_OF_SYMBOLS = 32
    }
}