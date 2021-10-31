import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.th.pv.*
import com.th.pv.data.*

class ActorListGridviewAdapter (
        private var pvData: PVData,
        private val showAlbums : Boolean,
        private val context: Context
) :
    BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private var filteredActors : MutableList<String> = mutableListOf()

    init {
        refilter()
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        refilter()
    }

    override fun getCount(): Int {
        return filteredActors.size
    }

    override fun getItem(position: Int): Actor? {
        return pvData.actors[filteredActors[position]]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View? {
        var convertView = convertView
        if (layoutInflater == null) {
            layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (convertView == null) {
            convertView = layoutInflater!!.inflate(R.layout.actor_list_rowitem, null)
        }

        val actor = pvData.actors[filteredActors[position]]!!

        imageView = convertView!!.findViewById(R.id.imageView)
        textView = convertView.findViewById(R.id.textView)

        glideLoadInto(context, imageView, pvData, pvData.images[actor.thumbnail])

        val ratingView = convertView.findViewById<me.zhanghai.android.materialratingbar.MaterialRatingBar>(R.id.actor_rating)
        ratingView.rating = actor.rating.toFloat() / 2

        textView.text = actor.name
        return convertView
    }

    private fun refilter() {
        filteredActors = mutableListOf()

        for (actor in pvData.actors.keys) {
            if ((showAlbums && pvData.actors[actor]!!.isAlbum()) || (!showAlbums && !pvData.actors[actor]!!.isAlbum()))
                filteredActors.add(actor)
        }

        filteredActors.sortByDescending { pvData.actors[it]!!.score }
    }
}