package com.hd.calculator.app.function.printer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.R;
import com.hd.calculator.app.business.Config;
import com.hd.calculator.app.business.TableManager;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderWithDishesRef;
import com.hd.calculator.app.util.ByteUtils;
import com.hd.calculator.app.util.GsonUtils;
import com.hd.calculator.app.util.LocationServiceUtils;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class PrinterManager implements ReceiveListener {

    private static final int REQUEST_PERMISSION = 0x01;
    private static final long DISCONNECT_INTERVAL = 500;
    //    private final String mBleAddress = "BT:A6:D7:3C:A3:59:5B";// 对方mac
    // A6:D7:3C:A6:3C:45 //我们mac

    public static final String DEFAULT_BLE_MAC = "192.168.0.126";
//    private final String mBleAddress = "A6:D7:3C:A6:3C:45";//德国

    private Printer mPrinter = null;

    private PrinterManager() {
    }

    public static PrinterManager getInstance() {
        return PrinterManagerHolder.INSTANCE;
    }

    /***
     * 进入主页调用此方法
     * 请求了权限，需要在回调中调用处理 handlePermissionResult 方法
     * @param context
     */
    public void init(Activity context) {
        requestRequirePermission(context);
        enableLocationSetting(context);
        initPrinter(MyApp.context);
    }

    /**
     * 释放打印机
     * 退出应用时调用此方法
     */
    public void release() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    /***
     * 发送打印数据
     * @return
     */
    public boolean sendData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            mPrinter.clearCommandBuffer();
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            mPrinter.clearCommandBuffer();
            try {
                mPrinter.disconnect();
            } catch (Exception ex) {
                // Do nothing
            }
            return false;
        }
        return true;
    }

    public void printOrder(long orderId, List<PrintOrderDishesRequireData> printItemList) {
        if (!createOrderData(orderId, printItemList)) {
            return;
        }
        boolean result = sendData();
        if (result) {
//            ToastUtils.show("打印成功");
            LogUtils.log("打印成功");
        } else {
//            ToastUtils.show("打印失败");
            LogUtils.log("打印失败");
//            release();
//            initPrinter(MyApp.context);
        }
    }

    public void handlePermissionResult(Activity context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION || grantResults.length == 0) {
            return;
        }

        List<String> requestPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {
                // If your app targets Android 12 (API level 31) and higher, it's recommended that you declare BLUETOOTH permission.
                if (permissions[i].equals(Manifest.permission.BLUETOOTH_SCAN) && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    requestPermissions.add(permissions[i]);
                }
                if (permissions[i].equals(Manifest.permission.BLUETOOTH_CONNECT) && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    requestPermissions.add(permissions[i]);
                }
            } else if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                // If your app targets Android 11 (API level 30) or lower, it's necessary that you declare ACCESS_FINE_LOCATION permission.
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    requestPermissions.add(permissions[i]);
                }
            } else {
                // If your app targets Android 9 (API level 28) or lower, you can declare the ACCESS_COARSE_LOCATION permission instead of the ACCESS_FINE_LOCATION permission.
                if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    requestPermissions.add(permissions[i]);
                }
            }
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(context, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }

    @Override
    public void onPtrReceive(Printer printer, int i, PrinterStatusInfo printerStatusInfo, String s) {
        MyApp.mainH.post(new Runnable() {
            @Override
            public synchronized void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();

                dispPrinterWarnings(printerStatusInfo);
            }
        });
    }

    private void dispPrinterWarnings(PrinterStatusInfo status) {
        String warningsMsg = "";

        if (status == null) {
            return;
        }

        if (status.getPaper() == Printer.PAPER_NEAR_END) {
            warningsMsg += MyApp.context.getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (status.getBatteryLevel() == Printer.BATTERY_LEVEL_1) {
            warningsMsg += MyApp.context.getString(R.string.handlingmsg_warn_battery_near_end);
        }

        if (status.getPaperTakenSensor() == Printer.REMOVAL_DETECT_PAPER) {
            warningsMsg += MyApp.context.getString(R.string.handlingmsg_warn_detect_paper);
        }

        if (status.getPaperTakenSensor() == Printer.REMOVAL_DETECT_UNKNOWN) {
            warningsMsg += MyApp.context.getString(R.string.handlingmsg_warn_detect_unknown);
        }

        LogUtils.log("Printer Status:" + warningsMsg);
    }

    /**
     * 初始化打印机
     *
     * @param context
     */
    private void initPrinter(Context context) {
        if (mPrinter != null) {
            return;
        }
        try {
            mPrinter = new Printer(Printer.TM_M30, Printer.MODEL_CHINESE, context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mPrinter.setReceiveEventListener(this);
    }

    /**
     * 连接打印机
     * 每次调用打印方法之前都调用此方法
     *
     * @return
     */
    private boolean connectPrinter() {
        if (mPrinter == null) {
            return false;
        }

        try {
            mPrinter.connect(formatMacAddress(Config.getPrinterBleMac()), Printer.PARAM_DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 断开打印机
     * onPtrReceive回调时调用此方法，使用Demo写法
     */
    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        while (true) {
            try {
                mPrinter.disconnect();
                break;
            } catch (final Exception e) {
                if (e instanceof Epos2Exception) {
                    //Note: If printer is processing such as printing and so on, the disconnect API returns ERR_PROCESSING.
                    if (((Epos2Exception) e).getErrorStatus() == Epos2Exception.ERR_PROCESSING) {
                        try {
                            e.printStackTrace();
                            Thread.sleep(DISCONNECT_INTERVAL);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        e.printStackTrace();
                        break;
                    }
                } else {
                    e.printStackTrace();
                    break;
                }
            }
        }

        mPrinter.clearCommandBuffer();
    }

    /***
     * 获取权限
     * @param context
     */
    private void requestRequirePermission(Activity context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        List<String> requestPermissions = new ArrayList<>();

        if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {
            // If your app targets Android 12 (API level 31) and higher, it's recommended that you declare BLUETOOTH permission.
            int permissionBluetoothScan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN);
            int permissionBluetoothConnect = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT);
            if (permissionBluetoothScan == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (permissionBluetoothConnect == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        } else if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            // If your app targets Android 11 (API level 30) or lower, it's necessary that you declare ACCESS_FINE_LOCATION permission.
            int permissionLocationFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionLocationFine == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            // If your app targets Android 9 (API level 28) or lower, you can declare the ACCESS_COARSE_LOCATION permission instead of the ACCESS_FINE_LOCATION permission.
            int permissionLocationCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionLocationCoarse == PackageManager.PERMISSION_DENIED) {
                requestPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
        }

        if (!requestPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(context, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
        }
    }

    /**
     * 请求权限
     *
     * @param context
     */
    private void enableLocationSetting(Activity context) {
        if (!LocationServiceUtils.isGooglePlayServicesAvailable(MyApp.context)) {
            LogUtils.log("Google Play Services unavailable");
//            LocationServiceUtils.getLocation(context, new LocationServiceUtils.LocationResultCallback() {
//                @Override
//                public void onLocationReceived(Location location) {
//                    LogUtils.log("Location received: " + location);
//                }
//
//                @Override
//                public void onLocationFailed(String error) {
//                    LogUtils.log("Location failed: " + error);
//                }
//            }, 5000);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(context);
//        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
//
//        task.addOnSuccessListener(context, new OnSuccessListener<LocationSettingsResponse>() {
//            @Override
//            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                // All location settings are satisfied. The client can initialize
//                // location requests here.
//                // ...
//                LogUtils.log("Location settings are satisfied." + GsonUtils.toJson(locationSettingsResponse));
//            }
//        });
//
//        task.addOnFailureListener(context, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                if (e instanceof ResolvableApiException) {
//                    LogUtils.log("Location settings are not satisfied. Show the user a dialog to");
//                    // Location settings are not satisfied, but this can be fixed
//                    // by showing the user a dialog.
//                    try {
//                        // Show the dialog by calling startResolutionForResult(),
//                        // and check the result in onActivityResult().
//                        ResolvableApiException resolvable = (ResolvableApiException) e;
//                        resolvable.startResolutionForResult(context, CommonStatusCodes.RESOLUTION_REQUIRED);
//                    } catch (IntentSender.SendIntentException sendEx) {
//                        // Ignore the error.
//                    }
//                }
//            }
//        });
    }

    private String formatMacAddress(String address) {
        //BT:A6:D7:3C:A3:59:5B
        return "TCP:" + address;
    }

    public void printDebugData() {
        if (!createDebugData()) {
            return;
        }

        if (!sendData()) {
        }
    }

    private boolean createDebugData() {
        String method = "";
//        Bitmap logoData = BitmapFactory.decodeResource(getResources(), R.drawable.store);
        StringBuilder textData = new StringBuilder();
        final int barcodeWidth = 2;
        final int barcodeHeight = 100;

        if (mPrinter == null) {
            return false;
        }

        try {

//            if(mDrawer.isChecked()) {
//                method = "addPulse";
//                mPrinter.addPulse(Printer.PARAM_DEFAULT,
//                        Printer.PARAM_DEFAULT);
//            }
            //解决编码问题
//            mPrinter.addTextLang(Printer.LANG_ZH_CN);
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

//            method = "addImage";
//            mPrinter.addImage(logoData, 0, 0,
//                    logoData.getWidth(),
//                    logoData.getHeight(),
//                    Printer.COLOR_1,
//                    Printer.MODE_MONO,
//                    Printer.HALFTONE_DITHER,
//                    Printer.PARAM_DEFAULT,
//                    Printer.COMPRESS_AUTO);

            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            List<Byte> dataList = new ArrayList<>();
            dataList.add((byte)27);
            dataList.add((byte)64);
            dataList.add((byte)28);
            dataList.add((byte)46);
            dataList.add((byte)27);
            dataList.add((byte)116);
            dataList.add((byte)19);

            byte[] COMMAND_INIT = {27,64,};
            byte[] COMMAND_CLOSE_CHINESE = {28,46};
            byte[] COMMAND_SET_CODE = {27,116,19};


            byte[] data = {27,64,28,46,27,116,19,(byte)132,(byte)148,(byte)129,(byte)225,83,116,117,116,116,103,97,114,116,117,114,32,83,116,114,97,(byte)225,101,10};
            mPrinter.addCommand(data);
            mPrinter.addFeedLine(1);
//            mPrinter.addTextLang(Printer.LANG_MULTI);
            String specialContent = "äöüStuttgartur Straße";
            byte[] specialContentData = ByteUtils.mergeByteArrays(COMMAND_INIT, COMMAND_CLOSE_CHINESE, COMMAND_SET_CODE, PC858Encoder.encodeToPC858(specialContent));
            mPrinter.addCommand(specialContentData);
//            mPrinter.addText(specialContent);
            mPrinter.addFeedLine(1);
            mPrinter.addText(specialContent);
            mPrinter.addTextLang(Printer.LANG_ZH_CN);
            mPrinter.addFeedLine(1);
            //String 转byte数组的方式
            mPrinter.addCommand("中文".getBytes("GBK"));
            mPrinter.addFeedLine(1);
            mPrinter.addCommand(specialContent.getBytes());

            mPrinter.addFeedLine(1);
//            mPrinter.addTextLang(Printer.LANG_ZH_CN);
//            mPrinter.addText("我是中文\n");
            mPrinter.addTextLang(Printer.LANG_EN);
            mPrinter.addText(" Hello~");
            mPrinter.addFeedLine(1);

//            textData.append("THE STORE 123 (555) 555 – 5555\n");
//            textData.append("STORE DIRECTOR – John Smith\n");
//            textData.append("\n");
//            textData.append("7/01/07 16:58 6153 05 0191 134\n");
//            textData.append("ST# 21 OP# 001 TE# 01 TR# 747\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

//            textData.append("400 OHEIDA 3PK SPRINGF  9.99 R\n");
//            textData.append("410 3 CUP BLK TEAPOT    9.99 R\n");
//            textData.append("445 EMERIL GRIDDLE/PAN 17.99 R\n");
//            textData.append("438 CANDYMAKER ASSORT   4.99 R\n");
//            textData.append("474 TRIPOD              8.99 R\n");
//            textData.append("433 BLK LOGO PRNTED ZO  7.99 R\n");
//            textData.append("458 AQUA MICROTERRY SC  6.99 R\n");
//            textData.append("493 30L BLK FF DRESS   16.99 R\n");
//            textData.append("407 LEVITATING DESKTOP  7.99 R\n");
//            textData.append("441 **Blue Overprint P  2.99 R\n");
//            textData.append("476 REPOSE 4PCPM CHOC   5.49 R\n");
//            textData.append("461 WESTGATE BLACK 25  59.99 R\n");
//            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
//
//            textData.append("SUBTOTAL                160.38\n");
//            textData.append("TAX                      14.43\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

            method = "addTextSize";
            mPrinter.addTextSize(2, 2);
            method = "addText";
            mPrinter.addText("TOTAL    174.81\n");
            method = "addTextSize";
            mPrinter.addTextSize(1, 1);
            method = "addFeedLine";
            mPrinter.addFeedLine(1);

            textData.append("CASH                    200.00\n");
            textData.append("CHANGE                   25.19\n");
            textData.append("------------------------------\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());

//            textData.append("Purchased item total number\n");
//            textData.append("Sign Up and Save !\n");
//            textData.append("With Preferred Saving Card\n");
            method = "addText";
            mPrinter.addText(textData.toString());
            textData.delete(0, textData.length());
            method = "addFeedLine";
            mPrinter.addFeedLine(2);

            method = "addBarcode";
//            mPrinter.addBarcode("01209457",
//                    Printer.BARCODE_CODE39,
//                    Printer.HRI_BELOW,
//                    Printer.FONT_A,
//                    barcodeWidth,
//                    barcodeHeight);

            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        } catch (Exception e) {
            e.printStackTrace();
            mPrinter.clearCommandBuffer();
            return false;
        }

        textData = null;

        return true;
    }

    private boolean createOrderData(long orderId, List<PrintOrderDishesRequireData> printOrderDishesRequireDataList) {
        if (mPrinter == null) {
            return false;
        }

        if (printOrderDishesRequireDataList == null || printOrderDishesRequireDataList.isEmpty()) {
            return false;
        }


        OrderWithDishesRef orderWithDishesRef = DataBaseRepository.getInstance().getOrderById(orderId);
        if (orderWithDishesRef == null) {
            return false;
        }

        StringBuilder lineBuilder = new StringBuilder();
        int textSize = 2;
        int lineLength = 64;
        if (textSize == 1) {
            lineLength = 56;
        } else {
            lineLength = 28;
        }
        for (int i = 0; i < lineLength; i++) {
            lineBuilder.append("-");
        }
//        String line = "------------------------------------------------";
        String line = lineBuilder.toString();

        OrderRecordEntity order = orderWithDishesRef.getOrder();
//        List<OrderDishesRecordEntity> dishesList = orderWithDishesRef.getDishesList();
        //foreach
        String tableInfo = TableManager.getIns().getDisplayTableName(order.getTableCode(), order.getOrderType());
        AccountEntity accountEntity = DataBaseRepository.getInstance().getByUserId(order.getOrderUserId());
//        String waiterInfo = accountEntity.getUserName();
//        String orderTime = TimeUtils.formatTimestampToDDMMYYYYHHmm(order.getCreateTime());
//        float amountTotal = 0f;
//        List<PrintOrderDishesRequireData> printOrderDishesRequireDataList = new ArrayList<>();
//        for (OrderDishesRecordEntity dishesRecordEntity : dishesList) {
//            DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(dishesRecordEntity.getDishesCode());
//            amountTotal += dishesEntity.getPrice() * dishesRecordEntity.getCount();
//            PrintOrderDishesRequireData printDishesInfo = new PrintOrderDishesRequireData();
//            printDishesInfo.setName(dishesEntity.getName());
//            printDishesInfo.setCount(dishesRecordEntity.getCount());
//            printDishesInfo.setDishedCode(dishesEntity.getCode());
//            printOrderDishesRequireDataList.add(printDishesInfo);
//        }

        try {
//            mPrinter.addTextLang(Printer.LANG_ZH_CN);
            mPrinter.addTextFont(Printer.FONT_B);
            mPrinter.addTextSize(textSize, textSize);
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);
            mPrinter.addFeedLine(1);

            String tableCode = tableInfo;
            String userName = accountEntity.getUserName();
            String time = TimeUtils.formatTimestampToHHmm(orderWithDishesRef.getOrder().getCreateTime());

//            mPrinter.addTextSize(2, 2);
            //1倍字体参考长度 line
            String title = PrinterManager.formatThreeStrings(tableCode, userName, time, lineLength); //1倍字体正常
//            String title = tableCode + " " + userName + " " + time;
            mPrinter.addText(title);
            mPrinter.addFeedLine(1);
//            mPrinter.addTextSize(1, 1);
            mPrinter.addTextAlign(Printer.ALIGN_LEFT);
            mPrinter.addText(line);
            mPrinter.addFeedLine(1);

            for (PrintOrderDishesRequireData data : printOrderDishesRequireDataList) {
//                mPrinter.addText(data.getCount() + "x\t" + data.getDishedCode() + "\t" + data.getName());
                mPrinter.addText(formatDishesStrings(lineLength, data.getCount() + "x", data.getDishedCode(), data.getName(), data.getRemark()));
                mPrinter.addFeedLine(1);
//                if (!data.getRemark().isEmpty()) {
//                    String[] remarkArray = data.getRemark().split(";");
//                    for (String remark : remarkArray) {
//                        mPrinter.addText("\t\t" + remark);
//                        mPrinter.addFeedLine(1);
//                    }
//                }
            }
            mPrinter.addFeedLine(1);
            //cut
            mPrinter.addCut(Printer.CUT_FEED);

        } catch (Exception e) {
            e.printStackTrace();
            mPrinter.clearCommandBuffer();
            return false;
        }

        return true;
    }

    public void printDebugCommandData() {
        if (mPrinter == null) {
            return;
        }

        String content = "This is a test command data";
        //content to byteArray
        byte[] bytes = content.getBytes();
        try {
            mPrinter.addCommand(bytes);
        } catch (Epos2Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class PrinterManagerHolder {
        private static final PrinterManager INSTANCE = new PrinterManager();
    }


    /**
     * 格式化三个字符串：左对齐、居中、右对齐
     * @param str1 左侧字符串
     * @param str2 中间字符串
     * @param str3 右侧字符串
     * @param maxLength
     * @return 格式化后的字符串，长度固定为totalWidth
     */
    public static String formatThreeStrings(String str1, String str2, String str3, int maxLength) {
        StringBuilder lineBuilder = new StringBuilder();

//        int length = "------------------------------------------------".length(); // 48 size = 1


        for (int i = 0; i < maxLength; i++) {
            lineBuilder.append("-");
        }

        if (maxLength < 0) {
            throw new IllegalArgumentException("总长度不能为负数");
        }

        // 安全处理null
        if (str1 == null) str1 = "";
        if (str2 == null) str2 = "";
        if (str3 == null) str3 = "";

        int len1 = str1.length();
        int len2 = str2.length();
        int len3 = str3.length();

        // 计算各段最大长度（左右各不超过1/3）
        int maxSegmentWidth = Math.max(0, maxLength / 3);
        int leftWidth = Math.min(len1, maxSegmentWidth);
        int rightWidth = Math.min(len3, maxSegmentWidth);
        int availableMidWidth = Math.max(0, maxLength - leftWidth - rightWidth);
        int midWidth = Math.min(len2, availableMidWidth);

        // 截取子串（右侧字符串取后缀）
        String leftPart = str1.substring(0, Math.min(len1, leftWidth));
        String rightPart = (len3 <= rightWidth) ?
                str3 :
                str3.substring(len3 - rightWidth);
        String midPart = str2.substring(0, Math.min(len2, midWidth));

        // 计算中间区域的剩余空间（用于居中）
        int remainingSpace = maxLength - leftWidth - rightWidth - midWidth;
        int leftPadding = (remainingSpace + 1) / 2; // 居中偏好偏左
        int rightPadding = remainingSpace - leftPadding;

        // 构建最终字符串
        StringBuilder sb = new StringBuilder(maxLength);
        sb.append(leftPart);
        appendSpaces(sb, leftPadding);
        sb.append(midPart);
        appendSpaces(sb, rightPadding);
        sb.append(rightPart);

        return sb.toString();
    }

    private static void appendSpaces(StringBuilder sb, int count) {
        for (int i = 0; i < count; i++) {
            sb.append(' ');
        }
    }

    public String formatDishesStrings(int maxLineLength, String str1, String str2, String str3, String str4) {
        StringBuilder result = new StringBuilder();

        // 校验行长度有效性
        if (maxLineLength <= 0) {
            throw new IllegalArgumentException("行长度必须是正整数");
        }

        // 组合第一部分：str1 + 两个空格 + str2 + 两个空格
        String fullPrefix = str1 + "  " + str2 + "  ";
        int fullPrefixLength = fullPrefix.length();

        // 检查完整前缀是否超过最大行长度
        if (fullPrefixLength > maxLineLength) {
            throw new IllegalArgumentException("前两个参数加上空格的长度超过" + maxLineLength + "字符");
        }

        // 计算第三参数首字符位置
        int thirdCharAlignment = fullPrefixLength;

        // 第一行可用空间
        int firstLineSpace = maxLineLength - fullPrefixLength;

        // 输出完整前缀
        result.append(fullPrefix);

        // 处理第三参数
        if (str3 != null && !str3.isEmpty()) {
            if (str3.length() <= firstLineSpace) {
                // 整个第三参数可以放在第一行
                result.append(str3);
            } else {
                // 输出第一段第三参数
                result.append(str3, 0, firstLineSpace);
                int index = firstLineSpace;

                while (index < str3.length()) {
                    // 换行
                    result.append('\n');

                    // 添加空格以对齐第三参数首字符位置
                    for (int i = 0; i < thirdCharAlignment; i++) {
                        result.append(' ');
                    }

                    // 计算本行可添加的字符数
                    int lineSpace = maxLineLength - thirdCharAlignment;
                    int segmentLength = Math.min(str3.length() - index, lineSpace);
                    result.append(str3, index, index + segmentLength);
                    index += segmentLength;
                }
            }
        }

        // 处理第四个参数（按分号分割）
        if (str4 != null && !str4.isEmpty()) {
            // 首先换行结束第三部分
            if (result.length() > 0 && result.charAt(result.length() - 1) != '\n') {
                result.append('\n');
            }

            // 分割第四个参数
            String[] parts = str4.split(";");

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i].trim();
                if (part.isEmpty()) continue;

                // 如果不是第一部分，添加空行分隔
                if (i > 0) {
                    result.append('\n');
                }

                // 添加对齐空格
                for (int j = 0; j < thirdCharAlignment; j++) {
                    result.append(' ');
                }

                // 处理第四参数的每部分
                int partIndex = 0;
                int lineSpace = maxLineLength - thirdCharAlignment;

                while (partIndex < part.length()) {
                    // 如果不是每部分的第一行，需要换行和对齐
                    if (partIndex > 0) {
                        result.append('\n');
                        for (int j = 0; j < thirdCharAlignment; j++) {
                            result.append(' ');
                        }
                    }

                    // 计算本行可添加的字符数
                    int segmentLength = Math.min(part.length() - partIndex, lineSpace);
                    result.append(part, partIndex, partIndex + segmentLength);
                    partIndex += segmentLength;
                }
            }
        }

        return result.toString();
    }

}
