package fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.example.project3.R
import com.example.project3.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_daily_list.*
import kotlinx.android.synthetic.main.fragment_daily_list.view.*
import recyclerviewadapter.DailyListAdapter
import roomdatabase.*
import viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class DailyListFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel       // добавляем ViewModel
    private var isListExist = false
    private var dailyListName : DailyListName? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_daily_list, container, false)

        val extDiaryParent = requireActivity().intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

        val adapter = newAdapter()
        layout.recyclerview_list.adapter = adapter
        layout.recyclerview_list.layoutManager = LinearLayoutManager(activity)
        layout.recyclerview_list.addItemDecoration(TopSpacingItemDecoration(20))    // Отступы

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(layout.recyclerview_list)

        if (mainViewModel.allDailyLists.hasActiveObservers())
            mainViewModel.allDailyLists.removeObservers(requireActivity())
        mainViewModel.allDailyLists.observe(viewLifecycleOwner, Observer {

            adapter.setDailyListItems(findDailyListItems(it, extDiaryParent))

        })

        val dailyList = findDailyList(mainViewModel.allDailyLists.value, extDiaryParent)
        if (dailyList != null)
            dailyListName = dailyList.dailyListName

        if (isListExist) {
            layout.layout_no_list.visibility = GONE
            layout.layout_list.visibility = VISIBLE
        } else {
            layout.layout_no_list.visibility = VISIBLE
            layout.layout_list.visibility = GONE
        }


        // Buttons
        // New item button
        layout.fab_new_item.setOnClickListener{
            val dialog = MaterialDialog(requireActivity())
            dialog.show{
                title(R.string.dialog_new_item)
                message(R.string.dialog_item_input_text)
                input(hintRes = R.string.dialog_item_name)

                positiveButton(R.string.dialog_yes) {
                    if(dailyListName != null) {
                        insertDailyListItem(DailyListItem(dialog.getInputField().toString(), dailyListName!!.id))
                    }
                    else
                        Toast.makeText(requireActivity(), "List Name is null", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                negativeButton(R.string.dialog_no) {
                    dialog.dismiss()
                }
            }


        }
        // Create button
        layout.button_create_list.setOnClickListener{
            val dialog = MaterialDialog(requireActivity())
            dialog.show{
                title(R.string.dialog_new_daily_list)
                message(R.string.dialog_new_daily_list_text)
                input(hintRes = R.string.dialog_new_daily_list_hint)

                positiveButton {
                    val newList = DailyListName(dialog.getInputField().toString(), extDiaryParent!!.diary.id)
                    insertDailyListName(newList)
                    dailyListName = newList

                    isListExist = true
                    layout.layout_no_list.visibility = GONE
                    layout.layout_list.visibility = VISIBLE

                    dialog.dismiss()
                }
                negativeButton {
                    dialog.dismiss()
                }
            }
        }
        return layout
    }

    // Setting adapter
    private fun newAdapter() : DailyListAdapter{
        return DailyListAdapter(requireActivity(),
            {
                // listenerUpdateList
                updateDailyListItem(it)
            }, {
                //listenerEdit
                //TODO: сделать изменение пункта
            })
    }

    private val itemTouchHelperCallback=
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT)
        {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // TODO: использовать только в списке дел
                //    val fromPosition = viewHolder.adapterPosition
                //    val toPosition = target.adapterPosition
                //    if (fromPosition < toPosition) {
                //        for (i in fromPosition until toPosition) {
                //            Collections.swap(extDiaryList, i, i + 1)
                //            val order1: Int = extDiaryList[i].diary.order!!
                //            val order2: Int = extDiaryList[i + 1].diary.order!!
                //            extDiaryList[i + 1].diary.order = order1
                //            extDiaryList[i].diary.order = order2
                //        }
                //    } else {
                //        for (i in fromPosition downTo toPosition + 1) {
                //            Collections.swap(mWordEntities, i, i - 1)
                //            val order1: Int = mWordEntities.get(i).getOrder()
                //            val order2: Int = mWordEntities.get(i - 1).getOrder()
                //            mWordEntities.get(i).setOrder(order2)
                //            mWordEntities.get(i - 1).setOrder(order1)
                //        }
                //    }
                //    //TODO: DiffUtil
                //    recyclerview.adapter!!.notifyItemMoved(fromPosition, toPosition)
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val extDiaryParent = requireActivity().intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary
                val dailyList = findDailyList(mainViewModel.allDailyLists.value!!, extDiaryParent)
                deleteDailyListItem(dailyList!!.dailyListItems[viewHolder.adapterPosition])
            }
        }

    // Finding list of DailyListItem items
    private fun findDailyListItems(dailyList : List<DailyList>, extDiaryParent : ExtendedDiary?) : List<DailyListItem>{
        var dailyListItems = emptyList<DailyListItem>()
        for(list in dailyList) {
            if(list.dailyListName.parentId == extDiaryParent!!.diary.id) {
                dailyListItems = list.dailyListItems
                //TODO: под вопросом
                //isListExist = true
                break
            }
        }
        return dailyListItems
    }

    // Find DailyListName
    private fun findDailyList(dailyList : List<DailyList>?, extDiaryParent: ExtendedDiary?) : DailyList?{
        if(dailyList != null) {
            for (list in dailyList)
                if (list.dailyListName.parentId == extDiaryParent!!.diary.id) {
                    isListExist = true
                    return list
                }
        }
        else {
            Toast.makeText(requireActivity(), "List Daily is null", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    // Возвращает текущую дату
    @SuppressLint("SimpleDateFormat")
    private fun currentDate() : String
    {
        val pattern = "\t\t\tHH:mm\n\ndd.MM.yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(Date())
    }


    // mainViewModel DailyListName methods

    private fun deleteDailyList(list : DailyListName){
        mainViewModel.deleteDailyListName(list)
    }

    private fun insertDailyListName(item : DailyListName){
        mainViewModel.insertDailyListName(item)
    }

    // mainViewModel DailyListItem methods

    private fun deleteDailyListItem(item : DailyListItem){
        mainViewModel.deleteOneDailyListItem(item)
    }

    private fun insertDailyListItem(item : DailyListItem){
        mainViewModel.insertDailyListItem(item)
    }

    private fun updateDailyListItem(item : DailyListItem){
        mainViewModel.updateDailyListItem(item)
    }

}