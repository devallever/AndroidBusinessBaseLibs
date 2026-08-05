package com.example.bluetoothutil;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by Yellow on 2017/9/19.
 */

public abstract class BaseBlueToothImp implements IBluetoothController, IBluetoothController.RegisterListener {


    protected BluetoothManager manager;
    protected BluetoothAdapter adapter;
    protected RegisterBluetoothBroastReciver reciver;
    protected BluetoothGattCallback callback;

    @Override
    public BluetoothAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void setCallback(BluetoothGattCallback callback) {
        if (this.callback == null) {
            this.callback = callback;
        }
    }

    @Override
    public int initBluetooth(Context context) {
        manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        if (adapter == null) {
            return UNSUPPORT_BLUETOOTH;
        }
        if (!adapter.isEnabled()) {
            return UNOPEN_BLUETOOTH;
        }
        return OPENSUCCESS;
    }


    @Override
    public void registerBroastReciver(Context context) {
        reciver = new RegisterBluetoothBroastReciver(this);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(reciver, filter);
    }

    @Override
    public void unRegisterBroastReciver(Context context) {
        if (reciver != null) {
            context.unregisterReceiver(reciver);
        }
    }

    @Override
    public void openBluetooth(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
        }
    }

    @Override
    public void repleaseResourse() {

    }

    @Override
    public void onRegisterSuccess(Context context, Intent intent) {
        if (adapter.isEnabled()) {
            startScan();
        }
    }
}
