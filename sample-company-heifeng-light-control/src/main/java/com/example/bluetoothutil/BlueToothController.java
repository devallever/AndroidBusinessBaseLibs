package com.example.bluetoothutil;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

/**
 * Created by Yellow on 2017/9/19.
 */

public class BlueToothController {

    private static final String TAG = BlueToothController.class.getSimpleName();

    public IBluetoothController controller;
    public IBluetoothConnectController iBluetoothConnectController;
    public Context context;
    public boolean isSupport = true;


    public BlueToothController(IBluetoothController controller, Context context) {
        this.controller = controller;
        this.context = context;
    }

    public void setiBluetoothConnectController(IBluetoothConnectController iBluetoothConnectController) {
        this.iBluetoothConnectController = iBluetoothConnectController;
    }

    public void registerBroastReciver() {
        controller.registerBroastReciver(context);
    }

    public void initBlueTooth() {
        int initTag = controller.initBluetooth(context);
        if (IBluetoothController.OPENSUCCESS == initTag) {
            //初始化完成，待启动扫描
        } else if (IBluetoothController.UNOPEN_BLUETOOTH == initTag) {
            controller.openBluetooth(context);
        } else if (IBluetoothController.UNSUPPORT_BLUETOOTH == initTag) {
            isSupport = false;
        } else {
            try {
                throw new Exception("bluetoothutil is abnormal");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSupportBLEBluetooth() {
        return isSupport;
    }

    public void start() {
        Log.d(TAG, "start: 开始扫描");
        controller.startScan();
    }

    public void connect(BluetoothDevice bluetoothDevice) {
        if (isSupport && iBluetoothConnectController != null) {
//            controller.stopScan();
            iBluetoothConnectController.connect(bluetoothDevice, context);
        } else {
            Log.e("excetion", "device connect fails");
        }
    }

    public void stopScan() {
        controller.stopScan();
    }


    public void repleaseResoure() {

        if (iBluetoothConnectController != null) {
            iBluetoothConnectController.disConnect();
        }
        if (controller != null) {
            controller.stopScan();
            controller.unRegisterBroastReciver(context);
            controller.repleaseResourse();
        }
        context = null;
    }

    public void disconnect() {
        if (iBluetoothConnectController != null) {
            iBluetoothConnectController.disConnect();
        }
    }
}
