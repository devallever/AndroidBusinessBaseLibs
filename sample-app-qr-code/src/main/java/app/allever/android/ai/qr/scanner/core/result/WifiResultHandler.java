/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.allever.android.ai.qr.scanner.core.result;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureHolder;
import com.google.zxing.client.android.R;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.wifi.WifiConfigManager;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.WifiParsedResult;

/**
 * Handles wifi access information.
 *
 * @author Vikram Aggarwal
 * @author Sean Owen
 */
public final class WifiResultHandler extends ResultHandler {

    private static final String TAG = WifiResultHandler.class.getSimpleName();

    private final Activity parent;

    private static final int[] buttons = {
            com.allever.app.qr.code.scaner.R.string.button_connect_wifi,
            com.allever.app.qr.code.scaner.R.string.button_copy_network_name,
            com.allever.app.qr.code.scaner.R.string.button_copy_password,
            com.allever.app.qr.code.scaner.R.string.button_share,
    };

    public WifiResultHandler(Activity activity, ParsedResult result) {
        super(activity, result);
        parent = activity;
    }

    @Override
    public int getButtonCount() {
        return buttons.length;
    }

    @Override
    public int getButtonText(int index) {
        return buttons[index];
    }

    @Override
    public void handleButtonPress(int index) {
        WifiParsedResult wifiResult = (WifiParsedResult) getResult();
        switch (index) {
            case 0: {
                WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager == null) {
                    Log.w(TAG, "No WifiManager available from device");
                    return;
                }
                final Activity activity = getActivity();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity.getApplicationContext(), R.string.wifi_changing_network, Toast.LENGTH_SHORT).show();
                    }
                });
                new WifiConfigManager(wifiManager).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, wifiResult);
                if (parent instanceof CaptureHolder) {
                    ((CaptureHolder) parent).restartPreviewAfterDelay(0L);
                }
                break;
            }
            case 1: {
                copy2Clipboard(wifiResult.getSsid());
                break;
            }
            case 2: {
                copy2Clipboard(wifiResult.getPassword());
                break;
            }
            case 3: {
                share(wifiResult.getDisplayResult());
                break;
            }
        }
    }

    // Display the name of the network and the network type to the user.
    @Override
    public CharSequence getDisplayContents() {
        WifiParsedResult wifiResult = (WifiParsedResult) getResult();
        String hidden = wifiResult.isHidden() ? "Yes" : "No";
        String ret = String.format("SSID: %s\nSecurity: %s\nPassword: %s\nHidden Network: %s",
                wifiResult.getSsid(),
                wifiResult.getNetworkEncryption(), wifiResult.getPassword(), hidden);
//        return wifiResult.getSsid() + " (" + wifiResult.getNetworkEncryption() + ')';
        return ret;
    }

    @Override
    public int getDisplayTitle() {
        return R.string.result_wifi;
    }
}