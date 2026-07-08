package app.allever.android.sample.im.connection

import okio.ByteString

interface ConnectionCallback {
    fun onMessageReceived(msg: String)
    fun onMessageReceived(bytes: ByteArray)
    fun onConnectionStateChanged(isConnected: Boolean)
}