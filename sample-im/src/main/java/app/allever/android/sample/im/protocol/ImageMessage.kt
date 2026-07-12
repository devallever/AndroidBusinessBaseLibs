package app.allever.android.sample.im.protocol

class ImageMessage(
    messageId: String = "",
    type: MessageType = MessageType.PRIVATE,
    fromUser: String = "",
    toUser: String = "",
    content: String = "",
    timestamp: Long = 0,
    status: MessageStatus = MessageStatus.SENDING,
    extras: MutableMap<String, Any?> = mutableMapOf(),
    var width: Int = 0,
    var height: Int = 0,
    var thumbnailUrl: String = ""
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
        override val contentType = ContentType.IMAGE
        override val clazz = ImageMessage::class.java
    }
}