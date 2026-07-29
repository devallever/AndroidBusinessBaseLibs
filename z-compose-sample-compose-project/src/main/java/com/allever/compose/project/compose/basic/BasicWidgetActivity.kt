package com.allever.compose.project.compose.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.compose.BaseComposeActivity
import app.allever.android.lib.core.compose.base.AbstractComposeActivity
import coil.compose.AsyncImage
import app.allever.android.lib.core.ext.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import z.compose.app.allever.android.sample.compose.project.R

/**
 *@Description
 *@author: zq
 *@date: 2024/3/15
 */
class BasicWidgetActivity : BaseComposeActivity() {

    @Composable
    fun AlertDialogExample(
        onDismissRequest: () -> Unit,
        onConfirmation: () -> Unit,
        dialogTitle: String,
        dialogText: String,
        icon: Int,
    ) {
        AlertDialog(
            icon = {
                Icon(
                    painterResource(id = icon),
                    contentDescription = "Example Icon",
                    modifier = Modifier
                        .size(42.dp)
                        .padding(10.dp)
                )
            },
            title = {
                Text(text = dialogTitle)
            },
            text = {
                Text(text = dialogText)
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }

    @Composable
    fun CustomDialogView(
        onDismissRequest: () -> Unit,
        onConfirmation: () -> Unit,
    ) {
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CustomDialogView",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )

                    Text(
                        text = "CustomDialogView Message",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                    Row {
                        TextButton(onClick = {
                            onConfirmation.invoke()
                        }) {
                            Text(text = "Confirm")
                        }

                        TextButton(onClick = {
                            onDismissRequest.invoke()
                        }) {
                            Text(text = "Dismiss")
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun ContentPage() {
        var showAlertDialog by remember {
            mutableStateOf(false)
        }
        if (showAlertDialog) {
            AlertDialogExample(
                onDismissRequest = { showAlertDialog = false },
                onConfirmation = { showAlertDialog = false },
                dialogTitle = "AlertDialogExample",
                dialogText = "This is AlertDialogExample",
                icon = R.drawable.zcp_ic_menu
            )
        }

        var customDialog by remember {
            mutableStateOf(false)
        }
        if (customDialog) {
            CustomDialogView(onDismissRequest = {
                customDialog = false
            }, onConfirmation = {
                customDialog = false
            })
        }

        //线性布局, 占满全屏
        Column(
            modifier = Modifier
                .fillMaxWidth()//占满宽度
                .fillMaxHeight()//占满高度
                .verticalScroll(
                    rememberScrollState()//垂直滚动
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "滚动布局 verticalScroll", Modifier.padding(10.dp))

            Text(text = "垂直布局 Column", Modifier.padding(10.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0x80ff0000))
            ) {
            }

            Text(text = "水平布局 Row", Modifier.padding(10.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0x8000ff00))
            ) {
            }

            Text(
                text = "文本用 Text",
                modifier = Modifier
                    .padding(10.dp)
                    .background(Color.Cyan),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(text = "本地图片 Image", Modifier.padding(10.dp))

            Image(
                painter = painterResource(id = R.drawable.zcp_default_image),
                contentDescription = ""
            )

            Text(
                text = "本地图片(圆角), clip(RoundedCornerShape(10.dp))",
                Modifier.padding(10.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.zcp_default_image),
                contentDescription = "",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .width(100.dp)
            )

            Text(text = "本地图片(圆形), clip(CircleShape)", Modifier.padding(10.dp))

            Image(
                painter = painterResource(id = R.drawable.zcp_default_image),
                contentDescription = "",
                modifier = Modifier
                    .clip(CircleShape)
                    .width(200.dp)
            )

            Text(text = "网络图片, AsyncImage", Modifier.padding(10.dp))
            AsyncImage(
                model = "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fup.enterdesk.com%2Fedpic_source%2F81%2Ff8%2Fc2%2F81f8c2d8bea55c7b77ba0c4446f2e6a1.jpg&refer=http%3A%2F%2Fup.enterdesk.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1670503104&t=7dec4bfe2fb8646de9f063bc6aa92e0d",
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(
                        RoundedCornerShape(10.dp)
                    )
            )

            Text(text = "按钮 Button, 要包裹Text", Modifier.padding(10.dp))

            Button(
                onClick = {
                    toast("click button")
                }, modifier = Modifier
                    .width(200.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff000000),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Click Me")
            }

            Text(text = "实心按钮 Button", Modifier.padding(10.dp))
            Button(onClick = { toast("Button") }) {
                Text(text = "Button")
            }

            Text(text = "颜色填充按钮 FilledTonalButton", Modifier.padding(10.dp))
            FilledTonalButton(onClick = { toast("FilledTonalButton") }) {
                Text(text = "FilledTonalButton")
            }

            Text(text = "轮廓按钮 OutlinedButton", Modifier.padding(10.dp))
            OutlinedButton(onClick = { toast("OutlinedButton") }) {
                Text(text = "OutlinedButton")
            }

            Text(text = "阴影按钮 ElevatedButton", Modifier.padding(10.dp))
            ElevatedButton(onClick = { toast("ElevatedButton") }) {
                Text(text = "ElevatedButton")
            }

            Text(text = "文本按钮 TextButton", Modifier.padding(10.dp))
            TextButton(onClick = { toast("TextButton") }) {
                Text(text = "TextButton")
            }

            Text(text = "图标按钮 IconButton", Modifier.padding(10.dp))
            IconButton(onClick = { toast("IconButton") }) {
                Icon(
                    painter = painterResource(id = R.drawable.zcp_ic_menu),
                    contentDescription = "",
                    modifier = Modifier
                        .size(42.dp)
                        .padding(12.dp)
                )
            }

            Text(text = "悬浮按钮 FloatingActionButton", Modifier.padding(10.dp))
            FloatingActionButton(onClick = {
                toast("FloatingActionButton")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.zcp_ic_menu),
                    contentDescription = "",
                    modifier = Modifier
                        .size(42.dp)
                        .padding(12.dp)
                )
            }

            Text(text = "扩展悬浮按钮 ExtendedFloatingActionButton", Modifier.padding(10.dp))
            ExtendedFloatingActionButton(onClick = {
                toast("ExtendedFloatingActionButton")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.zcp_ic_menu),
                    contentDescription = "",
                    modifier = Modifier
                        .size(42.dp)
                        .padding(12.dp)
                )
                Text(text = "ExtendedFloatingActionButton")
            }

            Text(text = "输入框 BasicTextField", Modifier.padding(10.dp))

            var content by remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))//要写在background上面
                    .background(color = Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = content, onValueChange = {
                        content = it
                    }, modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                    singleLine = true
                ) {
                    if (content.isEmpty()) {
                        Text(
                            text = "请输入内容",
                            color = Color(0xffb4b4b4),
                            fontSize = 16.sp
                        )
                    }
                    it()
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )
            Button(
                onClick = {
                    if (content.isEmpty()) {
                        toast("please input content")
                        return@Button
                    }
                    toast(content)
                }, modifier = Modifier
                    .width(200.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff000000),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Confirm")
            }

            Text(text = "选中框 CheckBox", Modifier.padding(10.dp))

            var checkBoxChecked by remember { mutableStateOf(false) }
            Checkbox(checked = checkBoxChecked, onCheckedChange = {
                toast("is Checked = $it")
                checkBoxChecked = it
            })

            Text(text = "单选 组合RadioButton", Modifier.padding(10.dp))
            val radioButtonText = listOf("A", "B", "C", "D")
            var radioButtonTag by remember {
                mutableStateOf("")
            }
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                radioButtonText.forEach {
                    RadioButton(selected = it == radioButtonTag, onClick = {
                        toast(it)
                        radioButtonTag = it
                    })

                    Text(text = it)
                }
            }

            Text(text = "开关 Switch", Modifier.padding(10.dp))

            var switchCheck by remember {
                mutableStateOf(false)
            }
            Switch(checked = switchCheck, onCheckedChange = {
                switchCheck = it
            })


            Text(text = "拖动进度条 Slide", Modifier.padding(10.dp))
            var slideValue by remember {
                mutableStateOf(0F)
            }
            Slider(
                valueRange = 0f..100f,
                modifier = Modifier.padding(horizontal = 16.dp),
                value = slideValue,
                onValueChange = {
                    slideValue = it
                },
                onValueChangeFinished = {
                    toast("slideValue: ${slideValue.toInt()}")
                })

            Text(text = "日期选择器 DatePicker", Modifier.padding(10.dp))
            val datePickerState = rememberDatePickerState()
            DatePicker(state = datePickerState)
            Text(
                "当前选中日期的时间戳 ${datePickerState.selectedDateMillis ?: "没有选择"}",
                modifier = Modifier.padding(10.dp)
            )

            Text(text = "时间选择器 TimePicker", Modifier.padding(10.dp))
            val timePickerState = rememberTimePickerState()
            TimePicker(state = timePickerState)
            Text(
                "当前选中的时间 ${timePickerState.hour}:${timePickerState.minute}",
                modifier = Modifier.padding(10.dp)
            )

            Text(text = "卡片 Card", Modifier.padding(10.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp)
            ) {

            }


            Text(text = "顶部状态栏 Card", Modifier.padding(10.dp))

            TopAppBar(
                title = {
                    Text(text = "TopAppBar", fontSize = 16.sp)
                }, modifier = Modifier, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0x80333333)
                ), navigationIcon = {
                    Image(
                        painter = painterResource(id = R.drawable.zcp_ic_menu),
                        contentDescription = "",
                        modifier = Modifier
                            .size(42.dp)
                            .padding(12.dp)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        toast("menu1")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.zcp_ic_menu),
                            contentDescription = "",
                            modifier = Modifier
                                .size(42.dp)
                                .padding(12.dp)
                        )
                    }
                    IconButton(onClick = {
                        toast("menu2")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.zcp_ic_menu),
                            contentDescription = "",
                            modifier = Modifier
                                .size(42.dp)
                                .padding(12.dp)
                        )
                    }
                }
            )

            Text(text = "系统默认弹窗 AlertDialog", Modifier.padding(10.dp))
            Button(onClick = {
                showAlertDialog = true
            }) {
                Text(text = "AlertDialog")
            }

            Text(text = "自定义弹窗 Dialog", Modifier.padding(10.dp))
            Button(onClick = {
                customDialog = true
            }) {
                Text(text = "Dialog")
            }


            Text(text = "圆形进度 CircularProgressIndicator", Modifier.padding(10.dp))

            var circleProgressValue by remember {
                mutableStateOf(0.3f)
            }
            Text(text = "带进度的圆形进度 CircularProgressIndicator", Modifier.padding(10.dp))
            CircularProgressIndicator(progress = circleProgressValue)
            Button(onClick = {
                lifecycleScope.launch {
                    for (i in 0..100) {
                        circleProgressValue = i.toFloat() / 100
                        delay(10)
                    }
                }
            }, modifier = Modifier.padding(10.dp)) {
                Text(text = "启动进度")
            }

            Text(text = "水平进度 LinearProgressIndicator", Modifier.padding(10.dp))

            var linearProgressValue by remember {
                mutableStateOf(0.3f)
            }
            Text(text = "带进度的水平进度 LinearProgressIndicator", Modifier.padding(10.dp))
            LinearProgressIndicator(progress = linearProgressValue)
            Button(onClick = {
                lifecycleScope.launch {
                    for (i in 0..100) {
                        linearProgressValue = i.toFloat() / 100
                        delay(10)
                    }
                }
            }, modifier = Modifier.padding(10.dp)) {
                Text(text = "启动进度")
            }

            Text(text = "TabLayout TabRow", Modifier.padding(10.dp))
            val tabTitles = listOf("Tab1", "Tab2", "Tab3")
            var tabIndex by remember {
                mutableStateOf(0)
            }
            TabRow(selectedTabIndex = tabIndex) {
                tabTitles.forEachIndexed { index, s ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = {
                            tabIndex = index
                        },
                        text = {
                            Text(text = s)
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.zcp_ic_menu),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(8.dp)
                            )
                        })
                }
            }

            Text(text = "TabLayout TabRow 自定义Tab", Modifier.padding(10.dp))
            var customTabIndex by remember {
                mutableStateOf(0)
            }
            TabRow(indicator = {}, divider = {}, selectedTabIndex = customTabIndex) {
                tabTitles.forEachIndexed { index, s ->
                    Tab(
                        selectedContentColor = Color.Black,
                        unselectedContentColor = Color.Gray,
                        selected = customTabIndex == index,
                        onClick = { customTabIndex = index }) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.zcp_ic_menu),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(6.dp)
                            )
                            Text(
                                text = s,
                                modifier = Modifier.padding(2.dp),
                                fontWeight = if (customTabIndex == index) FontWeight.Bold else FontWeight.Light
                            )
                        }
                    }
                }
            }

            Text(text = "滚动的Tab ScrollableTabRow", Modifier.padding(10.dp))
            val scrollTabTitles = mutableListOf<String>()
            for (i in 1..10) {
                scrollTabTitles.add("Tab${i}")
            }
            var scrollTabIndex by remember {
                mutableStateOf(0)
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            )


        }
    }


}