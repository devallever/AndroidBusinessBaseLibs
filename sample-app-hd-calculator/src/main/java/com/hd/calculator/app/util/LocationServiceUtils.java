package com.hd.calculator.app.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

public class LocationServiceUtils {
    private static final String TAG = "LocationUtils";

    // 定位结果回调接口
    public interface LocationResultCallback {
        void onLocationReceived(Location location);
        void onLocationFailed(String error);
    }

    /**
     * 检查Google Play服务是否可用
     * @param context 上下文对象
     * @return true 表示可用，false 表示不可用
     */
    public static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int status = api.isGooglePlayServicesAvailable(context);
        return status == ConnectionResult.SUCCESS;
    }

    /**
     * 获取位置信息（自动选择最佳实现）
     * @param context 上下文对象
     * @param callback 位置结果回调
     * @param intervalMs 请求间隔（毫秒）
     */
    @SuppressLint("MissingPermission")
    public static void getLocation(Context context,
                                   @NonNull final LocationResultCallback callback,
                                   long intervalMs) {

        // 先检查位置权限
        if (!hasLocationPermission(context)) {
            callback.onLocationFailed("Location permission denied");
            return;
        }

        if (isGooglePlayServicesAvailable(context)) {
            requestFusedLocation(context, callback, intervalMs);
        } else {
            requestSystemLocation(context, callback, intervalMs);
        }
    }

    /**
     * 使用Fused Location Provider获取位置
     */
    @SuppressLint("MissingPermission")
    private static void requestFusedLocation(Context context,
                                             final LocationResultCallback callback,
                                             long intervalMs) {

        try {
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
            LocationRequest request = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(intervalMs)
                    .setFastestInterval(intervalMs / 2);

            // 获取最后一次位置作为初始值
            Task<Location> lastLocationTask = client.getLastLocation();
            lastLocationTask.addOnSuccessListener(location -> {
                if (location != null) {
                    callback.onLocationReceived(location);
                }
            });

            // 设置位置更新监听
            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult != null) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            callback.onLocationReceived(location);
                        }
                    }
                }
            };

            // 请求位置更新
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                client.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
            }

        } catch (Exception e) {
            Log.e(TAG, "Fused location error", e);
            callback.onLocationFailed("Fused location error: " + e.getMessage());
        }
    }

    /**
     * 使用系统定位服务（Android原生API）
     */
    private static void requestSystemLocation(Context context,
                                              final LocationResultCallback callback,
                                              long intervalMs) {

        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                callback.onLocationFailed("LocationManager unavailable");
                return;
            }

            // 确定最佳位置提供者
            String provider = LocationManager.GPS_PROVIDER;
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    provider = LocationManager.NETWORK_PROVIDER;
                } else {
                    callback.onLocationFailed("No location providers available");
                    return;
                }
            }

            // 获取最后一次位置作为初始值
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
                if (lastKnownLocation != null) {
                    callback.onLocationReceived(lastKnownLocation);
                }
            }

            LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    callback.onLocationReceived(location);
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    callback.onLocationFailed("Provider disabled: " + provider);
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {}

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
            };

            // 请求位置更新
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(
                        provider,
                        intervalMs,
                        10, // 最小距离变化（米）
                        listener);
            }

        } catch (Exception e) {
            Log.e(TAG, "System location error", e);
            callback.onLocationFailed("System location error: " + e.getMessage());
        }
    }

    /**
     * 检查位置权限
     */
    private static boolean hasLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
