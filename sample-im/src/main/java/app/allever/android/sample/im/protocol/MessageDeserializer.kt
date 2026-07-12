package app.allever.android.sample.im.protocol

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

object MessageDeserializer : JsonDeserializer<Message> {

    private val contentTypeRegistry = ConcurrentHashMap<String, Class<out Message>>()

    init {
        contentTypeRegistry[TextMessage.contentType] = TextMessage.clazz
        contentTypeRegistry[ImageMessage.contentType] = ImageMessage.clazz
        contentTypeRegistry[VoiceMessage.contentType] = VoiceMessage.clazz
        contentTypeRegistry[CustomMessage.contentType] = CustomMessage.clazz
    }

    fun register(def: MessageTypeDef) {
        contentTypeRegistry[def.contentType] = def.clazz
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Message {
        val jsonObj = json.asJsonObject
        val contentType = jsonObj.get("contentType")?.asString ?: ContentType.TEXT
        val clazz = contentTypeRegistry[contentType] ?: CustomMessage::class.java
        return context.deserialize(json, clazz)
    }
}