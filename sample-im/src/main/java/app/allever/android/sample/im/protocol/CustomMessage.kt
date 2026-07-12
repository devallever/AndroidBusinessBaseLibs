package app.allever.android.sample.im.protocol

class CustomMessage(
    messageId: String = "",
    type: MessageType = MessageType.PRIVATE,
    fromUser: String = "",
    toUser: String = "",
    content: String = "",
    timestamp: Long = 0,
    status: MessageStatus = MessageStatus.SENDING,
    extras: MutableMap<String, Any?> = mutableMapOf(),
    var customType: String = ""
) : Message(
    messageId = messageId,
    type = type,
    contentType = ContentType.CUSTOM,
    fromUser = fromUser,
    toUser = toUser,
    content = content,
    timestamp = timestamp,
    status = status,
    extras = extras
)