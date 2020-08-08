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
import repository.NotesAndWords

// internal constructor means, that a constructor of an internal class is only
// visible within the same module, module means this full project

class WordListAdapter internal constructor(
    context: Context,
    private val listenerDeleteWord : (NotesAndWords) -> Unit,
    private val listenerOpenWord : (NotesAndWords) -> Unit,
    private val listenerEditWord : (NotesAndWords) -> Unit,
    private val listenerBookmark : (NotesAndWords) -> Unit
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context // откуда было запущено активити

    private var words = emptyList<NotesAndWords>()   // Сюда будут сохраняться дневники

    private val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(mContext)

    // передаем сюда образец одного элемента списка
    // этот класс ХРАНИТ в себе то самое вью, в котором будут что-то менять
    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val layoutItemView : RelativeLayout = itemView.findViewById(R.id.rellayout)
        // textView1 - отвечает за название
        private val wordItemView: TextView = itemView.findViewById(R.id.textView1)
        // textView - отвечает за описание
        private val wordDescriptionView: TextView = itemView.findViewById(R.id.textView)
        private val wordDateView: TextView = itemView.findViewById(R.id.date_text)
        private val wordImageView: ImageView = itemView.findViewById(R.id.imageView)
        private val wordStarView: ImageView = itemView.findViewById(R.id.imageView_star)

        // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
        fun bindView(diary: NotesAndWords) {
            //layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.primary_color1))
            // устанавливаем значения во вью
            wordItemView.text = diary.word.word
            var count = 0
            var str = ""
            for(i in diary.word.description) { // записываем в строку первые 12 символов
                if (count == 12) {
                    str += ".." // если их > 12, добавляем многоточие и завершаем цикл
                    break
                }
                str += i
                count++
            }
            // в RecyclerView будут видны первые 16 символов текста заметки
            wordDescriptionView.text = str // текст заметки
            wordDateView.text = diary.word.date // записываем дату

            if (prefs!!.getBoolean("color_check_diary", false)) {
                when (diary.word.color)
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
            if (diary.word.img != null)
            {
                val uriImage = Uri.parse(diary.word.img)
                wordImageView.setImageURI(uriImage)
            }
            else
                wordImageView.setImageResource(R.mipmap.ic_launcher_round)
            // иконка со звездочкой (избранное)
            if (diary.word.isFavorite)
                wordStarView.visibility = VISIBLE
            else
                wordStarView.visibility = GONE

            // Устанавливаем обработчик нажатий на элемент RecyclerView, при нажатии
            // будет вызываться первый listener, который открывает дневник
            itemView.setOnClickListener {
                listenerOpenWord(diary)
            }
            // обработчик долгих нажатий для вызова контекстного меню
            itemView.setOnLongClickListener{
                // Устанавливаем контекстное меню
                val popupMenu = PopupMenu(mContext, it)
                popupMenu.inflate(R.menu.menu_notes)
                // если избранное - меняется текст кнопки добавить/удалить из избранных
                if(diary.word.isFavorite) {
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
                                listenerDeleteWord(diary) // вызываем listener
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
                            listenerOpenWord(diary) // listener, описанный в NoteActivity
                            // Т.к. в этом обработчике нужно вернуть boolean, возвращаем true
                            true
                        }
                        R.id.edit -> {
                            listenerEditWord(diary)
                            true
                        }
                        R.id.bookmark -> {
                            listenerBookmark(diary)
                            notifyDataSetChanged()
                            if(diary.word.isFavorite)
                                wordStarView.visibility = VISIBLE
                            else
                                wordStarView.visibility = GONE
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        // добавляет контент(XML) из 1-го аргумента, и помещает во второй (родительский)
        val itemView = inflater.inflate(R.layout.recyclerview_layout, parent,
            false)
        return WordViewHolder(itemView)
    }

    // Устанавливает значение для каждого элемента RecyclerView
    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bindView(words[position])
    }

    // ВАЖНО: setWords вызывается в момент того, когда обсервер заметил изменения в записях
    // и чтобы зафиксировать эти изменения в RecyclerView, нужно передавать новый список сюда
    internal fun setWords(words: List<NotesAndWords>) {
        this.words = words      // обновляем внутренний список
        // notifyDataSetChanged() даст сигнал о том, что данные изменились
        // и нужно их обновить и в самом RecycleView
        notifyDataSetChanged()
    }
    internal fun setFavoriteWords(words: List<NotesAndWords>){
        this.words = words.sortedBy { !it.word.isFavorite }
        notifyDataSetChanged()
    }

    override fun getItemCount() = words.size // сколько эл-тов будет в списке
}