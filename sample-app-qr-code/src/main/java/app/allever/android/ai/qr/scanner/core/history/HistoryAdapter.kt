package app.allever.android.ai.qr.scanner.core.history

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.android.absbase.App
import com.android.absbase.utils.TimeUtils
import com.google.zxing.client.android.result.ResultHandler
import com.google.zxing.client.result.ParsedResultType
import com.google.zxing.client.result.URIParsedResult
import com.allever.app.qr.code.scaner.R
import app.allever.android.ai.qr.scanner.core.preview.ResultUIModel
import app.allever.android.ai.qr.scanner.core.preview.WebSafeCheckLayout
import app.allever.android.ai.qr.scanner.core.result.URIResultHandler
import java.text.SimpleDateFormat
import java.util.*

val ITEM_TYPE_DATE = R.layout.history_list_item_date
val ITEM_TYPE_HISTORY = R.layout.history_list_item_data
val ITEM_TYPE_NO_DATA = R.layout.history_list_item_empty

open class Item<T>(val type: Int, val obj: T, val id: Int = -1, var checked: Boolean? = null) {

    override fun equals(other: Any?): Boolean {
        if (this !== other) {
            if (this.hashCode() != other?.hashCode()) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        return obj?.hashCode() ?: 0
    }
}
typealias AnyItem = Item<*>

class DateItem(obj: String) : Item<String>(ITEM_TYPE_DATE, obj)

class HistoryItem(obj: com.google.zxing.client.android.history.HistoryItem,
                  id: Int,
                  val resultHandler: ResultHandler,
                  val qrType: ParsedResultType,
                  checked: Boolean = false)
    : Item<com.google.zxing.client.android.history.HistoryItem>(ITEM_TYPE_HISTORY, obj, id, checked = checked) {
    var safeType: Int? = null
}

class HistoryAdapter(val context: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>(), View.OnClickListener, View.OnLongClickListener {
    private var mInflater = LayoutInflater.from(context)
    internal var datas = mutableListOf<AnyItem>()
        set(items) {
            field = items
            notifyDataSetChanged()
        }
    var itemClickListener: OnItemClickListener? = null

    var editMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var checkedAll: Boolean
        get() {
            var hasChecked = false
            var hasUnchecked = false
            for (item in datas) {
                val checked = item.checked
                if (checked != null) {
                    if (checked) {
                        hasChecked = true
                    } else {
                        hasUnchecked = true
                    }
                }
            }
            return hasChecked && !hasUnchecked
        }
        set(value) {
            for (item in datas) {
                if (item.checked != null) {
                    item.checked = value
                }
            }
            notifyDataSetChanged()
        }

    internal abstract class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        var currentPos: Int = -1
        var currentItem: AnyItem? = null

        open fun bind(pos: Int, item: Item<*>) {
            currentPos = pos
            currentItem = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val view = mInflater.inflate(viewType, parent, false)
        val viewHolder = when (viewType) {
            ITEM_TYPE_NO_DATA -> object : ViewHolder(view) {

            }
            ITEM_TYPE_DATE -> object : ViewHolder(view) {
                var tvDate: TextView? = null

                init {
                    tvDate = view.findViewById(R.id.tv_date)
                }

                override fun bind(pos: Int, item: AnyItem) {
                    super.bind(pos, item)
                    tvDate?.text = item.obj as String
                }
            }
            ITEM_TYPE_HISTORY -> object : ViewHolder(view) {
                private var ivIcon: ImageView? = view.findViewById(R.id.iv_icon)
                private var tvTime: TextView? = view.findViewById(R.id.tv_time)
                private var tvTitle: TextView? = view.findViewById(R.id.tv_title)
                private var tvDetail: TextView? = view.findViewById(R.id.tv_detail)
                private var cbSelect: CheckBox? = view.findViewById(R.id.cb_select)
                private var mWebSafeCheckLayout: WebSafeCheckLayout? = view.findViewById(R.id.web_safe_check)

                init {
                    cbSelect?.tag = this
                    cbSelect?.setOnCheckedChangeListener { buttonView, isChecked ->
                        val holder = buttonView.tag as? ViewHolder
                        holder?.currentItem?.checked = isChecked
//                        if (holder != null) {
//                            itemClickListener?.onItemClick(holder.currentPos, holder.currentItem)
//                        }
                    }
                    view.setOnLongClickListener(this@HistoryAdapter)
                    view.setOnClickListener {
                        if (editMode) {
                            cbSelect?.isChecked = cbSelect?.isChecked == false
                        } else {
                            this@HistoryAdapter.onClick(view)
                        }
                    }
                }

                override fun bind(pos: Int, item: AnyItem) {
                    super.bind(pos, item)
                    val historyItem = item.obj as com.google.zxing.client.android.history.HistoryItem
                    val title: String
                    val detail: String
                    val time: String
                    val qrType = (item as? HistoryItem)?.qrType
                    val resultTypeUIData = ResultUIModel.get(qrType)
                    if (historyItem.result != null) {
                        title = resultTypeUIData.name ?: historyItem.result.text
                        detail = historyItem.displayAndDetails
                        time = TimeUtils.getTime(historyItem.result.timestamp, SimpleDateFormat("HH:mm:ss", Locale.getDefault()))
                    } else {
                        val resources = App.getContext().resources
                        title = resources.getString(com.google.zxing.client.android.R.string.history_empty)
                        detail = resources.getString(com.google.zxing.client.android.R.string.history_empty_detail)
                        time = ""
                    }
                    if (resultTypeUIData.iconHasBackground) {
                        val background = ResourcesCompat.getDrawable(context.resources, R.drawable.history_item_icon_bg, null)
                        ivIcon?.background = background
                        ivIcon?.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        ResultUIModel.changeBackground(qrType, ivIcon, false)
                    } else {
                        ivIcon?.background = null
                        ivIcon?.scaleType = ImageView.ScaleType.FIT_XY
                    }
                    val icon = resultTypeUIData.drawable
                    if (icon != null) {
                        ivIcon?.setImageDrawable(icon)
                    }
                    tvTime?.text = time
                    tvTitle?.text = title
                    tvDetail?.text = detail
                    cbSelect?.visibility = if (editMode) View.VISIBLE else View.GONE
                    cbSelect?.isChecked = this.currentItem?.checked == true
                    tvTime?.visibility = if (editMode) View.GONE else View.VISIBLE

                    if (qrType == ParsedResultType.URI) {
                        mWebSafeCheckLayout?.visibility = View.VISIBLE
                        val safeType = item.safeType
                        if (safeType == null || safeType == WebSafeCheckLayout.TYPE_UNKNOWN) {
                            mWebSafeCheckLayout?.onCheckListener = object : WebSafeCheckLayout.OnCheckLinsenter {
                                override fun onChecked(type: Int) {
                                    item.safeType = type
                                }
                            }
                            val uri = ((item.resultHandler as URIResultHandler).result as URIParsedResult).uri
                            mWebSafeCheckLayout?.start(uri)
                        } else {
                            mWebSafeCheckLayout?.type = safeType
                        }
                    } else {
                        mWebSafeCheckLayout?.visibility = View.GONE
                    }
                }

            }
            else -> throw IllegalArgumentException("View type not recognized")
        }
        view.tag = viewHolder
        return viewHolder
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        if (holder !is ViewHolder) {
            return
        }
        holder.bind(position, datas[position])
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = datas[position]
        return item.type
    }

    override fun onLongClick(v: View?): Boolean {
        val holder = v?.tag as? ViewHolder
        if (holder != null) {
            return itemClickListener?.onItemLongClick(holder.currentPos, holder.currentItem) == true
        }
        return false
    }

    override fun onClick(v: View?) {
        val holder = v?.tag as? ViewHolder

        if (holder != null) {
            itemClickListener?.onItemClick(holder.currentPos, holder.currentItem)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, item: AnyItem?)
        fun onItemLongClick(position: Int, item: AnyItem?): Boolean
    }
}

