package app.allever.android.sample.im.protocol

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class MessageDeserializer : JsonDeserializer<Message> {

    private val contentTypeRegistry = mutableMapOf<String, Class<out Message>>().apply {
        put(ContentType.TEXT, TextMessage::class.java)
        put(ContentType.IMAGE, ImageMessage::class.java)
        put(ContentType.VOICE, VoiceMessage::class.java)
        put(ContentType.CUSTOM, CustomMessage::class.java)
    }

    fun registerContentType(contentType: String, clazz: Class<out Message>) {
        contentTypeRegistry[contentType] = clazz
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Message {
        val jsonObj = json.asJsonObject
        val contentType = jsonObj.get("contentType")?.asString ?: ContentType.TEXT
        val clazz = contentTypeRegistry[contentType] ?: Message::class.java
        return context.deserialize(json, clazz)
    }
}