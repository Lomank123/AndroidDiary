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
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkCallingOrSelfPermission
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.lomank.diary.R
import roomdatabase.ExtendedDiary

class DiaryListAdapter internal constructor(
    context: Context,
    private val listenerDeleteDiary : (ExtendedDiary) -> Unit,
    private val listenerOpenDiary : (ExtendedDiary) -> Unit,
    private val listenerEditDiary : (ExtendedDiary) -> Unit,
    private val listenerUpdateDiary : (ExtendedDiary) -> Unit
) : RecyclerView.Adapter<DiaryListAdapter.DiaryViewHolder>() {

    // По сути переменная inflater используется как метка на родительский XML,
    // которая используется в onCreateViewHolder
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context // откуда было запущено активити

    private var diaries = emptyList<ExtendedDiary>()   // Сюда будут сохраняться дневники

    private val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(mContext)

    private val permissionRequestCode = 11
    private val permissionsList = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

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
        // default layout
        private val layoutItemView : ConstraintLayout = itemView.findViewById(R.id.constraint_layout)
        private val diaryItemView: TextView = itemView.findViewById(R.id.textView_name)
        private val diaryDescriptionView: TextView = itemView.findViewById(R.id.textView_content)
        private val diaryImageView: ImageView = itemView.findViewById(R.id.imageView)
        private val diaryStarView: ImageView = itemView.findViewById(R.id.imageView_star)
        private val diaryImageButtonView : ImageButton = itemView.findViewById(R.id.imageButtonOptions)
        // expandable layout
        private val expandableLayoutItemView : ConstraintLayout = itemView.findViewById(R.id.expandable_layout)
        private val creationDateItemView : TextView = itemView.findViewById(R.id.creation_date_text2)
        private val lastEditDateItemView : TextView = itemView.findViewById(R.id.last_edit_date_text2)

        private var isExpanded = false

        // эта функция применяется для каждого члена RecyclerView т.к. вызывается в onBindViewHolder
        fun bindView(extDiary: ExtendedDiary) {
            creationDateItemView.text = extDiary.diary.creationDate.toString()
            lastEditDateItemView.text = extDiary.diary.lastEditDate

            itemView.setOnClickListener {
                listenerOpenDiary(extDiary)
            }
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

            // color
            if(extDiary.diary.color != null && prefs!!.getBoolean("color_check_diary", false))
                layoutItemView.setBackgroundColor(extDiary.diary.color!!)
            else
                layoutItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white))

            // photo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if(checkPermission(mContext, permissionsList)) {
                    if (extDiary.diary.img != null) {
                        diaryImageView.visibility = VISIBLE
                        val uriImage = Uri.parse(extDiary.diary.img)
                        diaryImageView.setImageURI(uriImage)
                    }
                    else
                        diaryImageView.setImageResource(R.drawable.logo)
                        //diaryImageView.visibility = GONE
                } else {
                    requestPermissions(mContext as Activity,permissionsList, permissionRequestCode)
                }
            // иконка со звездочкой (избранное)
            if (extDiary.diary.favorite)
                diaryStarView.visibility = VISIBLE
            else
                diaryStarView.visibility = GONE


            // обработчик долгих нажатий для вызова контекстного меню
            diaryImageButtonView.setOnClickListener{
                // Устанавливаем контекстное меню
                val popupMenu = PopupMenu(mContext, it)
                popupMenu.inflate(R.menu.menu_notes)
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
                // Устанавливаем обработчик нажатий на пункты контекстного меню
                popupMenu.setOnMenuItemClickListener { item ->
                    when(item.itemId) {     // сколько пунктов меню - столько и вариантов в when()
                        R.id.delete -> {    // удаление дневника
                            // Диалоговое окно
                            val dialog = MaterialDialog(mContext)
                            dialog.show{
                                title(R.string.dialog_delete)
                                message(R.string.dialog_check_delete)
                                positiveButton(R.string.dialog_yes) {
                                    listenerDeleteDiary(extDiary)
                                    Toast.makeText(mContext, mContext.resources.getString(R.string.dialog_deleted), Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                                negativeButton(R.string.dialog_no) {
                                    dialog.dismiss()
                                }
                            }
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
                            extDiary.diary.favorite = !extDiary.diary.favorite
                            if(extDiary.diary.favorite) {
                                diaryStarView.visibility = VISIBLE
                                Toast.makeText(mContext, mContext.resources.getString(R.string.add_favor),
                                    Toast.LENGTH_SHORT).show()
                            } else {
                                diaryStarView.visibility = GONE
                                Toast.makeText(mContext, mContext.resources.getString(R.string.del_favor),
                                    Toast.LENGTH_SHORT).show()
                            }
                            listenerUpdateDiary(extDiary)
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
            if(checkCallingOrSelfPermission(context, permissions[i]) == PermissionChecker.PERMISSION_DENIED) {
                allSuccess = false
            }
        }
        return allSuccess
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

    // TODO: Убрать если не понадобится
    //internal fun setFavoriteDiaries(diaries: List<ExtendedDiary>){
    //    val oldList = this.diaries
    //    val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(
    //        DiaryItemDiffCallBack(oldList, diaries.sortedBy { !it.diary.favorite }))
    //    this.diaries = diaries.sortedBy { !it.diary.favorite }
    //    diffResult.dispatchUpdatesTo(this)
    //    //notifyDataSetChanged()
    //}

    override fun getItemCount() = diaries.size // сколько эл-тов будет в списке
}