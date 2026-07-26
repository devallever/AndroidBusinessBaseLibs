package com.hd.calculator.app.business;

import com.hd.calculator.app.function.printer.PrinterManager;
import com.hd.calculator.app.function.store.StoreManager;

public class Config {

    private static final String KEY_LOCAL_MODE = "local_mode";

    private static final String KEY_PRINT_SWITCH = "KEY_PRINT_SWITCH";

    private static final String KEY_PRINTER_BLE = "KEY_PRINTER_BLE";

    private static final String KEY_LOCAL_UN_UPLOAD_TABLE_DATA = "KEY_LOCAL_UN_UPLOAD_TABLE_DATA";

    /**
     * 是否离线模式
     * @return
     */
    public static boolean isLocalMode() {
        return StoreManager.getIns().getBoolean(KEY_LOCAL_MODE, true);
    }

    //setLocalMode
    public static void setLocalMode(boolean localMode) {
        StoreManager.getIns().putBoolean(KEY_LOCAL_MODE, localMode);
    }

    public static boolean getPrintSwitch() {
        return StoreManager.getIns().getBoolean(KEY_PRINT_SWITCH, true);
    }

    public static void setPrintSwitch(boolean printSwitch) {
        StoreManager.getIns().putBoolean(KEY_PRINT_SWITCH, printSwitch);
    }

    public static String getPrinterBleMac() {
        return StoreManager.getIns().getString(KEY_PRINTER_BLE, PrinterManager.DEFAULT_BLE_MAC);
    }

    public static void setPrinterBle(String bleMac) {
        StoreManager.getIns().putString(KEY_PRINTER_BLE, bleMac);
    }

    public static void setLocalUnUploadTableData(String data) {
        StoreManager.getIns().putString(KEY_LOCAL_UN_UPLOAD_TABLE_DATA, data);
    }

    public static String getLocalUnUploadTableData() {
        return StoreManager.getIns().getString(KEY_LOCAL_UN_UPLOAD_TABLE_DATA, "");
    }


}
