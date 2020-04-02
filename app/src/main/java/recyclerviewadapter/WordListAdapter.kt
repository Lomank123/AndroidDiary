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
import roomdatabase.Word

// internal constructor means, that a constructor of an internal class is only
// visible within the same module, module means this full project

class WordListAdapter internal constructor(
    context: Context,
    private val listenerDeleteWord : (Word) -> Unit,
    private val listenerOpenWord : (Word) -> Unit,
    private val listenerEditWord : (Word) -> Unit
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context // откуда было запущено активити

    private var words = emptyList<Word>()   // Сюда будут сохраняться дневники

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
        fun bindView(word: Word, listener : (Word) -> Unit) {

            //layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.primary_color1))

            // устанавливаем значения во вью
            wordItemView.text = word.word
            wordDescriptionView.text = word.description // записываем в TextView строку (описание)
            wordDateView.text = word.date // записываем дату

            if (prefs!!.getBoolean("color_check_diary", false)) {
                when (word.color)
                {
                    "pink" ->
                        layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext,
                            R.color.pink))
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
            if (word.img != null)
            {
                val uriImage = Uri.parse(word.img)
                wordImageView.setImageURI(uriImage)
            }
            else
                wordImageView.setImageResource(R.mipmap.ic_launcher_round)

            if(word.isFavorite)
                wordStarView.visibility = VISIBLE
            else
                wordStarView.visibility = GONE


            // Устанавливаем обработчик нажатий на элемент RecyclerView, при нажатии
            // будет вызываться первый listener, который открывает дневник
            itemView.setOnClickListener {
                listener(word)
            }
            // обработчик долгих нажатий для вызова контекстного меню
            itemView.setOnLongClickListener{
                // Устанавливаем контекстное меню
                val popupMenu = PopupMenu(mContext, it)
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
                                listenerDeleteWord(word) // вызываем listener
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
                            listener(word) // listener, описанный в NoteActivity
                            // Т.к. в этом обработчике нужно вернуть boolean, возвращаем true
                            true
                        }
                        R.id.edit -> {
                            listenerEditWord(word)
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
        holder.bindView(words[position], listenerOpenWord)
    }

    // ВАЖНО: setWords вызывается в момент того, когда обсервер заметил изменения в записях
    // и чтобы зафиксировать эти изменения в RecyclerView, нужно передавать новый список сюда
    internal fun setWords(words: List<Word>) {
        this.words = words      // обновляем внутренний список
        // notifyDataSetChanged() даст сигнал о том, что данные изменились
        // и нужно их обновить и в самом RecycleView
        notifyDataSetChanged()
    }
    internal fun setFavoriteWords(words: List<Word>){
        this.words = words.sortedBy { !it.isFavorite }
        notifyDataSetChanged()
    }

    override fun getItemCount() = words.size // сколько эл-тов будет в списке
}