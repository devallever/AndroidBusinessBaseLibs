/*
 * Copyright (C) guolin, Suzhou Quxiang Inc. Open source codes for study only.
 * Do not use for commercial purpose.
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

package com.funny.gif.memes.func.network.response.giffun

import com.google.gson.annotations.SerializedName


/**
 * 初始化请求的实体类封装。
 *
 * @author guolin
 * @since 17/2/12
 */
class InitResponse : GifFunResponse() {

    /**
     * 基本的url地址头，应当根据返回的url地址头来去组装所有后续的访问接口。
     */
    var base: String = ""

    /**
     * 新的token，重新延长了有效限。只有在初始化时传入了老的token，才会有新的token返回。
     */
    var token: String = ""

    /**
     * 已登录用户的头像。只有在初始化时传入了正确的token，才会有返回此字段。
     */
    var avatar: String = ""

    /**
     * 已登录用户的背景图。只有在初始化时传入了正确的token，才会有返回此字段。
     */
    @SerializedName("bg_image")
    var bgImage: String = ""

    /**
     * 是否存在版本更新。
     */
    @SerializedName("has_new_version")
    var hasNewVersion = false

    /**
     * 版本更新的具体信息。
     */
    var version: Version? = null
}
