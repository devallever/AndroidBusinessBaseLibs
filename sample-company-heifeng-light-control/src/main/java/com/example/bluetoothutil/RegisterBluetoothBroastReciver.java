package com.example.bluetoothutil;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Yellow on 2017/9/19.
 */

public class RegisterBluetoothBroastReciver extends BroadcastReceiver {


    private IBluetoothController.RegisterListener registerListener;

    public RegisterBluetoothBroastReciver(IBluetoothController.RegisterListener registerListener) {
        this.registerListener = registerListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int intExtra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
        if (intExtra == BluetoothAdapter.STATE_ON) {
            registerListener.onRegisterSuccess(context, intent);
        } else if (intExtra == BluetoothAdapter.STATE_TURNING_ON) {
            Log.e("onReceive:------------ ", "bluetooth opening");
        } else if (intExtra == BluetoothAdapter.STATE_TURNING_OFF) {
            Log.e("onReceive:------------ ", "bluetooth offing");
        } else if (intExtra == BluetoothAdapter.STATE_OFF) {
            Log.e("onReceive:------------ ", "bluetooth off");
        }
    }
}
