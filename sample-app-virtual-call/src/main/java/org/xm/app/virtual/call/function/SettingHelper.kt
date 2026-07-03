package org.xm.app.virtual.call.function

import app.allever.android.lib.core.store.StoreCore


object SettingHelper {

    private const val KEY_PHONE = "PHONE"
    private const val KEY_TIME = "TIME"
    private const val KEY_CONTACT = "CONTACT"
    private const val KEY_LOCAL = "LOCAL"
    private const val KEY_RINGTONE_URI = "KEY_RINGTONE_URI"
    private const val KEY_RINGTONE_TITLE = "KEY_RINGTONG_TITLE"
    private const val KEY_VIBRATOR = "KEY_VIBRATOR"
    private const val KEY_WALL_PAGER_TITLE = "KEY_WALL_PAGER_TITLE"
    private const val KEY_REPEAT_SWITCH = "KEY_REPEAT_SWITCH"
    private const val KEY_REPEAT_INTERVAL = "KEY_REPEAT_INTERVAL"
    private const val KEY_REPEAT_COUNT = "KEY_REPEAT_COUNT"
    private const val KEY_RADOM_CONTACT_SWITCH = "KEY_RADOM_CONTACT_SWITCH"
    private const val KEY_AVATAR = "KEY_AVATAR"

    fun setPhone(phone: String) {
        StoreCore.putString(KEY_PHONE, phone)
    }

    fun getPhone(): String? {
        return StoreCore.getString(KEY_PHONE, "13800138000")
    }

    fun setContact(contact: String) {
        StoreCore.putString(KEY_CONTACT, contact)
    }

    fun getContact(): String? {
        return StoreCore.getString(KEY_CONTACT, "张三")
    }

    fun setLocal(local: String) {
        StoreCore.putString(KEY_LOCAL, local)
    }

    fun getLocal(): String? {
        return StoreCore.getString(KEY_LOCAL, "北京")
    }

    fun setTime(time: Int) {
        StoreCore.putInt(KEY_TIME, time)
    }

    fun getTime(): Int? {
        return StoreCore.getInt(KEY_TIME, 5)
    }


    fun setRingtoneUri(uri: String) {
        StoreCore.putString(KEY_RINGTONE_URI, uri)
    }

    fun getRingtoneUri(): String? {
        return StoreCore.getString(KEY_RINGTONE_URI, "")
    }


    fun setRingtoneTitle(ringtoneTitle: String) {
        StoreCore.putString(KEY_RINGTONE_TITLE, ringtoneTitle)
    }

    fun getRingtoneTitle(): String? {
        return StoreCore.getString(KEY_RINGTONE_TITLE, "Default")
    }

    fun setWallPagerTitle(wallPagerTitle: String) {
        StoreCore.putString(KEY_WALL_PAGER_TITLE, wallPagerTitle)
    }

    fun getWallPagerTitle(): String {
        return StoreCore.getString(KEY_WALL_PAGER_TITLE, "Default")?:""
    }

    fun setVibrator(vibrator: Boolean) {
        StoreCore.putBoolean(KEY_VIBRATOR, vibrator)
    }

    fun getVibrator(): Boolean {
        return StoreCore.getBoolean(KEY_VIBRATOR, true)
    }

    /*
        private const val KEY_REPEAT_SWITCH = "KEY_REPEAT_SWITCH"
    private const val KEY_REPEAT_INTERVAL = "KEY_REPEAT_INTERVAL"
    private const val KEY_REPEAT_COUNT = "KEY_REPEAT_COUNT"

     */

    fun setRepeat(repeat: Boolean) {
        StoreCore.putBoolean(KEY_REPEAT_SWITCH, repeat)
    }

    fun getRepeat(): Boolean {
        return StoreCore.getBoolean(KEY_REPEAT_SWITCH, false)
    }

    fun setRepeatInterval(interval: Int) {
        StoreCore.putInt(KEY_REPEAT_INTERVAL, interval)
    }

    fun getRepeatInterval(): Int {
        return StoreCore.getInt(KEY_REPEAT_INTERVAL, 2)
    }

    fun setRepeatCount(interval: Int) {
        StoreCore.putInt(KEY_REPEAT_COUNT, interval)
    }

    fun getRepeatCount(): Int {
        return StoreCore.getInt(KEY_REPEAT_COUNT, 2)
    }

    fun setRandomContact(random: Boolean) {
        StoreCore.putBoolean(KEY_RADOM_CONTACT_SWITCH, random)
    }

    fun getRandomContact(): Boolean {
        return StoreCore.getBoolean(KEY_RADOM_CONTACT_SWITCH, false)
    }

    fun setAvatarPath(avatar: String) {
        StoreCore.putString(KEY_AVATAR, avatar)
    }

    fun getAvatarPath(): String {
        return StoreCore.getString(KEY_AVATAR, "")?:""
    }

}