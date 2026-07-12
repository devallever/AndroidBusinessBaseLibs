package app.allever.android.sample.im.protocol

import com.google.gson.Gson
import java.io.Serializable
import java.util.*

open class Message(
    open var messageId: String = "",
    open var type: MessageType = MessageType.PRIVATE,
    open var contentType: String = ContentType.TEXT,
    open var fromUser: String = "",
    open var toUser: String = "",
    open var content: String = "",
    open var timestamp: Long = 0,
    open var sendTime: Long = 0,
    open var deliverTime: Long = 0,
    open var readTime: Long = 0,
    open var status: MessageStatus = MessageStatus.SENDING,
    open var extras: MutableMap<String, Any?> = mutableMapOf()
) : Serializable {

    open fun toJson(): String = MessageProtocol.gson.toJson(this)

    companion object {
        fun fromJson(json: String): Message = MessageProtocol.gson.fromJson(json, Message::class.java)
    }
}

class MessageBuilder {
    private val message = Message().apply {
        timestamp = System.currentTimeMillis()
        messageId = UUID.randomUUID().toString()
    }

    fun messageId(value: String) = apply { message.messageId = value }
    fun type(value: MessageType) = apply { message.type = value }
    fun contentType(value: String) = apply { message.contentType = value }
    fun fromUser(value: String) = apply { message.fromUser = value }
    fun toUser(value: String) = apply { message.toUser = value }
    fun content(value: String) = apply { message.content = value }
    fun timestamp(value: Long) = apply { message.timestamp = value }
    fun sendTime(value: Long) = apply { message.sendTime = value }
    fun deliverTime(value: Long) = apply { message.deliverTime = value }
    fun readTime(value: Long) = apply { message.readTime = value }
    fun status(value: MessageStatus) = apply { message.status = value }
    fun extra(key: String, value: Any?) = apply { message.extras[key] = value }

    fun build(): Message = message

    fun buildText(): TextMessage {
        return TextMessage(
            messageId = message.messageId,
            type = message.type,
            fromUser = message.fromUser,
            toUser = message.toUser,
            content = message.content,
            timestamp = message.timestamp,
            status = message.status,
            extras = message.extras
        )
    }

    fun buildImage(): ImageMessage {
        return ImageMessage(
            messageId = message.messageId,
            type = message.type,
            fromUser = message.fromUser,
            toUser = message.toUser,
            content = message.content,
            timestamp = message.timestamp,
            status = message.status,
            extras = message.extras
        )
    }

    fun buildVoice(): VoiceMessage {
        return VoiceMessage(
            messageId = message.messageId,
            type = message.type,
            fromUser = message.fromUser,
            toUser = message.toUser,
            content = message.content,
            timestamp = message.timestamp,
            status = message.status,
            extras = message.extras
        )
    }

    fun buildCustom(): CustomMessage {
        return CustomMessage(
            messageId = message.messageId,
            type = message.type,
            fromUser = message.fromUser,
            toUser = message.toUser,
            content = message.content,
            timestamp = message.timestamp,
            status = message.status,
            extras = message.extras
        )
    }
}

object MessageProtocol {
    val gson: Gson = com.google.gson.GsonBuilder()
        .serializeNulls()
        .registerTypeAdapter(Message::class.java, MessageDeserializer())
        .create()
}