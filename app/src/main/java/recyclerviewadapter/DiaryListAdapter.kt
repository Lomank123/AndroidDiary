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

    // передаем сюда образец одного элемента списка
    // этот класс ХРАНИТ в себе то самое вью, в котором будут что-то менять
    inner class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val layoutItemView : RelativeLayout = itemView.findViewById(R.id.rellayout)
        // textView1 - отвечает за название
        private val diaryItemView: TextView = itemView.findViewById(R.id.textView1)
        // textView - отвечает за описание
        private val diaryDescriptionView: TextView = itemView.findViewById(R.id.textView)
        private val diaryDateView: TextView = itemView.findViewById(R.id.date_text)
        private val diaryImageView: ImageView = itemView.findViewById(R.id.imageView)
        private val diaryStarView: ImageView = itemView.findViewById(R.id.imageView_star)

        // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
        fun bindView(extDiary: ExtendedDiary) {
            //layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.primary_color1))
            // устанавливаем значения во вью
            diaryItemView.text = extDiary.diary.diary_name
            var count = 0
            var str = ""
            for(i in extDiary.diary.diary_content) { // записываем в строку первые 12 символов
                if (count == 12) {
                    str += ".." // если их > 12, добавляем многоточие и завершаем цикл
                    break
                }
                str += i
                count++
            }
            // в RecyclerView будут видны первые 16 символов текста заметки
            diaryDescriptionView.text = str // текст заметки
            diaryDateView.text = extDiary.diary.diary_date // записываем дату

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
            if (extDiary.diary.diaryImg != null)
            {
                val uriImage = Uri.parse(extDiary.diary.diaryImg)
                diaryImageView.setImageURI(uriImage)
            }
            else
                diaryImageView.setImageResource(R.mipmap.ic_launcher_round)
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
                            notifyDataSetChanged()
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
                // показываем меню
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
    //        android.R.anim.slide_in_left)
    //        viewToAnimate.animation = animation
    //    }
    //}

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

    // ВАЖНО: setWords вызывается в момент того, когда обсервер заметил изменения в записях
    // и чтобы зафиксировать эти изменения в RecyclerView, нужно передавать новый список сюда
    internal fun setDiaries(diaries: List<ExtendedDiary>) {
        this.diaries = diaries      // обновляем внутренний список
        // notifyDataSetChanged() даст сигнал о том, что данные изменились
        // и нужно их обновить и в самом RecycleView
        notifyDataSetChanged()
    }
    internal fun setFavoriteDiaries(diaries: List<ExtendedDiary>){
        this.diaries = diaries.sortedBy { !it.diary.favorite }
        notifyDataSetChanged()
    }

    override fun getItemCount() = diaries.size // сколько эл-тов будет в списке
}