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
    var url: String = ""
) : Message(
    messageId = messageId,
    type = type,
    contentType = ContentType.IMAGE,
    fromUser = fromUser,
    toUser = toUser,
    content = content,
    timestamp = timestamp,
    status = status,
    extras = extras
)