/*
 * Copyright 2012 ZXing authors
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

package com.google.zxing.client.android.history;

import com.google.zxing.Result;

/**
 * Represents one item in the scan history.
 */
public final class HistoryItem {

  private final int id;
  private final Result result;
  private final String display;
  private final String details;

  public HistoryItem(int id, Result result, String display, String details) {
    this.id = id;
    this.result = result;
    this.display = display;
    this.details = details;
  }

  public int getId() {
    return this.id;
  }

  public Result getResult() {
    return result;
  }

  public String getDisplayAndDetails() {
    StringBuilder displayResult = new StringBuilder();
    if (display == null || display.isEmpty()) {
      displayResult.append(result.getText());
    } else {
      displayResult.append(display);
    }
    if (details != null && !details.isEmpty()) {
      displayResult.append(" : ").append(details);
    }
    return displayResult.toString();
  }

}
