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

import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.WhatsappParsedResult;
import com.allever.app.qr.code.scaner.R;

/**
 * Offers appropriate actions for URLS.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class WhatsappResultHandler extends ResultHandler {
    private static final int[] buttons = {
            R.string.button_search_from_whatsapp,
            R.string.button_copy_link,
            R.string.button_share,
    };

    public WhatsappResultHandler(Activity activity, ParsedResult result) {
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
        WhatsappParsedResult uriResult = (WhatsappParsedResult) getResult();
        String uri = uriResult.revertRawData();
        switch (index) {
            case 0:
                String userId = uriResult.getUserId();
                String packageName = "com.whatsapp";
                if (userId != null && !SocialUtils.isAppExist(packageName)) {
//                    uri = String.format("https://api.whatsapp.com/send?phone=%s", userId);
                    SocialUtils.showDialog(getActivity(), "WhatsApp", packageName);
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
        return com.google.zxing.client.android.R.string.zxing_result_uri;
    }

    @Override
    public boolean areContentsSecure() {
        return false;
    }
}
