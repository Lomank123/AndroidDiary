package fragments

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.lomank.diary.AboutActivity
import com.lomank.diary.R
import com.lomank.diary.SettingsHolderActivity
import other.TopSpacingItemDecoration
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

    private var isComplete = false

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
        mainViewModel.allExtendedDiaries.observe(viewLifecycleOwner, {
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
                if((listDoneSize * 100) / listSize >= 100) {
                    // появляется всего 1 раз когда отмечены все пункты
                    if(!isComplete)
                        Snackbar.make(layout, resources.getString(R.string.snackbar_succeed), Snackbar.LENGTH_SHORT).show()
                    isComplete = true
                }
            } else {
                layout.progressBar_appbar.progress = 0
                layout.progressbar_text.text = resources.getString(R.string.progressbar_progress_full)
            }

            // don't need for now
            //layout.recyclerview_list.scrollToPosition(0)
        })

        layout.collapsing_toolbar_layout.title = extDiaryParent!!.diary.listName
        layout.progressbar_text.text = resources.getString(R.string.progressbar_progress_full)

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

        layout.imageButton_clear_list.setOnClickListener {
            val dialog = MaterialDialog(requireActivity())
            dialog.show{
                title(R.string.dialog_clear_list_title)
                message(R.string.dialog_clear_list)
                positiveButton(R.string.dialog_yes) {
                    // deleting all list items
                    deleteDailyList(extDiaryParent.diary.id, layout)
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
                    layout.imageButton_clear_list.visibility = GONE
                } else if (isShow) {
                    isShow = false
                    layout.progress_layout.animate().translationY(0f).start()
                    layout.progressbar_text.animate().alpha(1f).setDuration(300).start()
                    layout.imageButton_edit_list_name.visibility = VISIBLE
                    layout.imageButton_clear_list.visibility = VISIBLE
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
        inflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.search_view).isVisible = false
        menu.findItem(R.id.star).isVisible = false
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

    // SnackBar creation (with UNDO button)
    private fun createUndoSnackBar(view : View, list : List<DailyListItem>){
        val snackBar = Snackbar.make(view, resources.getString(R.string.snackbar_clear_list), Snackbar.LENGTH_LONG)
        snackBar.setAnchorView(R.id.fab_new_item)
        snackBar.setAction(resources.getString(R.string.snackbar_undo_btn)){
            insertDailyList(list)
        }
        snackBar.show()
    }

    // mainViewModel DailyListItem methods

    private fun deleteDailyListItem(item: DailyListItem){
        mainViewModel.deleteOneDailyListItem(item)
    }

    private fun insertDailyListItem(item: DailyListItem){
        mainViewModel.insertDailyListItem(item)
    }

    private fun insertDailyList(list : List<DailyListItem>){
        mainViewModel.insertListItems(list)
    }

    private fun updateDailyListItem(item: DailyListItem){
        mainViewModel.updateDailyListItem(item)
    }

    private fun deleteDailyList(id : Long, view : View){
        val oldDailyList = allDailyListItems
        mainViewModel.deleteDailyList(id)
        createUndoSnackBar(view, oldDailyList)
    }

}