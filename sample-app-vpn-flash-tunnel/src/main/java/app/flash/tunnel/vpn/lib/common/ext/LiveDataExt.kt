package app.flash.tunnel.vpn.lib.common.ext

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.toLiveData(): LiveData<T> {
    return this
}

//fun <T> MutableResult<T>.asLiveData(): MutableResult<T> {
//    return this
//}