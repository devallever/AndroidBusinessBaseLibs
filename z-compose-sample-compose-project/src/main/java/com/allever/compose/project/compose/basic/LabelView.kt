package com.allever.compose.project.compose.basic

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LabelView(content: String) {
    Text(content, modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold)
}