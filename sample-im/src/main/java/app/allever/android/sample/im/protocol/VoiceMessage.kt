package app.allever.android.sample.im.protocol

class VoiceMessage(
    messageId: String = "",
    type: MessageType = MessageType.PRIVATE,
    fromUser: String = "",
    toUser: String = "",
    content: String = "",
    timestamp: Long = 0,
    status: MessageStatus = MessageStatus.SENDING,
    extras: MutableMap<String, Any?> = mutableMapOf(),
    var duration: Int = 0,
    var sampleRate: Int = 0
) : Message(
    messageId = messageId,
    type = type,
    contentType = Companion.contentType,
    fromUser = fromUser,
    toUser = toUser,
    content = content,
    timestamp = timestamp,
    status = status,
    extras = extras
) {
    companion object : MessageTypeDef {
        override val contentType = ContentType.VOICE
        override val clazz = VoiceMessage::class.java
    }
}