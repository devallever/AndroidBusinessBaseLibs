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

import com.android.absbase.App;
import com.android.absbase.utils.AppUtils;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ViberParsedResult;
import com.allever.app.qr.code.scaner.R;

/**
 * Offers appropriate actions for URLS.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViberResultHandler extends ResultHandler {
    private static final int[] buttons = {
            R.string.button_search_from_viber,
            R.string.button_copy_link,
            R.string.button_share,
    };

    public ViberResultHandler(Activity activity, ParsedResult result) {
        super(activity, result);
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
    public Integer getDefaultButtonID() {
        return 0;
    }

    @Override
    public void handleButtonPress(int index) {
        ViberParsedResult uriResult = (ViberParsedResult) getResult();
        String uri = uriResult.revertRawData();
        switch (index) {
            case 0:
                String userId = uriResult.getUserId();
                String packageName = "com.viber.voip";
                if (userId != null && !AppUtils.isAppExist(App.getContext(), packageName)) {
//                    uri = String.format("https://www.viber.com/%s", userId);
                    SocialUtils.showDialog(getActivity(), "Viber", packageName);
                    return;
                }
                openURL(uri);
                break;
            case 1:
                copy2Clipboard(uri);
                break;
            case 2:
                share(uri);
                break;
            default:
                break;
        }
    }

    @Override
    public int getDisplayTitle() {
        return com.google.zxing.client.android.R.string.result_uri;
    }

    @Override
    public boolean areContentsSecure() {
        return false;
    }
}
