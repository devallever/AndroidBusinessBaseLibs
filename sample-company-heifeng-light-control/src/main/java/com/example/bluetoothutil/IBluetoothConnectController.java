package com.example.bluetoothutil;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;

/**
 * Created by Administrator on 2018/3/28.
 */

public interface IBluetoothConnectController {

    void connect(BluetoothDevice device, Context context, BluetoothGattCallback gattCallback);

    void disConnect();

    void connect(BluetoothDevice device, Context context);

    void setBluetoothListener(IBluetoothController.BluetoothListener bluetoothListener);
}
