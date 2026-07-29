package com.allever.compose.core

class TextClickItem(val title: String, val desc: String, val block:(data: TextClickItem)->Unit = {}) {
}