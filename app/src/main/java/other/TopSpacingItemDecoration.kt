package other

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TopSpacingItemDecoration(private val padding: Int): RecyclerView.ItemDecoration() {

    //Класс, для добавления в ресайклер вью пробелов над каждой *карточкой*
    //*Прямоугольнику* присваивается отступ, переданный в этот класс
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.top = padding
    }
}