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
import roomdatabase.ExtendedDiary

// internal constructor means, that a constructor of an internal class is only
// visible within the same module, module means this full project

class DiaryListAdapter internal constructor(
    context: Context,
    private val listenerDeleteDiary : (ExtendedDiary) -> Unit,
    private val listenerOpenDiary : (ExtendedDiary) -> Unit,
    private val listenerEditDiary : (ExtendedDiary) -> Unit,
    private val listenerBookmarkDiary : (ExtendedDiary) -> Unit
) : RecyclerView.Adapter<DiaryListAdapter.DiaryViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context // откуда было запущено активити

    private var diaries = emptyList<ExtendedDiary>()   // Сюда будут сохраняться дневники

    private val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(mContext)

    class DiaryItemDiffCallBack(
        private var oldDiaryList : List<ExtendedDiary>,
        private var newDiaryList : List<ExtendedDiary>
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

        private val layoutItemView : RelativeLayout = itemView.findViewById(R.id.relative_layout)
        // textView1 - отвечает за название
        private val diaryItemView: TextView = itemView.findViewById(R.id.textView_name)
        // textView - отвечает за описание
        private val diaryDescriptionView: TextView = itemView.findViewById(R.id.textView_content)
        private val diaryDateView: TextView = itemView.findViewById(R.id.date_text)
        private val diaryImageView: ImageView = itemView.findViewById(R.id.imageView)
        private val diaryStarView: ImageView = itemView.findViewById(R.id.imageView_star)

        // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
        fun bindView(extDiary: ExtendedDiary) {
            //layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.primary_color1))
            // устанавливаем значения во вью
            diaryItemView.text = extDiary.diary.name
            var count = 0
            var str = ""
            for(i in extDiary.diary.content) { // записываем в строку первые 12 символов
                if (count == 12) {
                    str += ".." // если их > 12, добавляем многоточие и завершаем цикл
                    break
                }
                str += i
                count++
            }
            // в RecyclerView будут видны первые 16 символов текста заметки
            diaryDescriptionView.text = str // текст заметки
            diaryDateView.text = extDiary.diary.lastEditDate // записываем дату

            if (prefs!!.getBoolean("color_check_diary", false)) {
                when (extDiary.diary.color)
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

            // установка фото
            if (extDiary.diary.img != null)
            {
                diaryImageView.visibility = VISIBLE
                val uriImage = Uri.parse(extDiary.diary.img)
                diaryImageView.setImageURI(uriImage)
            }
            else
                diaryImageView.setImageResource(R.drawable.logo)
                //diaryImageView.visibility = GONE
            // иконка со звездочкой (избранное)
            if (extDiary.diary.favorite)
                diaryStarView.visibility = VISIBLE
            else
                diaryStarView.visibility = GONE

            // Устанавливаем обработчик нажатий на элемент RecyclerView, при нажатии
            // будет вызываться первый listener, который открывает дневник
            itemView.setOnClickListener {
                listenerOpenDiary(extDiary)
            }
            // обработчик долгих нажатий для вызова контекстного меню
            itemView.setOnLongClickListener{
                // Устанавливаем контекстное меню
                val popupMenu = PopupMenu(mContext, it)
                popupMenu.inflate(R.menu.menu_notes)
                // если избранное - меняется текст кнопки добавить/удалить из избранных
                if(extDiary.diary.favorite) {
                    popupMenu.menu.findItem(R.id.bookmark).title = mContext.resources.
                    getString(R.string.remove_bookmark)
                }
                else {
                    popupMenu.menu.findItem(R.id.bookmark).title = mContext.resources.
                    getString(R.string.bookmark)
                }
                // Устанавливаем обработчик нажатий на пункты контекстного меню
                popupMenu.setOnMenuItemClickListener { item ->
                    when(item.itemId) {     // сколько пунктов меню - столько и вариантов в when()
                        R.id.delete -> {    // удаление дневника
                            // Диалоговое окно
                            val deleteDialog = AlertDialog.Builder(mContext)
                            deleteDialog.setTitle(mContext.resources.
                            getString(R.string.dialog_delete))
                            deleteDialog.setMessage(mContext.resources.
                            getString(R.string.dialog_check_delete))
                            deleteDialog.setPositiveButton(mContext.resources.
                            getString(R.string.dialog_yes)){ _, _ ->
                                listenerDeleteDiary(extDiary) // вызываем listener
                                Toast.makeText(mContext, mContext.resources.
                                getString(R.string.dialog_deleted), Toast.LENGTH_SHORT).show()
                            }
                            deleteDialog.setNegativeButton(mContext.resources.
                            getString(R.string.dialog_no))
                            { dialog, _ ->
                                dialog.dismiss()
                            }
                            deleteDialog.show()
                            true
                        }
                        R.id.open -> { // открытие дневника
                            listenerOpenDiary(extDiary) // listener, описанный в NoteActivity
                            // Т.к. в этом обработчике нужно вернуть boolean, возвращаем true
                            true
                        }
                        R.id.edit -> {
                            listenerEditDiary(extDiary)
                            true
                        }
                        R.id.bookmark -> {
                            listenerBookmarkDiary(extDiary)
                            if(extDiary.diary.favorite)
                                diaryStarView.visibility = VISIBLE
                            else
                                diaryStarView.visibility = GONE
                            true
                        }
                        // Иначе вернем false (если when не сработал ни разу)
                        else -> false
                    }
                }
                popupMenu.show() // показываем меню
                // Т.к. в LongClickListener нужно вернуть boolean, возвращаем его
                return@setOnLongClickListener true
            }
        }
    }

    // создание ViewHolder (одинаково для всех RecyclerView)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский)
        val itemView = inflater.inflate(R.layout.recyclerview_layout, parent,
            false)
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
    internal fun setFavoriteDiaries(diaries: List<ExtendedDiary>){
        val oldList = this.diaries
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(
            DiaryItemDiffCallBack(oldList, diaries.sortedBy { !it.diary.favorite }))
        this.diaries = diaries.sortedBy { !it.diary.favorite }
        diffResult.dispatchUpdatesTo(this)
        //notifyDataSetChanged()
    }

    override fun getItemCount() = diaries.size // сколько эл-тов будет в списке
}