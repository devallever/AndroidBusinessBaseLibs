package app.allever.android.sample.login.google

class GoogleUserInfo {
    /**
     * Authorization Code（授权码）
     *
     * 后端用此 Code 向 Google 换取 Token，一次性使用，有效期约 10 分钟。
     * null 表示本次登录未获取到（可能配置未启用 requestServerAuthCode）
     */
    var authCode: String = ""
    /** Google ID Token（JWT 格式） */
    var idToken: String = ""
    /** 用户邮箱 */
    var email: String = ""
    /** 用户显示名称 */
    var displayName: String = ""
    /** 用户头像 URL */
    var photoUrl: String = ""
}