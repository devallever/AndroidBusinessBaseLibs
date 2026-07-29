package com.allever.compose.core.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationView(
    tabEntity: MutableList<TabEntity>,
    tabIndex: Int,
    selectedColor: Color = Color.Black,
    unSelectColor: Color = Color.Gray,
    onTabSelected: (index: Int) -> Unit = {}
) {
    TabRow(selectedTabIndex = tabIndex, divider = {}, indicator = {}) {
        tabEntity.forEachIndexed { index, entity ->
            Tab(
                selectedContentColor = selectedColor,
                unselectedContentColor = unSelectColor,
                selected = tabIndex == index,
                onClick = {
                    onTabSelected(index)
                }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                ) {
                    Icon(
                        painter = painterResource(id = entity.selectedIcon),
                        contentDescription = "",
                        modifier = Modifier
                            .size(32.dp)
                            .padding(6.dp)
                    )
                    Text(
                        text = entity.label,
                        modifier = Modifier.padding(2.dp),
                        fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.Light
                    )
                }
            }
        }
    }
}

data class TabEntity(
    val label: String,
    val selectedIcon: Int,
    val unSelectIcon: Int,
)