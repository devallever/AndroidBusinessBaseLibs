package app.allever.android.sample.login.google

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.ext.toast
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.SecureRandom
import java.util.Base64

object GoogleCredentialHelper {
    private var clientId = "118701384531-ol3l27ns3ottu5aoaalk1bt24e9ctgpu.apps.googleusercontent.com"

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun launchSign(callback: SignResultCallback? = null) {

        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = clientId)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        val credentialManager = CredentialManager.create(App.context)

        val failureMessage = "Sign in failed!"
        //using delay() here helps prevent NoCredentialException when the BottomSheet Flow is triggered
        //on the initial running of our app
//        delay(2500)
        try {
            // The getCredential is called to request a credential from Credential Manager.
            val result = credentialManager.getCredential(
                request = request,
                context = App.context,
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val userInfo = GoogleUserInfo()
                userInfo.apply {
                    displayName = googleIdTokenCredential.displayName?:""
                    idToken = googleIdTokenCredential.idToken
                    email = googleIdTokenCredential.id
                }
                log("google user = ${userInfo.toJson()}")
                callback?.onSuccess(userInfo)
            }
        } catch (e: GoogleIdTokenParsingException) {
            val msg = "$failureMessage: Issue with parsing received GoogleIdToken"
            logE(msg)
            e.printStackTrace()
            callback?.onError(msg)
        } catch (e: NoCredentialException) {
            val msg = "$failureMessage: No credentials found"
            toast(msg)
            logE(msg)
            e.printStackTrace()
            callback?.onError(msg)
        } catch (e: GetCredentialCancellationException) {
            logE("Sign-in was cancelled")
            e.printStackTrace()
            callback?.onCancel()
        } catch (e: GetCredentialCustomException) {
            val msg = "$failureMessage: Issue with custom credential request"
            logE(msg)
            e.printStackTrace()
            callback?.onError(msg)
        } catch (e: GetCredentialException) {
            val msg = "$failureMessage: Failure getting credentials"
            logE(msg)
            e.printStackTrace()
            callback?.onError(msg)
        }
    }

    //This function is used to generate a secure nonce to pass in with our request
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom.getInstanceStrong().nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }

}