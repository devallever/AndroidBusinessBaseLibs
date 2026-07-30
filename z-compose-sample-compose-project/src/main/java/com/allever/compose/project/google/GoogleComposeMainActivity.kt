package com.allever.compose.project.google

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.allever.android.lib.common.compose.BaseComposeActivity
import app.allever.android.lib.core.compose.base.AbstractComposeActivity
import app.allever.android.lib.common.compose.theme.ComposeProjectTheme

/**
 *@Description
 *@author: Allever
 *@date: 2024/1/5
 */

class GoogleComposeMainActivity : BaseComposeActivity() {
    override fun init() {

    }

    @Composable
    override fun ContentPage() {
        MyApp()
    }
}

@Composable()
fun Greeting(name: String) {
    val expended = rememberSaveable {
        mutableStateOf(false)
    }
    val expendedPadding by animateDpAsState(
        targetValue = if (expended.value) 48.dp else 0.dp,
        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy,
//            stiffness = Spring.StiffnessLow
        )
    )

    Surface(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(Modifier.padding(24.dp)) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(bottom = expendedPadding)
            ) {
                Text(text = "Hello,")
                Text(text = name, style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ))
            }
            ElevatedButton(onClick = {
                expended.value = !expended.value
            }) {
                Text(text = if (expended.value) "Show less" else "Show more")
            }
        }

    }
}

@Composable
fun OnBoardingScreen(callback: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to the Basics Codelab!")
        Button(onClick = { callback.invoke() }, modifier = Modifier.padding(24.dp)) {
            Text(text = "Continue")
        }
    }
}

@Composable
fun MyApp(
    modifier: Modifier = Modifier,
    nameList: List<String> = listOf("Compose Project", "Android")
) {

    val shouldShowOnBoarding = rememberSaveable { mutableStateOf(true) }

    if (shouldShowOnBoarding.value) {
        OnBoardingScreen {
            shouldShowOnBoarding.value = false
        }
    } else {
        //可以用 Surface 包围 可组合项, 设置背景颜色
        val names = List(30) { "${it + 1}" }
        LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
            items(names) {
                Greeting(name = it)
            }
        }
    }

}
