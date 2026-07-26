package com.hd.calculator.app.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

public class NetworkUtils {

    private static final String TAG = "NetworkUtils";
    private static ConnectivityManager connectivityManager;
    private static ConnectivityManager.NetworkCallback networkCallback;
    private static NetworkStateChangeListener networkStateListener;

    /**
     * 初始化网络工具类（在Application中调用）
     */
    public static void init(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * 注册网络状态监听
     */
    public static void registerNetworkCallback(NetworkStateChangeListener listener) {
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null. Did you call NetworkUtils.init()?");
            return;
        }

        networkStateListener = listener;

        // 注销之前的监听器（如果存在）
        if (networkCallback != null) {
            unregisterNetworkCallback();
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d(TAG, "Network available");
                if (networkStateListener != null) {
                    networkStateListener.onNetworkAvailable(true);
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.d(TAG, "Network lost");
                if (networkStateListener != null) {
                    networkStateListener.onNetworkLost();

                    // 检查剩余的网络状态
                    checkNetworkStates();
                }
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                Log.d(TAG, "Network capabilities changed");
                checkNetworkStates();
            }

            @Override
            public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
                Log.d(TAG, "Link properties changed");
                checkNetworkStates();
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        connectivityManager.registerNetworkCallback(request, networkCallback);
    }

    /**
     * 注销网络状态监听
     */
    public static void unregisterNetworkCallback() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            networkCallback = null;
            networkStateListener = null;
            Log.d(TAG, "Network callback unregistered");
        }
    }

    /**
     * 检测网络整体是否可用（包括移动数据和Wi-Fi）
     */
    public static boolean isNetworkAvailable() {
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null. Did you call NetworkUtils.init()?");
            return false;
        }

        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    /**
     * 检测移动数据是否可用
     */
    public static boolean isMobileDataEnabled() {
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null. Did you call NetworkUtils.init()?");
            return false;
        }

        boolean result = false;
        Network[] networks = connectivityManager.getAllNetworks();
        for (Network network : networks) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 检测Wi-Fi是否可用
     */
    public static boolean isWifiEnabled() {
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null. Did you call NetworkUtils.init()?");
            return false;
        }

        boolean result = false;
        Network[] networks = connectivityManager.getAllNetworks();
        for (Network network : networks) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 检查当前所有网络状态并通知监听器
     */
    private static void checkNetworkStates() {
        if (networkStateListener == null) return;

        boolean wifiState = isWifiEnabled();
        boolean mobileDataState = isMobileDataEnabled();

        networkStateListener.onWifiStateChanged(wifiState);
        networkStateListener.onMobileDataStateChanged(mobileDataState);

        // 如果没有活动网络但监听器需要知道
        if (!wifiState && !mobileDataState) {
            networkStateListener.onNetworkLost();
        } else {
            networkStateListener.onNetworkAvailable(true);
        }
    }

    /**
     * 获取当前网络类型
     *
     * @return 网络类型字符串：WIFI, MOBILE, VPN, OTHER, NONE
     */
    public static String getNetworkType() {
        if (!isNetworkAvailable()) {
            return "NONE";
        }

        if (isWifiEnabled()) {
            return "WIFI";
        }

        if (isMobileDataEnabled()) {
            return "MOBILE";
        }

        // 检查是否有VPN连接
        Network[] networks = connectivityManager.getAllNetworks();
        for (Network network : networks) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    return "VPN";
                }
            }
        }

        return "OTHER";
    }

    /**
     * 网络状态变更监听器
     */
    public interface NetworkStateChangeListener {
        void onNetworkAvailable(boolean isAvailable);

        void onNetworkLost();

        void onWifiStateChanged(boolean isConnected);

        void onMobileDataStateChanged(boolean isConnected);
    }
}
