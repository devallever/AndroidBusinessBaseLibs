package com.example.bluetoothutil;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.os.Build;

/**
 * Created by Yellow on 2017/9/19.
 */

public class HighVersionBluetoothImp extends BaseBlueToothImp {

    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;

    public HighVersionBluetoothImp(ScanCallback scanCallback) {
        this.scanCallback = scanCallback;
    }

    @Override
    public void startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = adapter.getBluetoothLeScanner();
            bluetoothLeScanner.stopScan(scanCallback);
            bluetoothLeScanner.startScan(scanCallback);
        }
    }

    @Override
    public void repleaseResourse() {
        super.repleaseResourse();
        if (bluetoothLeScanner != null && scanCallback != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }
    }

    @Override
    public void stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }
}
