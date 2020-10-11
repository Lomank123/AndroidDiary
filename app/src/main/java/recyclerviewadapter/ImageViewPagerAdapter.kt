package recyclerviewadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.davemorrissey.labs.subscaleview.ImageSource.uri
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.lomank.diary.R

class ImageViewPagerAdapter(
    context : Context
) : RecyclerView.Adapter<ImageViewPagerAdapter.ViewPagerViewHolder>() {

    private val mContext = context

    private var images = emptyList<String?>()

   inner class ViewPagerViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

       private val photoItemView : SubsamplingScaleImageView = itemView.findViewById(R.id.photoView_holder)

       fun bindView(image : String?){
            if(image != null){
                photoItemView.setImage(uri(image))
                photoItemView.orientation = SubsamplingScaleImageView.ORIENTATION_0
            }
       }
   }

    internal fun setImages(list : List<String?>){
        this.images = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.image_item_layout, parent, false)
        return ViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.bindView(images[position])
    }

    override fun getItemCount(): Int {
        return images.size
    }
}