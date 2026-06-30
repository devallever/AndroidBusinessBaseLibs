package com.step.wincash.utils

import com.step.wincash.R

object BannerNativeUtil {

    var bannerImageList = mutableListOf<Pair<Int, Int>>()

    var nativeImageList = mutableListOf<Pair<Int, Int>>()

    fun getMyBannerImageList(): List<Pair<Int, Int>> {
        if (bannerImageList.isEmpty()) {
            bannerImageList = mutableListOf(
                Pair(R.drawable.st_ic_banner1, R.drawable.ic_banner_bt1),
                Pair(R.drawable.st_ic_banner2, R.drawable.ic_banner_bt2),
                Pair(R.drawable.ic_banner3, R.drawable.ic_banner_bt3),
                Pair(R.drawable.ic_banner4, R.drawable.ic_banner_bt4)
            )
        }
        return bannerImageList
    }

    fun getMyNativeImageList(): List<Pair<Int, Int>> {
        if (nativeImageList.isEmpty()) {
            nativeImageList = mutableListOf(
                Pair(R.drawable.ic_native1,R.drawable.ic_banner_bt1),
                Pair(R.drawable.ic_native2,R.drawable.ic_banner_bt2),
                Pair(R.drawable.ic_native3,R.drawable.ic_banner_bt3),
                Pair(R.drawable.ic_native4,R.drawable.ic_banner_bt4)
            )
        }
        return nativeImageList
    }

    fun getBannerImage(): Pair<Int, Int> {
        val res = getMyBannerImageList().random()
        bannerImageList.remove(res)
        return res
    }

    fun getNativeImage(): Int {
        val res = getMyNativeImageList().random()
        val index = getMyNativeImageList().indexOf(res)
        nativeImageList.remove(res)
        return index
    }

    fun getBannerClickSize(): Int {
        return SpUtil.get(SpKey.BANNER_CLICK_NUM, 0)
    }

    fun addBannerClickSize() {
        val size = getBannerClickSize() + 1
        SpUtil.put(SpKey.BANNER_CLICK_NUM, size)
    }

    fun getNativeClickSize(): Int {
        return SpUtil.get(SpKey.NATIVE_CLICK_NUM, 0)
    }

    fun addNativeClickSize() {
        val size = getNativeClickSize() + 1
        SpUtil.put(SpKey.NATIVE_CLICK_NUM, size)
    }
}