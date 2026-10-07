package com.allever.compose.project.compose.basic

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.allever.android.lib.common.compose.BaseComposeActivity
import app.allever.android.lib.core.ext.toast
import z.compose.app.allever.android.sample.compose.project.R

class ListComposeSampleActivity : BaseComposeActivity() {
    override fun init() {
        initTopBar("ListComposeSample")
    }

    @Composable
    override fun ContentPage() {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LabelView("垂直列表 LazyColumn")
            val userList = listOf(
                User(1, "Alice", R.drawable.zcp_avatar_1),
                User(2, "Bob", R.drawable.zcp_avatar_2),
                User(3, "Charlie", R.drawable.zcp_avatar_me),
            )
            LazyColumn {
                itemsIndexed(userList) { index, user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                toast("Clicked Item")
                            }//换个顺序就能改变点击效果
                            .padding(16.dp, 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = user.avatar),
                            contentDescription = user.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(
                                    CircleShape
                                )
                                .clickable {
                                    toast("Clicked Image")
                                })

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        ) {
                            Text(
                                text = user.name,
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(4.dp)
                            )
                            Text(
                                text = "id: ${user.id}",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }

            //水平列表
            LabelView("水平列表 LazyRow")
            LazyRow {
                itemsIndexed(userList) { index, user ->
                    Card(Modifier
                        .padding(8.dp)
                        .clickable {
                            toast("Clicked Card")
                        }
                        .width(200.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = user.avatar),
                                contentDescription = user.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(
                                        CircleShape
                                    )
                                    .clickable {
                                        toast("Clicked Image")
                                    })
                            Text(
                                text = user.name,
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(4.dp)
                            )
                            Text(
                                text = "id: ${user.id}",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(4.dp)
                            )
                        }

                    }
                }
            }
        }
    }


    private inner class User(val id: Long, val name: String, val avatar: Int)
}