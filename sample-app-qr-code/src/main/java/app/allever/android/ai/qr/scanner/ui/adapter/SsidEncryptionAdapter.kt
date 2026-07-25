package app.allever.android.ai.qr.scanner.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.allever.app.qr.code.scaner.R

class SsidEncryptionAdapter(context: Context, data: List<String>) : BaseAdapter() {
    private val mContext: Context = context
    private val mData: List<String> = data

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: MyViewHolder
        val itemView: View
        if (convertView == null){
            itemView = LayoutInflater.from(mContext).inflate(R.layout.qr_item_ssid_encryption, parent, false)
            viewHolder = MyViewHolder(itemView)
            itemView.tag = viewHolder
        }else{
            itemView = convertView
            viewHolder = itemView.tag as MyViewHolder
        }

        viewHolder.tvEncryption.text = mData[position]
        return itemView
    }

    override fun getItem(position: Int): Any = mData[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = mData.size

    inner class MyViewHolder(itemView: View){
        val tvEncryption: TextView = itemView as TextView
    }

}

