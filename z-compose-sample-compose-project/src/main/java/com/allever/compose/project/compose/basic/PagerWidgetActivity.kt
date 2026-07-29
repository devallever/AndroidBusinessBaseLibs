package com.allever.compose.project.compose.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.allever.compose.core.ui.ComposeProjectTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState

/**
 *@Description
 *@author: zq
 *@date: 2024/3/15
 */
class PagerWidgetActivity : ComponentActivity() {
    @OptIn(ExperimentalPagerApi::class)
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
                        text = "水平ViewPager HorizontalPager",
                        Modifier.padding(10.dp),
                        fontWeight = FontWeight.Bold
                    )
                    val pagerState = rememberPagerState(0)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        HorizontalPager(count = 3, state = pagerState) { index ->
                            when (index) {
                                0 -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .background(Color(0x80ff0000))
                                    )
                                }
                                1 -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .background(Color(0x8000ff00))
                                    )
                                }
                                2 -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .background(Color(0x800000ff))
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "垂直ViewPager VerticalPager",
                        Modifier.padding(10.dp),
                        fontWeight = FontWeight.Bold
                    )

                    val pagerStateV = rememberPagerState(0)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        VerticalPager(count = 3, state = pagerStateV) { index ->
                            when (index) {
                                0 -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .background(Color(0x80ff0000))
                                    )
                                }
                                1 -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .background(Color(0x8000ff00))
                                    )
                                }
                                2 -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .background(Color(0x800000ff))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}