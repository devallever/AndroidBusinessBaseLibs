package app.allever.android.compose.sample.app.tweet.business.login

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.allever.android.compose.sample.app.tweet.base.AppBaseActivity
import app.allever.android.compose.sample.app.tweet.resource.colorPageBg
import app.allever.android.compose.sample.app.tweet.resource.colorTextPrimary

class LoginActivity: AppBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().background(colorPageBg)) {
                Text(
                    text = "Tweet",
                    fontSize = 30.sp,
                    color = colorTextPrimary,
                    modifier = Modifier
                        .padding(vertical = 60.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}