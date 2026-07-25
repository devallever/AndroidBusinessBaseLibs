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
import android.text.TextUtils;

import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.result.EmailAddressParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.allever.app.qr.code.scaner.R;

/**
 * Handles email addresses.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class EmailAddressResultHandler extends ResultHandler {
    private static final int[] buttons = {
            R.string.button_send_email,
            R.string.button_copy_email,
            R.string.button_share
    };

    public EmailAddressResultHandler(Activity activity, ParsedResult result) {
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
    public void handleButtonPress(int index) {
        EmailAddressParsedResult emailResult = (EmailAddressParsedResult) getResult();
        switch (index) {
            case 0:
                sendEmail(emailResult.getTos(),
                        emailResult.getCCs(),
                        emailResult.getBCCs(),
                        emailResult.getSubject(),
                        emailResult.getBody());
                break;
            case 1:
                copy2Clipboard(emailResult.getTos()[0]);
                break;
            case 2: {
                CharSequence body = emailResult.getBody();
                if (TextUtils.isEmpty(body)) {
                    body = getDisplayContents();
                }
                share(body.toString());
                break;
            }
        }
    }

    @Override
    public int getDisplayTitle() {
        return com.google.zxing.client.android.R.string.result_email_address;
    }
}
