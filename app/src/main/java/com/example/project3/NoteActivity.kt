package com.example.project3

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import recyclerviewadapter.ViewPagerStatesAdapter

class NoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        val prefs: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(this)
        if (!(prefs!!.contains("sorted_notes")))
            prefs.edit().putBoolean("sorted_notes", false).apply()

        viewPagerWithFragments()
    }

    private fun viewPagerWithFragments()
    {
        val viewPager : ViewPager2 = findViewById(R.id.viewpager1)
        val pagerAdapter = ViewPagerStatesAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = pagerAdapter

        val tabLayout : TabLayout = findViewById(R.id.tablayout1)
        TabLayoutMediator(tabLayout, viewPager){ tab, _ ->
            tab.text = "Fragment"
        }.attach()
    }

    // TODO: Перенести в фрагмент
    // Поскольку передается позиция в списке адаптера, невозможно удалить нужный нам объект,
    // т.к. позиции при поиске не совпадают с позициями в главном списке
    //private val itemTouchHelperCallback=
    //    object :
    //        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
    //        override fun onMove(
    //            recyclerView: RecyclerView,
    //            viewHolder: RecyclerView.ViewHolder,
    //            target: RecyclerView.ViewHolder
    //        ): Boolean {
    //            return false
    //        }
    //
    //        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    //            val extDiaryParent = intent.getSerializableExtra("extDiaryParent") as? ExtendedDiary
    //            var getList = emptyList<Note>()
    //            for(extDiary in mainViewModel.allExtendedDiaries.value!!)
    //            {
    //                if(extDiary.diary.id == extDiaryParent!!.diary.id) // находим запись с нужным нам id дневника
    //                {
    //                    getList = extDiary.notes   // получаем список заметок этого дневника
    //                    break
    //                }
    //            }
    //            val note = getList[viewHolder.adapterPosition]
    //            deleteNote(note)
    //            recyclerview1.adapter!!.notifyDataSetChanged()
    //        }
    //
    //    }
}
