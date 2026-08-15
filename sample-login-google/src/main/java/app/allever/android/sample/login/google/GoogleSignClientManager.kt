package app.allever.android.sample.login.google

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.core.helper.ActivityHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


object GoogleSignClientManager {

    private val RC_SIGN_IN = 0x01
    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    private var clientId = "118701384531-ol3l27ns3ottu5aoaalk1bt24e9ctgpu.apps.googleusercontent.com"

    // Build a GoogleSignInClient with the options specified by gso.
    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestId()
            .requestIdToken(clientId)
            .requestServerAuthCode(clientId)
            .build()
    }

    fun checkLogin(): Boolean {
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        val account = GoogleSignIn.getLastSignedInAccount(App.context)
        return account != null
    }

    fun getLoginUser(): GoogleUserInfo? {
        val account = GoogleSignIn.getLastSignedInAccount(App.context)
        return if (account != null) {
            val userInfo = GoogleUserInfo()
            userInfo.idToken = account.idToken?:""
            userInfo.displayName = account.displayName?:""
            userInfo.email = account.email?:""
            userInfo.photoUrl = account.photoUrl?.toString()?:""
            userInfo.authCode = account.serverAuthCode?:""
            userInfo
        } else {
            null
        }
    }

    fun launchSign(activity: Activity) {
        val mGoogleSignInClient = GoogleSignIn.getClient(ActivityHelper.getTopActivity()!!, gso)
        val signInIntent: Intent? = mGoogleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun signOut() {
        GoogleSignIn.getClient(ActivityHelper.getTopActivity()!!, gso).signOut()
    }

    fun handleResult(requestCode: Int, data: Intent?, callback: SignResultCallback? = null) {

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task, callback)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount?>, callback: SignResultCallback?) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                val userInfo = GoogleUserInfo()
                userInfo.idToken = account.idToken?:""
                userInfo.displayName = account.displayName?:""
                userInfo.email = account.email?:""
                userInfo.photoUrl = account.photoUrl?.toString()?:""
                userInfo.authCode = account.serverAuthCode?:""
                callback?.onSuccess(userInfo)
            } else {
                callback?.onError("login failed")
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            logE("signInResult:failed code=" + e.statusCode)
            e.printStackTrace()
            callback?.onError("login failed")
        }
    }
}