package com.allever.video.editor.ui

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.allever.video.editor.R
import com.allever.video.editor.ui.widget.CircleFillProgressView
import com.allever.video.editor.ui.widget.FontView

class BottomTabHolder {
    var layout: ViewGroup? = null
        private set
    var imageView: ImageView? = null
        private set
    var textView: TextView? = null
        private set
    var downloadImageView: ImageView? = null
        private set
//    var progressBar: ProgressBar? = null
//        private set
    var circleProgressBar: CircleFillProgressView? = null
        private set
    var circleProgressBarContainer: ViewGroup? = null
        private set
    var progressText: TextView? = null
        private set
    var mask: View? = null
        private set
    var crown: ImageView? = null
        private set
    var fontView: FontView? = null

    var obj: Any? = null

    private var mNewFlagView: ImageView? = null

    fun getLayoutId(): Int {
        return layout?.id ?: View.NO_ID
    }

    fun setOnClickListener(listener: View.OnClickListener?) {
        layout?.setOnClickListener(listener)
    }

    fun setVisibility(visibility: Int) {
        layout?.visibility = visibility
    }

    fun setImageResource(@DrawableRes resId: Int) {
        imageView?.setImageResource(resId)
    }

    fun setImageBitmap(bitmap: Bitmap) {
        imageView?.setImageBitmap(bitmap)
    }

    fun setImageDrawable(drawable: Drawable) {
        imageView?.setImageDrawable(drawable)
    }

    fun setText(@StringRes resId: Int) {
        textView!!.setText(resId)
    }

    fun setText(text: String) {
        textView?.text = text
    }

    fun show(visible: Boolean) {
        if (visible) {
            layout?.visibility = View.VISIBLE
        } else {
            layout?.visibility = View.GONE
        }
    }

    fun showNewFlag(visible: Boolean) {
        if (visible) {
            mNewFlagView?.visibility = View.VISIBLE
        } else {
            mNewFlagView?.visibility = View.GONE
        }
    }

    companion object {

        fun getHolder(activity: Activity, layout: ViewGroup?,
                      @StringRes labelId: Int? = null,
                      @DrawableRes iconId: Int? = null,
                      clickListener: View.OnClickListener? = null): BottomTabHolder {
            val holder = BottomTabHolder()
            holder.layout = layout
            holder.layout?.tag = holder
            holder.imageView = holder.layout?.findViewById(R.id.image_view)
            holder.textView = holder.layout?.findViewById(R.id.text_view)
            holder.mNewFlagView = holder.layout?.findViewById(R.id.newflag_view)
            holder.crown = holder.layout?.findViewById(R.id.iv_crown)
            holder.downloadImageView = holder.layout?.findViewById(R.id.download_view)
//            holder.progressBar = holder.layout?.findViewById(R.id.progress_bar)
            holder.circleProgressBar = holder.layout?.findViewById(R.id.circle_progress_bar)
            holder.circleProgressBarContainer = holder.layout?.findViewById(R.id.circle_progress_bar_container)
            holder.progressText = holder.layout?.findViewById(R.id.tv_progress)
            holder.mask = holder.layout?.findViewById(R.id.mask)
            holder.fontView = holder.layout?.findViewById(R.id.font_view)

//            val colorFilter = activity.resources.getColor(R.color.edit_bottom_btn_color_filter)
//            holder.imageView?.setColorFilter(colorFilter)

            if (labelId != null) {
                holder.setText(labelId)
            }
            if (iconId != null) {
                holder.setImageResource(iconId)
            }
            if (clickListener != null) {
                holder.setOnClickListener(clickListener)
            }

            return holder
        }

        fun getHolderInflater(activity: Activity, @LayoutRes layoutId: Int,
                              @StringRes labelId: Int? = null,
                              @DrawableRes iconId: Int? = null,
                              clickListener: View.OnClickListener? = null): BottomTabHolder {
            val viewGroup = activity.layoutInflater.inflate(layoutId, null) as? ViewGroup
            viewGroup?.id = View.generateViewId()
            return getHolder(
                activity,
                viewGroup,
                labelId,
                iconId,
                clickListener
            )
        }

        fun getHolderBy(activity: Activity, @IdRes layoutId: Int,
                        @StringRes labelId: Int? = null,
                        @DrawableRes iconId: Int? = null,
                        clickListener: View.OnClickListener? = null): BottomTabHolder {
            val viewGroup = activity.findViewById(layoutId) as? ViewGroup
            return getHolder(
                activity,
                viewGroup,
                labelId,
                iconId,
                clickListener
            )
        }
    }
}