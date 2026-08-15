package app.allever.android.sample.login.google

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.Gravity
import androidx.annotation.RequiresApi
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialCustomException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.router.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.Base64

/***
 * 通过CredentialManager实现Google登录
 * https://codelabs.developers.google.com/sign-in-with-google-android?hl=zh-cn#0
 * 
 */
class LoginByCredentialManagerFragment: ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {

    //replace with your own web client ID from Google Cloud Console
//    val webClientId = "118701384531-0btlra6pkn3ndkoucg70j3hl75h020vv.apps.googleusercontent.com" //android
    val webClientId =
        "118701384531-ol3l27ns3ottu5aoaalk1bt24e9ctgpu.apps.googleusercontent.com" //web client

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter(
        Gravity.CENTER
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(

        TextDetailClickItem("按钮登录") {
            launchGoogleLogin()
        },
        TextDetailClickItem("弹窗登录", "待完善") {
            launchGoogleLoginWithDialog()
        },

        )


    @RequiresApi(Build.VERSION_CODES.O)
    private fun launchGoogleLogin() {
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        lifecycleScope.launch {
            signIn(request, requireContext())
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun launchGoogleLoginWithDialog() {
// Create a Google ID option with filtering by authorized accounts enabled.
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setNonce(generateSecureRandomNonce())
            .build()

        // Create a credential request with the Google ID option.
        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Attempt to sign in with the created request using an authorized account
        lifecycleScope.launch {
            val e = signIn(request, requireContext())
            // If the sign-in fails with NoCredentialException,  there are no authorized accounts.
            // In this case, we attempt to sign in again with filtering disabled.
            if (e is NoCredentialException) {
                val googleIdOptionFalse: GetGoogleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setNonce(generateSecureRandomNonce())
                    .build()

                val requestFalse: GetCredentialRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOptionFalse)
                    .build()

                //We will build out this function in a moment
                signIn(requestFalse, requireContext())
            }
        }
    }

    //This function is used to generate a secure nonce to pass in with our request
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateSecureRandomNonce(byteLength: Int = 32): String {
        val randomBytes = ByteArray(byteLength)
        SecureRandom.getInstanceStrong().nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }

    suspend fun signIn(request: GetCredentialRequest, context: Context): Exception? {
        val credentialManager = CredentialManager.create(context)
        val failureMessage = "Sign in failed!"
        //using delay() here helps prevent NoCredentialException when the BottomSheet Flow is triggered
        //on the initial running of our app
//        delay(2500)
        return try {
            // The getCredential is called to request a credential from Credential Manager.
            val result = credentialManager.getCredential(
                request = request,
                context = context,
            )
            log(result.toJson())

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                log("Signed in as: ${googleIdTokenCredential.id}")//邮箱账号
                log("idToken: ${googleIdTokenCredential.idToken}")//
            }

            toast("Sign in successful!")
            null
        } catch (e: GoogleIdTokenParsingException) {
            toast(failureMessage)
            log("$failureMessage: Issue with parsing received GoogleIdToken")
            e
        } catch (e: NoCredentialException) {
            toast(failureMessage)
            logE("$failureMessage: No credentials found")
            e
        } catch (e: GetCredentialCancellationException) {
            toast("Sign-in cancelled")
            logE("$failureMessage: Sign-in was cancelled")
            e
        } catch (e: GetCredentialCustomException) {
            toast(failureMessage)
            logE("$failureMessage: Issue with custom credential request")
            e
        } catch (e: GetCredentialException) {
            toast(failureMessage)
            logE("$failureMessage: Failure getting credentials")
            e
        }
    }
}