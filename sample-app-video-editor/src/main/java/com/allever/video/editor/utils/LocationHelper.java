package com.allever.video.editor.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import com.android.absbase.helper.log.DLog;
import com.android.absbase.utils.DebugUtil;

/**
 * 地理位置帮助类
 */
public class LocationHelper {
    private static final String TAG = LocationHelper.class.getName();
    private Context mContext;
    private LocationManager mLocationManager = null;
    private MyLocationListener [] locationListeners = null;
    
    public LocationHelper(Context context) {
        this.mContext = context.getApplicationContext();
        mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
    }
    
    public synchronized Location getLocation() {
        // returns null if not available
        if( locationListeners == null )
            return null;
        // location listeners should be stored in order best to worst
        for(int i=0;i<locationListeners.length;i++) {
            Location location = locationListeners[i].getLocation();
            if( location != null )
                return location;
        }
        return null;
    }
    
//    public boolean testHasReceivedLocation() {
//        if( locationListeners == null )
//            return false;
//        for(int i=0;i<locationListeners.length;i++) {
//            if( locationListeners[i].test_has_received_location )
//                return true;
//        }
//        return false;
//    }
    
//    public synchronized void setupLocationListener() {
//        DLog.d(TAG, "setupLocationListener");
//        // Define a listener that responds to location updates
//        // we only set it up if store_location is true, to avoid unnecessarily wasting battery
//        boolean store_location = SPDataManager.isShowLocation();
//        if( store_location && locationListeners == null ) {
//            locationListeners = new MyLocationListener[2];
//            locationListeners[0] = new MyLocationListener();
//            locationListeners[1] = new MyLocationListener();
//
//            // location listeners should be stored in order best to worst
//            // also see https://sourceforge.net/p/opencamera/tickets/1/ - need to check provider is available
//            if( mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) ) {
//                try {
//                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListeners[1]);
//                } catch (Throwable tr) {
//                    DLog.e(TAG, "", tr);
//                }
//            }
//            if( mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER) ) {
//                try {
//                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListeners[0]);
//                } catch (Throwable tr) {
//                    DLog.e(TAG, "", tr);
//                }
//            }
//        }
//        else if( !store_location ) {
//            freeLocationListeners();
//        }
//    }
    
//    public synchronized void freeLocationListeners() {
//        DLog.d(TAG, "freeLocationListeners");
//        if( locationListeners != null ) {
//            for(int i=0;i<locationListeners.length;i++) {
//                try {
//                    mLocationManager.removeUpdates(locationListeners[i]);
//                } catch (Throwable tr) {
//                    DLog.e(TAG, "", tr);
//                }
//                locationListeners[i] = null;
//            }
//            locationListeners = null;
//        }
//    }
    
//    public boolean hasLocationListeners() {
//        if( this.locationListeners == null )
//            return false;
//        if( this.locationListeners.length != 2 )
//            return false;
//        for(int i=0;i<this.locationListeners.length;i++) {
//            if( this.locationListeners[i] == null )
//                return false;
//        }
//        return true;
//    }
    
    private class MyLocationListener implements LocationListener {
        private Location location = null;
        public boolean test_has_received_location = false;
        
        Location getLocation() {
            return location;
        }
        
        public void onLocationChanged(Location location) {
            DLog.d(TAG, "onLocationChanged");
            this.test_has_received_location = true;
            // Android camera source claims we need to check lat/long != 0.0d
            if( location.getLatitude() != 0.0d || location.getLongitude() != 0.0d ) {
                if (DebugUtil.isDebuggable()) {
                    DLog.d(TAG, "received location:");
                    DLog.d(TAG, "lat " + location.getLatitude() + " long " + location.getLongitude() + " accuracy " + location.getAccuracy());
                }
                this.location = location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
             switch( status ) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                {
                    if (DebugUtil.isDebuggable()) {
                        if( status == LocationProvider.OUT_OF_SERVICE )
                            DLog.d(TAG, "location provider out of service");
                        else if( status == LocationProvider.TEMPORARILY_UNAVAILABLE )
                            DLog.d(TAG, "location provider temporarily unavailable");
                    }
                    this.location = null;
                    this.test_has_received_location = false;
                    break;
                }
             }
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            DLog.d(TAG, "onProviderDisabled");
            this.location = null;
            this.test_has_received_location = false;
        }
    }

    /**
     * location 的格式是类似于-90.0000+180.0000
     *
     * @param location
     * @return 如果返回值是空的说明经纬度不存在 结果是一个String数组  第一个是纬度 第二个是经度
     */
//    @Nullable
//    @Size(value = 2)
//    public static String[] parseLocation(String location) {
//        if (TextUtils.isEmpty(location)) {
//            return null;
//        } else {
//            double longitude = 0.0;
//            double latitude = 0.0;
//            int index1 = location.lastIndexOf("-");
//            int index2 = location.lastIndexOf("+");
//            if (index1 != 0 && index1 != -1) {
//                latitude = Double.valueOf(location.substring(0, index1));
//                longitude = Double.valueOf(location.substring(index1, location.length()));
//            } else if (index2 != 0 && index2 != -1) {
//                latitude = Double.valueOf(location.substring(0, index2));
//                longitude = Double.valueOf(location.substring(index2, location.length()));
//            } else {
//                return null;
//            }
//            String[] result = new String[2];
//            //纬度
//            result[0] = Location.convert(latitude, Location.FORMAT_DEGREES);
//            //经度
//            result[1] = Location.convert(longitude, Location.FORMAT_DEGREES);
//            return result;
//        }
//    }
}
