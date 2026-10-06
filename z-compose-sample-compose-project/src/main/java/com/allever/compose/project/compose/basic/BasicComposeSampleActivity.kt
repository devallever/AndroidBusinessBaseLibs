package com.allever.compose.project.compose.basic

import android.widget.Button
import android.widget.ImageButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Checkbox
import androidx.compose.ui.semantics.Role.Companion.RadioButton
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.compose.BaseComposeActivity
import app.allever.android.lib.core.ext.toast
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import z.compose.app.allever.android.sample.compose.project.R

class BasicComposeSampleActivity : BaseComposeActivity() {
    override fun init() {
        initTopBar("Basic Compose Sample")
    }

    @Composable
    override fun ContentPage() {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(0.dp, 10.dp), horizontalAlignment = Alignment.CenterHorizontally //水平居中
        ) {
            LabelView("滚动布局(垂直)：Column + Modifier.verticalScroll")

            LabelView("垂直布局：Column <=> LinearLayout")
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0x8000FF00))
            )

            LabelView("水平布局：Row <=> LinearLayout")
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0x80FF0000))
            ) {
                LabelView("Row")
                LabelView("Column")
                LabelView("Box")
            }

            LabelView("容器 Box <=> FrameLayout")
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF0000FF))
            )

            //卡片
            LabelView("卡片 Card <=> CardView")
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
            ) {}

            LabelView("文本 Text <=> TextView")
            Text(
                "Content",
                modifier = Modifier
                    .padding(10.dp)// 外边距，无背景
                    .background(Color(0xFFFF0000))
                    .padding(10.dp), // 内边距，有背景
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )

            LabelView("图标 Image，无点击")
            Image(
                painter = painterResource(id = R.drawable.zcp_ic_living),
                contentDescription = "Image",
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .padding(10.dp),
                colorFilter = ColorFilter.tint(Color.Red), // 过滤
                contentScale = ContentScale.Inside, // 内容比例
//                onClick = { /* Handle click */ } //没有点击功能
            )

            Row {
                Image(
                    painter = painterResource(id = R.drawable.zcp_avatar_1),
                    contentDescription = "Image",
                    modifier = Modifier
                        .padding(10.dp)
                        .width(100.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Image(
                    painter = painterResource(id = R.drawable.zcp_avatar_1),
                    contentDescription = "Image",
                    modifier = Modifier
                        .padding(10.dp)
                        .width(100.dp)
                        .height(100.dp)
                        .clip(CircleShape)
                )
            }

            LabelView("网络图片 AsyncImage")
            AsyncImage(
                model = "https://img2.baidu.com/it/u=2919605790,3031864974&fm=253&fmt=auto&app=138&f=JPEG?w=693&h=500",
                contentDescription = "AsyncImage",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            LabelView("原始按钮 Button, 包裹Text")
            Button(
                onClick = { toast("Click Me") }, modifier = Modifier.padding(10.dp)
            ) {
                Text("Click Me")
            }

            LabelView("样式按钮 Button")
            Button(
                onClick = { toast("Click Me") },
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black, contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Click Me")
            }

            LabelView("颜色填充按钮 FilledTonalButton")
            FilledTonalButton(onClick = { toast("Click Me：FilledTonalButton") }) {
                Text("FilledTonalButton")
            }

            LabelView("边框按钮 OutlinedButton")
            OutlinedButton(onClick = { toast("Click Me：OutlinedButton") }) {
                Text("OutlinedButton")
            }

            //阴影按钮
            LabelView("阴影按钮 ElevatedButton")
            ElevatedButton(onClick = { toast("Click Me：ElevatedButton") }) {
                Text("ElevatedButton")
            }

            //文本按钮
            LabelView("文本按钮 TextButton")
            TextButton(onClick = { toast("Click Me：TextButton") }) {
                Text("TextButton")
            }

            //图标按钮
            LabelView("图标按钮 IconButton")
            IconButton(onClick = { toast("Click Me：IconButton") }) {
                Image(
                    painter = painterResource(id = R.drawable.zcp_ic_living),
                    contentDescription = "IconButton",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Inside
                )
            }

            //悬浮按钮
            LabelView("悬浮按钮 FloatingActionButton")
            Row {
                FloatingActionButton(
                    onClick = { toast("Click Me：FloatingActionButton") },
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text("FloatingActionButton", modifier = Modifier.padding(10.dp))
                }
                FloatingActionButton(
                    onClick = { toast("Click Me：FloatingActionButton") },
                    modifier = Modifier.padding(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.zcp_ic_living),
                        contentDescription = "FloatingActionButton"
                    )
                }
            }


            //悬浮扩展按钮
            LabelView("悬浮扩展按钮 ExtendedFloatingActionButton")
            ExtendedFloatingActionButton(onClick = { toast("Click Me：ExtendedFloatingActionButton") }) {
                Image(
                    painter = painterResource(id = R.drawable.zcp_ic_menu),
                    contentDescription = "",
                    modifier = Modifier
                        .size(42.dp)
                        .padding(12.dp),
                    contentScale = ContentScale.Inside
                )
                Text("ExtendedFloatingActionButton")
            }

            //输入框
            val textFieldContent = remember { mutableStateOf("") }
            LabelView("输入框 TextField")
            TextField(
                value = textFieldContent.value,
                onValueChange = { textFieldContent.value = it },
                label = { Text("Label") })
            LabelView("输入框 BasicTextField")
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp)) //裁切要在background之上
                    .background(Color.White)
            ) {
                BasicTextField(
                    value = textFieldContent.value,
                    onValueChange = {
                        textFieldContent.value = it
                    },
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                    singleLine = true
                ) {
                    if (textFieldContent.value.isEmpty()) {
                        Text(
                            text = "请输入内容", color = Color(0xffb4b4b4)
                        )
                    }
                    it()
                }
            }

            Button(onClick = {
                if (textFieldContent.value.isEmpty()) {
                    toast("Please enter some text")
                    return@Button
                }
                toast("You entered: ${textFieldContent.value}")
            }) {
                Text("Confirm")
            }

            //复选框
            LabelView("复选框 Checkbox")
            val checkboxState = remember { mutableStateOf(false) }
            Checkbox(
                checked = checkboxState.value, onCheckedChange = {
                    checkboxState.value = it
                    toast("Checkbox is ${if (it) "checked" else "unchecked"}")
                })

            //单选框
            LabelView("单选框 RadioButton")
            val radioButtonText = listOf("A", "B", "C", "D")
            var radioButtonTag by remember {
                mutableStateOf("")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)
            ) {
                radioButtonText.forEach {
                    RadioButton(
                        selected = radioButtonTag == it, onClick = {
                            radioButtonTag = it
                            toast(it)
                        })
                    Text(text = it)
                }
            }

            //开关
            LabelView("开关 Switch")
            val switchState = remember { mutableStateOf(false) }
            Switch(
                checked = switchState.value, onCheckedChange = {
                    switchState.value = it
                    toast("Switch is ${if (it) "checked" else "unchecked"}")
                })

            //圆形进度条
            val progressState = remember { mutableFloatStateOf(1f) }
            LabelView("圆形进度条 CircularProgressIndicator")
            CircularProgressIndicator(
                progress = { progressState.floatValue },
                modifier = Modifier
                    .size(54.dp)
                    .padding(10.dp)
            )
            //水平进度条
            LinearProgressIndicator(
                progress = { progressState.floatValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
            Button(onClick = {
                lifecycleScope.launch {
                    for (i in 0..100) {
                        progressState.floatValue = i.toFloat() / 100
                        delay(10)
                    }
                }
            }) {
                Text("更新进度")
            }

            //TabLayout + TabRow
            LabelView("TabRow + Tab <=> TabLayout")
            val tabText = listOf("Tab1", "Tab2", "Tab3")
            val tabIndex = remember { mutableIntStateOf(0) }
            TabRow(selectedTabIndex = tabIndex.intValue, modifier = Modifier.fillMaxWidth()) {
                tabText.forEachIndexed { index, text ->
                    Tab(
                        selected = tabIndex.intValue == index,
                        onClick = { tabIndex.intValue = index },
                        text = { Text(text) },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.zcp_ic_living),
                                contentDescription = "Tab",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(8.dp)
                            )
                        })
                }
            }

            LabelView("自定义 TabRow + Tab <=> TabLayout")
            TabRow(indicator = {}, divider = {}, selectedTabIndex = tabIndex.intValue, modifier = Modifier.fillMaxWidth()) {
                tabText.forEachIndexed { index, text ->
                    Tab(
                        selectedContentColor = Color.Black,
                        unselectedContentColor = Color.Gray,
                        selected = tabIndex.intValue == index,
                        onClick = { tabIndex.intValue = index }) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                contentScale = ContentScale.Inside,
                                painter = painterResource(id = R.drawable.zcp_ic_living),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(0.dp, 8.dp, 0.dp, 0.dp)
                            )
                            Text(
                                text = text,
                                modifier = Modifier.padding(0.dp, 2.dp, 0.dp, 10.dp),
                                fontWeight = if (tabIndex.intValue == index) FontWeight.Bold else FontWeight.Light
                            )
                        }
                    }
                }
            }

            //
            LabelView("滚动的Tab")
            Text(text = "滚动的Tab ScrollableTabRow", Modifier.padding(10.dp))
            val scrollTabTitles = mutableListOf<String>()
            for (i in 1..10) {
                scrollTabTitles.add("Tab${i}")
            }
            var scrollTabIndex by remember {
                mutableIntStateOf(0)
            }
            ScrollableTabRow(
                selectedTabIndex = scrollTabIndex,
                edgePadding = 0.dp,
                divider = {}) {
                scrollTabTitles.forEachIndexed { index, s ->
                    Tab(
                        selected = scrollTabIndex == index,
                        onClick = {
                            scrollTabIndex = index
                        },
                        text = {
                            Text(text = s)
                        })
                }

            }
        }
    }

    @Composable
    private fun LabelView(content: String) {
        Text(content, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
    }
}