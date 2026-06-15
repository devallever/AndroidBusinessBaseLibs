package com.example.charge.utils

import com.example.charge.R
import kotlin.collections.random

object NativeUtil {

    var nativeImageList = mutableListOf<Pair<Int, Int>>()

    fun getMyNativeImageList(): List<Pair<Int, Int>> {
        if (nativeImageList.isEmpty()) {
            nativeImageList = mutableListOf(
                Pair(R.drawable.ic_native1, R.drawable.ic_banner_bt1),
                Pair(R.drawable.ic_native2, R.drawable.ic_banner_bt2),
                Pair(R.drawable.ic_native3, R.drawable.ic_banner_bt3),
                Pair(R.drawable.ic_native4, R.drawable.ic_banner_bt4)
            )
        }
        return nativeImageList
    }

    fun getNativeImage(): Int {
        val res = getMyNativeImageList().random()
        val index = getMyNativeImageList().indexOf(res)
        nativeImageList.remove(res)
        return index
    }

    fun getNativeClickSize(): Int {
        return SpUtil.get(SpKey.NATIVE_CLICK_NUM, 0)
    }

    fun addNativeClickSize() {
        val size = getNativeClickSize() + 1
        SpUtil.put(SpKey.NATIVE_CLICK_NUM, size)
    }
}