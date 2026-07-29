package com.allever.compose.project.compose.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allever.compose.core.ui.ComposeProjectTheme
import z.compose.app.allever.android.sample.compose.project.R

/**
 *@Description
 *@author: zq
 *@date: 2024/3/15
 */
class GridWidgetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeProjectTheme {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "垂直网格 LazyVerticalGrid",
                        Modifier.padding(10.dp),
                        fontWeight = FontWeight.Bold
                    )

                    val list = mutableListOf<Int>().apply {
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                    }
                    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                        itemsIndexed(list) { index, item ->
                            Column(
                                Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = item),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(
                                            RoundedCornerShape(10.dp)
                                        )
                                )
                            }


                        }
                    }

                    Text(
                        text = "水平网格 LazyHorizontalGrid",
                        Modifier.padding(10.dp),
                        fontWeight = FontWeight.Bold
                    )

                    val listH = mutableListOf<Int>().apply {
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                        add(R.drawable.default_image)
                    }
                    LazyHorizontalGrid(rows = GridCells.Fixed(2), modifier = Modifier.height(240.dp)) {
                        itemsIndexed(listH) {index, item ->
                            Column(
                                Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = item),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(
                                            RoundedCornerShape(10.dp)
                                        )
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}