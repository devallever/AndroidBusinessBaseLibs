package com.example.bluetoothutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.List;

/**
 * Created by Yellow on 2017/9/19.
 */

public class BluetoothClient {


    public static final String TAG = "BluetoothClient";
    private ScanCallback scanCallback;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private BlueToothController blueToothController;
    private IBluetoothController.ScanListener scanListener;
    private BaseBlueToothImp baseBlueToothImp;
    private boolean isRegisteringScan = false;  //是否在注册蓝牙后的开始扫描蓝牙设备。

    public void setRegisteringScan() {
        isRegisteringScan = true;
    }

    public void setScanListener(IBluetoothController.ScanListener scanListener) {
        this.scanListener = scanListener;
    }

    public void setConnectListener(IBluetoothController.BluetoothListener listener) {
        BluetoothConnectImp bluetoothConnectImp = new BluetoothConnectImp(baseBlueToothImp);
        bluetoothConnectImp.setBluetoothListener(listener);
        if (blueToothController != null) {
            blueToothController.setiBluetoothConnectController(bluetoothConnectImp);
        }
    }

    /**
     * 初始化蓝牙扫描前需要的操作
     */
    private void initBlueToothController(Context context) {
        if (blueToothController == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                blueToothController = new BlueToothController(baseBlueToothImp = new HighVersionBluetoothImp(scanCallback), context);
            } else {
                blueToothController = new BlueToothController(baseBlueToothImp = new LowVerSionBluetoothImp(leScanCallback), context);
            }

            if (isRegisteringScan) {
                blueToothController.registerBroastReciver();
            }
            blueToothController.initBlueTooth();
        }
    }

    public void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (scanCallback == null) {
                scanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        scanListener.onFoundDevice(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                    }

                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        super.onBatchScanResults(results);
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        Log.d(TAG, "onScanFailed: code = " + errorCode);
                        scanListener.onScanFail();
//                        ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED
                    }
                };
            }
            initBlueToothController(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (leScanCallback == null) {
                leScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                        scanListener.onFoundDevice(device, rssi, scanRecord);
                    }
                };
            }
            initBlueToothController(context);
        } else {
            scanListener.onUnSupportBluetooth();
            return;
        }
    }

    public void startScan() {
        if (blueToothController != null) {
            blueToothController.start();
        }
    }

    public void connectDevice(BluetoothDevice device) {
        blueToothController.connect(device);
    }

    public void stopScan() {
        if (blueToothController != null) {
            blueToothController.stopScan();
        }
    }

    public void replease() {
        if (blueToothController != null){
            blueToothController.repleaseResoure();
        }
    }

    public void disconnect() {
        if (blueToothController != null) {
            blueToothController.disconnect();
        }
    }

    public BluetoothAdapter getAdapter() {
        return baseBlueToothImp.getAdapter();
    }

    public BluetoothGattCallback getCallback() {
        return baseBlueToothImp.callback;
    }
}
