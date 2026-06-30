package app.android.allever.gp.quick.project.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryUtil {
    private static final String TAG = "BatteryUtil";
    private static String technology;
    private static int voltage;
    private static int level;
    private static double temperature;
    private static String status;
    private static String health;
    private static String plugged;

    private static CB cb;

    /**
     * 获取电池容量
     */
    public static String getBatteryCapacity(Context context) {
        Object mPowerProfile;
        double batteryCapacity = 0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class)
                    .newInstance(context);

            batteryCapacity = (double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return String.valueOf(batteryCapacity);
    }

    /**
     *通过接收系统广播获取电池的信息
     */
    public static void  receiverBatteryOhterInfo(Context context, CB callback) {
        cb = callback;
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent receiver = context.registerReceiver(null, filter);

        technology =receiver.getStringExtra("technology"); //获取电池技术
        if(technology.equals("") || technology.equals(null)){
            technology="Unknown";
        }
        voltage = receiver.getIntExtra("voltage", 0); //获取电压(mv)
        level = receiver.getIntExtra("level", 0); //获取当前电量
        temperature = receiver.getIntExtra("temperature", 0)/10.0; //获取温度(数值)并转为电池摄氏温度

        //电池状态
        switch (receiver.getIntExtra("status",0)){
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                status="未知";
                break;
            case BatteryManager.BATTERY_STATUS_CHARGING:
                status="充电中";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                status="放电中";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                status="未充电";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                status="电池满";
                break;
        }
        android.util.Log.d(TAG,"status:"+status);

        //电池健康情况
        switch (receiver.getIntExtra("health",0)){
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                health="未知";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                health="良好";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                health="过热";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                health="没电";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                health="过电压";
                break;
            case BatteryManager.BATTERY_HEALTH_COLD:
                health="温度过低";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                health="未知错误";
                break;
        }
        android.util.Log.d(TAG,"health:"+health);

        //充电类型
        switch (receiver.getIntExtra("plugged",0)){
            case BatteryManager.BATTERY_PLUGGED_AC:
                plugged="充电器";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                plugged="USB";
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                plugged="无线充电";
                break;
            default:
                plugged="未充电";
                break;
        }
        android.util.Log.d(TAG,"plugged:"+plugged);

        //double temp, String health, String mv, String charge
        cb.onReceiveInfo(temperature, health, voltage  + "mV", status);
    }

    public static void unRegister() {
        cb = null;
    }

    public static interface CB {
        void onReceiveInfo(double temp, String health, String mv, String charge);
    }
}
