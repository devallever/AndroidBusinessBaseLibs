package app.allever.android.lib.common.compose.data

class TextClickItem(
    val title: String,
    val desc: String = "",
    val block: (data: TextClickItem) -> Unit = {}
)
