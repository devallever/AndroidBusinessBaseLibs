package com.allever.business.lib.project.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.allever.android.lib.common.compose.BaseComposeActivity
import app.allever.android.lib.common.compose.theme.ComposeProjectTheme
import app.allever.android.lib.router.annotation.Route

@Route(path = "/appcompose/main")
class MainActivity : BaseComposeActivity() {
    @Composable
    override fun ContentPage() {
//        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//
//        }
        Greeting(
            name = "Android",
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeProjectTheme {
        Greeting("Android")
    }
}