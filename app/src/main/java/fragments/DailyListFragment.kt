package fragments

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.lomank.diary.AboutActivity
import com.lomank.diary.R
import com.lomank.diary.SettingsHolderActivity
import com.lomank.diary.TopSpacingItemDecoration
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_daily_list.view.*
import recyclerviewadapter.DailyListAdapter
import roomdatabase.DailyListItem
import roomdatabase.ExtendedDiary
import viewmodel.MainViewModel

class DailyListFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel       // добавляем ViewModel

    private lateinit var allDailyListItems : List<DailyListItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_daily_list, container, false)

        val extDiaryParent = requireActivity().intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val adapter = newAdapter()
        layout.recyclerview_list.adapter = adapter
        layout.recyclerview_list.layoutManager = LinearLayoutManager(activity)
        layout.recyclerview_list.addItemDecoration(TopSpacingItemDecoration(20))    // Отступы

        if (mainViewModel.allExtendedDiaries.hasActiveObservers())
            mainViewModel.allExtendedDiaries.removeObservers(requireActivity())
        mainViewModel.allExtendedDiaries.observe(viewLifecycleOwner, Observer {
            allDailyListItems = findDailyListItems(it, extDiaryParent)
            adapter.setDailyListItems(allDailyListItems)
            layout.recyclerview_list.setHasFixedSize(true)

            // Updating horizontal Progress Bar
            val listSize = allDailyListItems.size
            var listDoneSize = 0
            for (item in allDailyListItems) {
                if (item.isDone)
                    listDoneSize++
            }
            if (listSize != 0) {
                val i = ObjectAnimator.ofInt(
                    layout.progressBar_appbar,
                    "progress",
                    (listDoneSize * 100) / listSize
                )
                i.interpolator = FastOutLinearInInterpolator()
                i.duration = 500
                i.addUpdateListener {
                    layout.progressbar_text.text =
                        i.animatedValue.toString() + " " + resources.getString(
                            R.string.progressbar_progress
                        )
                }
                i.start()
                // TODO: Изменить текст на строковый ресурс
                if((listDoneSize * 100) / listSize >= 100) {
                    Snackbar.make(layout, "You have succeed!", Snackbar.LENGTH_SHORT)
                        .show()
                }
            } else
                layout.progressBar_appbar.progress = 0


            // don't need for now
            //layout.recyclerview_list.scrollToPosition(0)
        })

        layout.collapsing_toolbar_layout.title = extDiaryParent!!.diary.listName
        layout.progressbar_text.text = resources.getString(R.string.progressbar_progress_full)

        // Setting Touch helper
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(layout.recyclerview_list)

        // Buttons
        // New item button
        layout.fab_new_item.setOnClickListener{
            val dialog = MaterialDialog(requireActivity())
            dialog.show{
                title(R.string.dialog_new_item)
                message(R.string.dialog_item_input_text)
                input(hintRes = R.string.dialog_item_name){ _, text ->
                    val item = DailyListItem(text.toString(), extDiaryParent.diary.id)
                    insertDailyListItem(item)
                }
                positiveButton(R.string.dialog_yes) {

                    dialog.dismiss()
                }
                negativeButton(R.string.dialog_no) {
                    dialog.dismiss()
                }
            }
        }
        layout.imageButton_edit_list_name.setOnClickListener{
            val dialog = MaterialDialog(requireActivity())
            dialog.show{
                title(R.string.edit_btn)
                message(R.string.dialog_edit_text)
                input(hintRes = R.string.dialog_item_name){ _, text ->
                    val newDiary = extDiaryParent.diary
                    newDiary.listName = text.toString()
                    mainViewModel.updateDiary(newDiary)
                    layout.collapsing_toolbar_layout.title = text.toString()
                }
                positiveButton(R.string.dialog_yes) {

                    dialog.dismiss()
                }
                negativeButton(R.string.dialog_no) {
                    dialog.dismiss()
                }
            }
        }

        layout.appbar_layout.addOnOffsetChangedListener(object :
            AppBarLayout.OnOffsetChangedListener {
            var isShow = false
            var scrollRange: Int = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {

                if (scrollRange == -1)
                    scrollRange = appBarLayout!!.totalScrollRange
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    layout.progress_layout.animate().translationY(scrollRange.toFloat()).start()
                    layout.progressbar_text.animate().alpha(0f).setDuration(300).start()
                    layout.imageButton_edit_list_name.visibility = GONE
                } else if (isShow) {
                    isShow = false
                    layout.progress_layout.animate().translationY(0f).start()
                    layout.progressbar_text.animate().alpha(1f).setDuration(300).start()
                    layout.imageButton_edit_list_name.visibility = VISIBLE
                }
            }
        })

        return layout
    }

    // Setting adapter
    private fun newAdapter() : DailyListAdapter{
        return DailyListAdapter(requireActivity(),
            {
                // listenerUpdateList
                updateDailyListItem(it)
            }, {
                //listenerDelete
                deleteDailyListItem(it)
            })
    }

    private val itemTouchHelperCallback=
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT)
        {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return true
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val extDiaryParent = requireActivity().intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary
                val dailyListItems = findDailyListItems(mainViewModel.allExtendedDiaries.value!!, extDiaryParent)
                deleteDailyListItem(dailyListItems[viewHolder.adapterPosition])
            }
        }

    // Finding list of DailyListItem items
    private fun findDailyListItems(
        allDiariesList: List<ExtendedDiary>,
        extDiaryParent: ExtendedDiary?
    ) : List<DailyListItem>{
        var dailyListItems = emptyList<DailyListItem>()
        for(list in allDiariesList) {
            if(list.diary.id == extDiaryParent!!.diary.id) {
                dailyListItems = list.dailyListItems
                break
            }
        }
        return dailyListItems
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.actionbar_menu, menu)
        menu.findItem(R.id.search_view).isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.settings -> {
                // открытие окна "Настройки"
                val intentSettings = Intent(activity, SettingsHolderActivity::class.java)
                startActivity(intentSettings)
                return super.onOptionsItemSelected(item)
            }
            R.id.about -> {
                // открытие окна "О нас"
                val aboutIntent = Intent(requireActivity(), AboutActivity::class.java)
                startActivity(aboutIntent)
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // mainViewModel DailyListItem methods

    private fun deleteDailyListItem(item: DailyListItem){
        mainViewModel.deleteOneDailyListItem(item)
    }

    private fun insertDailyListItem(item: DailyListItem){
        mainViewModel.insertDailyListItem(item)
    }

    private fun updateDailyListItem(item: DailyListItem){
        mainViewModel.updateDailyListItem(item)
    }

}