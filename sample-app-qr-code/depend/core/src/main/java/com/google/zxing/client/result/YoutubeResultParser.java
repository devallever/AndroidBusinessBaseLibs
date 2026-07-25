/*
 * Copyright 2007 ZXing authors
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

package com.google.zxing.client.result;

import com.google.zxing.Result;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public final class YoutubeResultParser extends ResultParser {

    // <scheme>://<host>:<port>[<path>|<pathPrefix>|<pathPattern>]

    /**
     * <data android:scheme="remote"/>
     * <data android:host="youtube.com"/>
     * <data android:host="www.youtube.com"/>
     * <data android:host="m.youtube.com"/>
     * <data android:pathPrefix="/remote"/>
     * <data android:pathPrefix="/ytremote"/>
     * <p>
     * <data android:scheme="http"/>
     * <data android:scheme="https"/>
     * <data android:host="youtube.com"/>
     * <data android:host="www.youtube.com"/>
     * <data android:host="m.youtube.com"/>
     * <data android:host="youtu.be"/>
     * <p>
     * <data android:scheme="vnd.youtube"/>
     * <data android:scheme="vnd.youtube.launch"/>
     */

    public static final String SEARCH_PREFIX = "vnd.youtube://youtube.com/results?search_query=";

    private Pattern uriPattern = Pattern.compile("(\\b(http|https|remote|vnd\\.youtube)\\b://.*\\.?\\b(youtu|youtube)\\b\\.\\b(be|com)\\b)/(.*)");

    @Override
    public YoutubeParsedResult parse(Result result) {
        String rawText = getMassagedText(result);
        String rawTextLC = rawText.toLowerCase();

        if (rawTextLC.startsWith(SEARCH_PREFIX)) {
            return new YoutubeParsedResult(rawText.substring(SEARCH_PREFIX.length()));
        }

        Matcher matcher = uriPattern.matcher(rawTextLC);
        if (matcher.find()) {
            YoutubeParsedResult youtubeParsedResult = new YoutubeParsedResult();
            youtubeParsedResult.setUrl(rawText);
            return youtubeParsedResult;
        }
        return null;
    }

}