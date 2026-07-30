package com.allever.compose.project.watch

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.allever.android.lib.common.compose.BaseComposeActivity
import z.compose.app.allever.android.sample.compose.project.R

/**
 *@Description
 *@author: zq
 *@date: 2024/1/3
 */
class WatchMainActivity : BaseComposeActivity() {
    override fun init() {

    }

    @Composable
    override fun ContentPage() {
        //纵向布局
        Column {
            Content()
            NavBar()
        }
    }

    @Composable
    fun ColumnScope.Content() {
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xfffafafa))
        ) {
            TopBar()
            SearchBar()
            TabBar()
            LoveArea()
            GoToArea()
        }
    }


    @Composable
    fun RowScope.NavItem(@DrawableRes iconId: Int, tint: Color) {
        Button(
            onClick = { },
            Modifier
                .weight(1f)
                .fillMaxHeight(), shape = RectangleShape,
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Icon(
                painterResource(iconId), "图标", Modifier
                    .size(24.dp), tint = tint
            )
        }
    }

    @Preview
    @Composable()
    fun NavBar() {
        Row(
            Modifier
                .height(64.dp)
                .padding(0.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(iconId = R.drawable.zcp_input_bar_add, tint = Color(0xfffa9e51))
            NavItem(iconId = R.drawable.zcp_input_bar_add, tint = Color(0xffb4b4b4))
            NavItem(iconId = R.drawable.zcp_input_bar_add, tint = Color(0xffb4b4b4))
            NavItem(iconId = R.drawable.zcp_input_bar_add, tint = Color(0xffb4b4b4))
        }
    }

    @Preview
    @Composable
    fun TopBar() {
//横向
        Row(
            Modifier
                .fillMaxWidth()
                .padding(28.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(id = R.drawable.zcp_ic_header),
                "头像",
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )

            Column(
                Modifier
                    .weight(1f)
                    .padding(10.dp)
            ) {
                Text(text = "欢迎回来!", fontSize = 14.sp, color = Color(0xffb4b4b4))
                Text(text = "Allever", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Surface(Modifier.clip(CircleShape), color = Color(0xfffef7f0)) {
                Icon(
                    painterResource(id = R.drawable.zcp_ic_notification),
                    "通知",
                    Modifier
                        .size(64.dp)
                        .padding(10.dp)
                )
            }

        }
    }

    @Preview
    @Composable
    fun SearchBar() {
        Row(
            Modifier
                .padding(28.dp, 6.dp)
                .clip(CircleShape)
                .fillMaxWidth()
                .height(56.dp)
                .background(color = Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {

            var searchText by remember { mutableStateOf("") }
            BasicTextField(
                value = searchText,
                onValueChange = { searchText = it },
                Modifier
                    .padding(start = 16.dp)
                    .weight(1f), textStyle = TextStyle(fontSize = 16.sp)
            ) {
                if (searchText.isEmpty()) {
                    Text(text = "搜搜看?", color = Color(0xffb4b4b4), fontSize = 16.sp)
                }
                it()
            }

            Box(
                Modifier
                    .padding(6.dp)
                    .clip(CircleShape)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .background(color = Color(0xfffa9e51)),
            ) {
                Icon(
                    painterResource(id = R.drawable.zcp_ic_search),
                    "搜索",
                    Modifier
                        .size(24.dp)
                        .align(Alignment.Center), tint = Color.White
                )
            }

        }
    }

    @Preview
    @Composable
    fun TabBar() {
        val tabList = listOf("推荐", "萌宠", "科技", "影院", "时事", "热点", "人文", "体育", "少儿")
        var selected by remember { mutableStateOf(0) }
        LazyRow(
            Modifier
                .padding(4.dp, 8.dp)
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(12.dp, 0.dp)
        ) {
            itemsIndexed(tabList) { index, item ->
                Column(
                    Modifier
                        .padding(16.dp, 4.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        fontWeight = if (index == selected) FontWeight.Bold else FontWeight.Normal,
                        text = item,
                        color = if (index == selected) Color(0xfffa9e51) else Color(0xffb4b4b4),
                        modifier = Modifier.clickable {
                            selected = index
                        }
                    )

                    if (index == selected) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(color = Color(0xfffa9e51))
                        ) {

                        }
                    }
                }

            }
        }
    }

    @Preview()
    @Composable
    fun LoveArea() {
        Column {
            Row(Modifier.padding(28.dp, 8.dp)) {
                Text(text = "TA 爱的", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(text = "查看全部", color = Color(0xffb4b4b4))
            }


            val loveItemList = mutableListOf<LoveItem>().apply {
                for (i in 0..10) {
                    add(LoveItem(R.drawable.zcp_ic_header, "Title:${i}", "Desc:${i}", i.toFloat()))
                }
            }

            //horizontalArrangement = Arrangement.spacedBy(20.dp) 分割每个item间距
            //contentPadding = PaddingValues(28.dp, 0.dp), 给两头加间距
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(28.dp, 0.dp)
            ) {
                itemsIndexed(loveItemList) { index, item ->
                    Column(
                        Modifier
//                            .padding(8.dp, 0.dp)
                            .width(260.dp)
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color = Color.White)
                    ) {
                        Image(
                            painterResource(id = item.cover),
                            contentDescription = "",
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                                .fillMaxWidth()
                                .height(140.dp),
                            contentScale = ContentScale.Crop
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(text = item.title, fontSize = 17.sp)
                                Text(text = item.desc, color = Color(0xffb4b4b4), fontSize = 12.sp)
                            }

                            Row(
                                Modifier
                                    //clip要写在前面不然没效果
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color = Color(0xfffef7f0))
                                    .padding(8.dp, 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.zcp_ic_star),
                                    contentDescription = "score",
                                    Modifier.size(24.dp),
                                    tint = Color(0xfffa9e51)
                                )

                                Text(
                                    text = item.score.toString(),
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(6.dp, 0.dp),
                                    color = Color(0xfffa9e51)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun GoToArea() {
        Column(Modifier.padding(28.dp, 10.dp)) {
            Text(
                text = "TA 去过",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(0.dp, 8.dp)
            )

            Row(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .fillMaxWidth()
                    .background(color = Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painterResource(id = R.drawable.zcp_ic_header),
                    contentDescription = "头像",
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .size(64.dp)
                )

                Column {
                    Text(text = "5分钟前", fontSize = 12.sp, color = Color(0xffb4b4b4))
                    Text(text = "扔物线学堂")
                    Text(text = "广州", fontSize = 12.sp, color = Color(0xffb4b4b4))
                }
            }
        }
    }
}

data class LoveItem(val cover: Int, val title: String, val desc: String, val score: Float)




