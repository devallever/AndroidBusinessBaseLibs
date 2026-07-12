package app.allever.android.sample.im.protocol

interface MessageTypeDef {
    val contentType: String
    val clazz: Class<out Message>
}