package com.example.bluetoothutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Created by Administrator on 2018/3/28.
 */

public class BluetoothConnectImp implements IBluetoothConnectController {

    private static final String TAG = "YouRen";

    private Handler handler = new Handler(Looper.getMainLooper());

    private BluetoothGatt mBluetoothGatt;
    private IBluetoothController iBluetoothController;
    private IBluetoothController.BluetoothListener bluetoothListener;


    public BluetoothConnectImp(IBluetoothController iBluetoothController) {
        this.iBluetoothController = iBluetoothController;
        this.iBluetoothController.setCallback(gattCallback);
    }

    public void setBluetoothListener(IBluetoothController.BluetoothListener bluetoothListener) {
        this.bluetoothListener = bluetoothListener;
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead: " + gatt.getDevice().getAddress() + " -> " + characteristic.getUuid().toString());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite: " + gatt.getDevice().getAddress() + " -> " + characteristic.getUuid().toString());
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, final int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange: newState = " + newState);
            mBluetoothGatt = gatt;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                return;
            }
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                        disConnect();
                if (bluetoothListener != null) {
                    bluetoothListener.onDisConnect(gatt);
                }
                return;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            mBluetoothGatt = gatt;
            if (bluetoothListener != null) {
                bluetoothListener.onServicesDiscovered(gatt, status);
            }

//            handler.postDelayed(() -> {
//                iBluetoothController.startScan();
//            }, 500);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged: " + characteristic.getUuid().toString());
            mBluetoothGatt = gatt;
            if (bluetoothListener != null) {
                bluetoothListener.OnBleDataComing(gatt, characteristic);
            }
        }

    };

    public void connect(BluetoothDevice device, Context context) {
//        disConnect();
        BluetoothAdapter adapter = iBluetoothController.getAdapter();
        if (adapter != null) {
            BluetoothDevice remoteDevice = adapter.getRemoteDevice(device.getAddress());
            remoteDevice.connectGatt(context, false, gattCallback);
        }

    }

    public BluetoothAdapter getAdapter() {
        return iBluetoothController.getAdapter();
    }

    public void disConnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    public void connect(BluetoothDevice device, Context context, BluetoothGattCallback gattCallback) {
//        disConnect();
//        this.gattCallback = gattCallback;
//        BluetoothAdapter adapter = iBluetoothController.getAdapter();
//        if (adapter != null) {
//            BluetoothDevice remoteDevice = adapter.getRemoteDevice(device.getAddress());
//            remoteDevice.connectGatt(context, false, gattCallback);
//        }
    }

}
