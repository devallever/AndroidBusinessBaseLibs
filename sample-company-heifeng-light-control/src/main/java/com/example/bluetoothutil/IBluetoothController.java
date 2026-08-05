package com.example.bluetoothutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Yellow on 2017/9/18.
 */

public interface  IBluetoothController {

    int UNSUPPORT_BLUETOOTH = 0;

    int OPENSUCCESS = 1;

    int UNOPEN_BLUETOOTH = 2;


    int initBluetooth(Context context);

    void registerBroastReciver(Context context);

    void unRegisterBroastReciver(Context context);

    void startScan();

    void stopScan();

    void openBluetooth(Context context);

    void repleaseResourse();

    BluetoothAdapter getAdapter();

    void setCallback(BluetoothGattCallback callback);

    interface ScanListener {

        void onFoundDevice(BluetoothDevice device, int rssi, byte[] scanRecord);

        void onScanFail();

        void onUnSupportBluetooth();
    }


    interface BluetoothListener {

        void OnBleDataComing(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

        void onServicesDiscovered(BluetoothGatt gatt, int status);

        void onDisConnect(BluetoothGatt gatt);
    }

    interface RegisterListener {
        void onRegisterSuccess(Context context, Intent intent);
    }

}
