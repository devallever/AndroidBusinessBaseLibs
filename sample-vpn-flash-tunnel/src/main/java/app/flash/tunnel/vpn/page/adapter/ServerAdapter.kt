package app.flash.tunnel.vpn.page.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.RecyclerView
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.data.NodeItem
import app.flash.tunnel.vpn.databinding.ItemAdBinding
import app.flash.tunnel.vpn.databinding.ItemServerBinding
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.helper.ad.AdHelper
import app.flash.tunnel.vpn.lib.common.ext.loadCircle
import app.flash.tunnel.vpn.lib.common.util.DisplayManager
import app.flash.tunnel.vpn.page.adapter.viewholder.AdViewHolder
import app.flash.tunnel.vpn.page.adapter.viewholder.NodeViewHolder
import com.github.shadowsocks.preference.DataStore

class ServerAdapter(val data: MutableList<NodeItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_NODE = 0
        const val TYPE_AD = 1
    }

    var callback: (position: Int, item: NodeItem) -> Unit = { position, item -> }

    private var mSelectPosition = -1

    private val hadShowAdMargin = DisplayManager.dip2px(8)

    fun updateSelectPosition(position: Int) {
        mSelectPosition = 0//smart mode
        notifyDataSetChanged()
    }

    fun updateSelectPosition() {
        if (TunnelHelper.isSmartMode()) {
            mSelectPosition = 0//smart mode
        } else {
            data.mapIndexed { index, item ->
                if (item.entity?.id == DataStore.profileId) {
                    mSelectPosition = index
                    notifyDataSetChanged()
                    return
                }
            }
        }
        mSelectPosition = 0//smart mode
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_AD -> AdViewHolder(
                ItemAdBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> NodeViewHolder(
                ItemServerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        if (holder is NodeViewHolder) {
            holder.binding.apply {
                //flag
                if (item.isSmartMode()) {
                    ivFlag.loadCircle(R.drawable.icon_item_default)
                } else {
                    TunnelHelper.loadRegionsFlag(ivFlag, item.cc)
                }
                //node name
                tvNodeName.text = item.nn
                //countryName
                tvCountryName.text = item.cn

                //select
                val selectIcon = if (mSelectPosition == position) {
                    R.drawable.icon_choose_selected_12
                } else {
                    R.drawable.icon_choose_unselected_12
                }
                ivSelect.setImageResource(selectIcon)

                //item bg
                val bg = if (mSelectPosition == position) {
                    R.drawable.shape_item_server_selected
                } else {
                    R.drawable.shape_item_server_un_selected
                }
                root.setBackgroundResource(bg)

                root.setOnClickListener {
                    callback.invoke(position, item)
                }
            }
        } else if (holder is AdViewHolder) {
            val container = holder.binding.adContainer
            AdHelper.loadNodeListNative(container, success = {
                val lp = container.layoutParams as MarginLayoutParams
                lp.setMargins(hadShowAdMargin, hadShowAdMargin, hadShowAdMargin, hadShowAdMargin)
                container.layoutParams = lp
            }, fail = {
                val lp = container.layoutParams as MarginLayoutParams
                lp.setMargins(0, 0, 0, 0)
                container.layoutParams = lp
            })
        }

    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is NodeViewHolder) {
            return
        }
        if (holder is AdViewHolder) {
            AdHelper.destroyNative(holder.binding.adContainer)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = data[position]
        return item.type
    }
}