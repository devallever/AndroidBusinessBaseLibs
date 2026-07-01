package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.android.absbase.App
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.editor.bean.EffectBean
import com.android.absbase.utils.TimeUtils
import com.allever.video.editor.function.editor.bean.VideoBean
import com.allever.video.editor.utils.DragHelper
import com.allever.video.editor.utils.MediaThumbnailUtil

class EffectListView : LinearLayout {
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var effectAdapter: EffectAdapter
    private var effectBeans = mutableListOf<EffectBean>()

    var currentSelectEffectBean: EffectBean? = null
        set(value) {
            field = value
            if (value != null) {
                effectAdapter.setSelectBean(value)
            }
        }

    var onItemListener: OnItemListener? = null
        set(value) {
            field = value
            effectAdapter.onItemListener = value
        }

    constructor(context: Context) : this(context, null) {}

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView()
    }

    private fun initView() {
        recyclerView = androidx.recyclerview.widget.RecyclerView(context)
        addView(recyclerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        layoutManager.orientation = androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = layoutManager
        effectAdapter =
            EffectAdapter(recyclerView)
        recyclerView.adapter = effectAdapter
        val marginLeft = ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_margin_left).toInt()
        val itemWidth = ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_width).toInt()
        recyclerView.addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
                val pos = parent.getChildLayoutPosition(view)
                outRect.right = marginLeft
                if (pos == 0) {
                    outRect.left = marginLeft
                }
                outRect.top = (parent.height - itemWidth) / 2
            }
        })

    }

    fun setData(beans: List<EffectBean>) {
        effectBeans.clear()
        effectBeans.addAll(beans)
        effectAdapter.setData(effectBeans)
    }

    interface OnItemListener {
        fun onClick(effectBean: EffectBean)
        fun onAdd()
        fun onItemRangeMoved(beans: List<EffectBean>, from: Int, to: Int)
    }

    class EffectAdapter(var recyclerView: androidx.recyclerview.widget.RecyclerView) : androidx.recyclerview.widget.RecyclerView.Adapter<EffectAdapter.EffectViewHolder>(), OnClickListener, DragHelper.DragStateCallback {
        private var effectBeans = mutableListOf<EffectBean>()
        private val width = ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_width).toInt()

        private var prevSelectHolder: EffectViewHolder? = null
        private var currentSelectEffectBean: EffectBean? = null

        var onItemListener: OnItemListener? = null
        init {
            DragHelper.bind(recyclerView, effectBeans, this)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EffectViewHolder {
            val view = LayoutInflater.from(parent?.context
                    ?: App.getContext()).inflate(R.layout.video_edit_trim_item_view, parent,
                    false)
            val lp = view.layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams
            lp.width = width
            lp.height = width
            val effectViewHolder =
                EffectViewHolder(
                    view
                )
            view.tag = effectViewHolder
            return effectViewHolder
        }

        override fun getItemCount(): Int {
            return effectBeans.size + 1
        }

        override fun onBindViewHolder(holder: EffectViewHolder, position: Int) {
            holder ?: return
            holder.setSelectState(false)
            if (position < effectBeans.size) {
                val effectBean = effectBeans[position]
                holder.bind(effectBean)
                if (currentSelectEffectBean?.id == effectBean.id) {
                    holder.setSelectState(true)
                }
                holder.itemView.setOnClickListener(this)
            } else {
                holder.bind()
                OnClickListenerHelper.setOnClickListener(
                    holder.itemView,
                    this,
                    true,
                    (TimeUtils.TimeConstant.ONE_SEC * 2).toInt()
                )
            }
        }

        override fun onClick(v: View?) {
            val holder = v?.tag as? EffectViewHolder
                ?: return
            if (holder.isAddItem) {
                onItemListener?.onAdd()
            } else {
                prevSelectHolder?.setSelectState(false)
                holder.setSelectState(true)
                holder.effectBean?.apply {
                    onItemListener?.onClick(this)
                }
                prevSelectHolder = holder
            }
        }

        override fun getData(): MutableList<out Any> {
            return effectBeans
        }
        override fun allowDrag(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
            return (viewHolder as? EffectViewHolder)?.isAddItem == false
        }

        override fun allowSwipe(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onItemRangeMoved(from: Int, to: Int) {
            onItemListener?.onItemRangeMoved(effectBeans, from, to)
        }
        override fun onDragStart() {

        }

        override fun onDragEnd(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, from: Int, to: Int) {

        }
        fun setData(beans: List<EffectBean>) {
            effectBeans.clear()
            effectBeans.addAll(beans)

            notifyDataSetChanged()
        }

        fun setSelectBean(bean: EffectBean) {
            currentSelectEffectBean = bean
        }

        class EffectViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            private val imageView: ImageView = itemView.findViewById(R.id.image_view)
            private val selectView: View = itemView.findViewById(R.id.select_view)

            var effectBean: EffectBean? = null
                private set
            var isAddItem = false
                private set

            init {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            }

            fun bind(effectBean: EffectBean) {
                this.effectBean = effectBean
                isAddItem = false
                itemView.background = null
                val thumbBitmaps = effectBean.getThumbBitmapForFrame()
                val bitmap = if (thumbBitmaps.isNotEmpty()) {
                    thumbBitmaps[0]
                } else null
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(R.drawable.icon_album_default)
                    if(effectBean is VideoBean){
                        val bitmap = MediaThumbnailUtil.createPreViewVideoThumbnail(effectBean.path, MediaStore.Images.Thumbnails.MINI_KIND)
                        if(bitmap != null){
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                }
            }

            fun bind() {
                isAddItem = true
                setSelectState(false)
                imageView.setImageResource(R.drawable.icon_edit_video_add)
                itemView.setBackgroundResource(R.color.video_edit_bottom_trim_add_background_color)
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }

            fun setSelectState(select: Boolean) {
                if (select) {
                    selectView.visibility = View.VISIBLE
                } else {
                    selectView.visibility = View.GONE
                }
            }
        }
    }
}
