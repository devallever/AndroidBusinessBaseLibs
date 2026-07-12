package app.allever.android.sample.im.protocol

enum class MessageType(val value: Int) {
    PRIVATE(1),
    GROUP(2),
    SYSTEM(3),
    BROADCAST(4);

    companion object {
        fun fromValue(value: Int): MessageType = 
            values().find { it.value == value } ?: PRIVATE
    }
}