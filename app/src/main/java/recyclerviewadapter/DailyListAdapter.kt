package recyclerviewadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.lomank.diary.R
import roomdatabase.DailyListItem


class DailyListAdapter internal constructor(
    context: Context,
    private val listenerUpdateList : (DailyListItem) -> Unit,
    private val listenerDelete : (DailyListItem) -> Unit
) : RecyclerView.Adapter<DailyListAdapter.DailyListViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context

    private var dailyListItems = emptyList<DailyListItem>()

    class DailyListItemDiffCallBack(
        private var oldNoteList : List<DailyListItem>,
        private var newNoteList : List<DailyListItem>
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

    inner class DailyListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val checkBoxItemView : CheckBox = itemView.findViewById(R.id.checkBox_list)
        private val buttonDeleteItemView : ImageButton = itemView.findViewById(R.id.imageButton_delete)
        private val buttonEditItemView : ImageButton = itemView.findViewById(R.id.imageButton_edit)

        fun bindView(dailyListItem : DailyListItem, position: Int) {

            checkBoxItemView.text = dailyListItem.name
            checkBoxItemView.isChecked = dailyListItem.isDone
            checkBoxItemView.setOnClickListener{
                dailyListItem.isDone = !dailyListItem.isDone
                listenerUpdateList(dailyListItem)
            }
            buttonEditItemView.setOnClickListener{
                val dialog = MaterialDialog(mContext)
                dialog.show{
                    title(R.string.edit_btn)
                    message(R.string.dialog_edit_text)
                    input(hintRes = R.string.dialog_item_name){ _, text ->
                        dailyListItem.name = text.toString()
                    }
                    positiveButton(R.string.dialog_yes) {
                        listenerUpdateList(dailyListItem)
                        notifyItemChanged(position)
                        dialog.dismiss()
                    }
                    negativeButton(R.string.dialog_no) {
                        dialog.dismiss()
                    }
                }
            }
            buttonDeleteItemView.setOnClickListener{
                listenerDelete(dailyListItem)
            }
        }
    }

    internal fun setDailyListItems(dailyListItems: List<DailyListItem>) {
        val oldList = this.dailyListItems
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(
            DailyListItemDiffCallBack(oldList, dailyListItems)
        )
        this.dailyListItems = dailyListItems
        diffResult.dispatchUpdatesTo(this)

        // Alternative
        //this.dailyListItems = dailyListItems
        //notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyListAdapter.DailyListViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_layout_daily_list, parent,
            false)
        return DailyListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DailyListAdapter.DailyListViewHolder, position: Int) {
        holder.bindView(dailyListItems[position], position)
    }

    override fun getItemCount(): Int {
        return dailyListItems.size
    }

}