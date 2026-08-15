package app.allever.android.sample.login.google

interface SignResultCallback {
    fun onSuccess(googleUserInfo: GoogleUserInfo)
    fun onError(msg: String)
    fun onCancel()
}