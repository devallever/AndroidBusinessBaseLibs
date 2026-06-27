//package com.example.charge.utils
//
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.ConnectivityManager.NetworkCallback
//import android.net.Network
//import android.net.NetworkCapabilities
//import android.net.NetworkRequest
//import android.os.Build
//import com.example.charge.ChargeApp
//import app.allever.android.lib.core.app.App
//import com.example.charge.event.NetworkChangeEvent
//import org.greenrobot.eventbus.EventBus
//
//
//object NetworkHelper {
//
//    private var connectivityManager: ConnectivityManager? = null
//    private var networkCallback: NetworkCallback? = null
//    var isNetworkAvailable: Boolean = true
//
//    fun setupNetworkCallback() {
//        connectivityManager =
//            ChargeApp.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
//        networkCallback = object : NetworkCallback() {
//            public override fun onAvailable(network: Network) {
//                super.onAvailable(network)
//                if (!isNetworkAvailable) {
//                    EventBus.getDefault().post(NetworkChangeEvent(true))
//                }
//                isNetworkAvailable = true
//                // 网络可用
//                if (App.DEBUG) {
//                    log("网络可用")
//                }
//            }
//
//            override fun onLost(network: Network) {
//                super.onLost(network)
//                if (isNetworkAvailable) {
//                    EventBus.getDefault().post(NetworkChangeEvent(false))
//                }
//                isNetworkAvailable = false
//                // 网络丢失
//                if (App.DEBUG) {
//                    log("网络丢失")
//                }
//            }
//        }
//
//        // 注册回调
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            connectivityManager!!.registerDefaultNetworkCallback(networkCallback!!)
//        } else {
//            // 对于API 21-23，使用以下方法
//            val request: NetworkRequest? = NetworkRequest.Builder()
//                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//                .build()
//            connectivityManager!!.registerNetworkCallback(request!!, networkCallback!!)
//        }
//    }
//
//}