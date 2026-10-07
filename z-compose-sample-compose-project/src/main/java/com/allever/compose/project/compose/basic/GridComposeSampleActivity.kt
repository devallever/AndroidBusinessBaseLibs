package com.allever.compose.project.compose.basic

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.allever.android.lib.common.compose.BaseComposeActivity
import z.compose.app.allever.android.sample.compose.project.R

class GridComposeSampleActivity : BaseComposeActivity() {
    override fun init() {
        initTopBar("GridComposeSampleActivity")
    }

    @Composable
    override fun ContentPage() {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LabelView("垂直网格 LazyVerticalGrid")
            val avatarList = listOf<Int>(
                R.drawable.zcp_avatar_1,
                R.drawable.zcp_avatar_1,
                R.drawable.zcp_avatar_1,
                R.drawable.zcp_avatar_1,
                R.drawable.zcp_avatar_1,
                R.drawable.zcp_avatar_1,
                R.drawable.zcp_avatar_1,
                R.drawable.zcp_avatar_1,
            )
            LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                itemsIndexed(avatarList) { index, item ->
                    Image(
                        painter = painterResource(id = item),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(96.dp)
                            .clip(
                                RoundedCornerShape(12.dp)
                            ),
                        contentScale = ContentScale.Crop

                    )
                }
            }

            //水平网格
            LabelView("水平网格 LazyHorizontalGrid")
            LazyHorizontalGrid(rows = GridCells.Fixed(2)) {
                itemsIndexed(avatarList) { index, item ->
                    Image(
                        painter = painterResource(id = item),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(72.dp)
                            .clip(
                                RoundedCornerShape(12.dp)
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}