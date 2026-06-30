package app.android.allever.gp.quick.project.core

data class RankItem(
    val downloadSpeed: Double,
    val uploadSpeed: Double,
    val networkType: String,
    val model: String,
    val time: String
) {
}