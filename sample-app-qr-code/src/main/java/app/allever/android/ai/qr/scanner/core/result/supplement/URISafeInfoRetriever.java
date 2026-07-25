/*
 * Copyright (C) 2010 ZXing authors
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

package app.allever.android.ai.qr.scanner.core.result.supplement;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.google.zxing.client.android.HttpHelper;
import com.google.zxing.client.android.R;
import com.google.zxing.client.android.history.HistoryManager;
import com.google.zxing.client.result.URIParsedResult;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import kotlin.text.Regex;

final class URISafeInfoRetriever extends SupplementalInfoRetriever {

    private final int TYPE_SECURE = 1;
    private final int TYPE_MALICIOUS = 2;
    private final int TYPE_UNKNOWN = 3;

    private final URIParsedResult result;
    private final String uri;
    private WeakReference<Context> contextRef;

    URISafeInfoRetriever(TextView textView, URIParsedResult result, HistoryManager historyManager, Context context) {
        super(textView, historyManager);
        this.result = result;
        this.uri = result.getURI();
        this.contextRef = new WeakReference<>(context);
    }

    @Override
    void retrieveSupplementalInfo() {
        CharSequence contents = null;
        String requestURI = "https://transparencyreport.google.com/transparencyreport/api/v3/safebrowsing/status?site=" + uri;
        int type = TYPE_UNKNOWN;
        try {
            contents = HttpHelper.downloadViaHttp(requestURI, HttpHelper.ContentType.HTML, 4096);
            if (!TextUtils.isEmpty(contents)) {
                List<String> list = new Regex("sb.ssr\",").split(contents, 0);
                if (list != null && list.size() > 1) {
                    String line = list.get(1);
                    if (!TextUtils.isEmpty(line)) {
                        char c = line.charAt(0);
                        try {
                            int code = Integer.parseInt(String.valueOf(c));
                            switch (code) {
                                case 2:
                                    type = TYPE_MALICIOUS;
                                    break;
                                case 1:
                                    type = TYPE_SECURE;
                                    break;
                                default:
                                    type = TYPE_SECURE;
                                    break;
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }
        } catch (IOException ioe) {
            // ignore this
            return;
        }
        setType(type);
    }

    private void setType(int type) {
        String info = "";
        switch (type) {
            case TYPE_SECURE:
                info = "  Secure URL";
                break;
            case TYPE_MALICIOUS:
                info = "  Malicious URL";
                break;
            case TYPE_UNKNOWN:
                info = "  Unknown website";
                break;
        }
        SpannableString spannableString = new SpannableString("  " + info);
        Drawable drawable = contextRef.get().getResources().getDrawable(R.drawable.launcher_icon);
        drawable.setBounds(0, 0, 48, 48);
        ImageSpan imageSpan = new ImageSpan(drawable);
        spannableString.setSpan(imageSpan, 0, 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        append(uri, spannableString);
    }
}
