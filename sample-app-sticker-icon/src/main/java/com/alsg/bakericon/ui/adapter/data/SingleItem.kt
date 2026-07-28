package com.alsg.bakericon.ui.adapter.data

import android.os.Parcel
import android.os.Parcelable

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */

class SingleItem() : Parcelable {
    var url = ""

    constructor(parcel: Parcel) : this() {
        url = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SingleItem> {
        override fun createFromParcel(parcel: Parcel): SingleItem {
            return SingleItem(parcel)
        }

        override fun newArray(size: Int): Array<SingleItem?> {
            return arrayOfNulls(size)
        }
    }
}