package recyclerviewadapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RelativeLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.project3.R
import roomdatabase.DailyListItem
import roomdatabase.Note


class DailyListAdapter internal constructor(
    context: Context,
    private val listenerUpdateList : (DailyListItem) -> Unit,
    private val listenerEdit : (DailyListItem) -> Unit
) : RecyclerView.Adapter<DailyListAdapter.DailyListViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val mContext = context

    private val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(mContext)

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

        private val layoutItemView : RelativeLayout = itemView.findViewById(R.id.relative_layout_list)
        private val checkBoxItemView : CheckBox = itemView.findViewById(R.id.checkBox_list)

        fun bindView(dailyListItem : DailyListItem) {


            checkBoxItemView.text = dailyListItem.name
            checkBoxItemView.isChecked = dailyListItem.isDone
            checkBoxItemView.setOnCheckedChangeListener{ _: CompoundButton?, isChecked: Boolean ->
                dailyListItem.isDone = isChecked
                listenerUpdateList(dailyListItem)

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
        //notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyListAdapter.DailyListViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_layout_list, parent,
            false)
        return DailyListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DailyListAdapter.DailyListViewHolder, position: Int) {
        holder.bindView(dailyListItems[position])
    }

    override fun getItemCount(): Int {
        return dailyListItems.size
    }

}