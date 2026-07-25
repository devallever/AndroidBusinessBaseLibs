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
import com.google.zxing.client.result.YoutubeParsedResult;
import com.allever.app.qr.code.scaner.R;

/**
 * Offers appropriate actions for URLS.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class YoutubeResultHandler extends ResultHandler {
    private static final int[] buttons = {
            R.string.button_search_from_youtube,
            R.string.button_copy_link,
            R.string.button_share,
    };

    private static final String YOUTUBE_PACKAGENAME = "com.google.android.youtube";

    public YoutubeResultHandler(Activity activity, ParsedResult result) {
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
        YoutubeParsedResult uriResult = (YoutubeParsedResult) getResult();
        String uri = uriResult.revertRawData();
        switch (index) {
            case 0:
                String userId = uriResult.getUserId();
                if (userId != null && !AppUtils.isAppExist(App.getContext(), YOUTUBE_PACKAGENAME)) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(Uri.parse("https://www.youtube.com/results?search_query=" + userId));
//                    intent.setClassName(YOUTUBE_PACKAGENAME, "com.google.android.youtube.UrlActivity");
//                    launchIntent(intent);
                    uri = String.format("https://www.youtube.com/results?search_query=%s", userId);
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
