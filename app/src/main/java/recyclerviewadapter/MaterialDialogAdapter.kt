package recyclerviewadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lomank.diary.R
import roomdatabase.Diary
import roomdatabase.ExtendedDiary

class MaterialDialogAdapter internal constructor(
    extDiaryList : List<ExtendedDiary>,
    private val listenerSwap : (Diary) -> Unit
) : RecyclerView.Adapter<MaterialDialogAdapter.ItemViewHolder>() {

    private var diaryList = extDiaryList

    inner class ItemViewHolder(v : View) : RecyclerView.ViewHolder(v){

        private val relativeLayoutDialog : RelativeLayout = v.findViewById(R.id.relative_layout_dialog_list)
        private val textViewDialog : TextView = v.findViewById(R.id.textView_dialog)

        fun bindView(item : Diary){
            textViewDialog.text = item.name
            relativeLayoutDialog.setOnClickListener {
                listenerSwap(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialDialogAdapter.ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_layout_dialog, parent, false)
        return ItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: MaterialDialogAdapter.ItemViewHolder, position: Int) {
        holder.bindView(diaryList[position].diary)
    }

    override fun getItemCount(): Int {
        return diaryList.size
    }


}