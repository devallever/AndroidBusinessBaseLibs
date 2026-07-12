package app.allever.android.sample.im.protocol

enum class MessageStatus(val value: Int) {
    SENDING(0),
    SENT(1),
    DELIVERED(2),
    READ(3);

    companion object {
        fun fromValue(value: Int): MessageStatus = 
            values().find { it.value == value } ?: SENDING
    }
}