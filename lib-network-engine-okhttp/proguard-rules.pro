# 保留引擎类及其 companion object 初始化块（确保自动注册生效）
-keep class app.allever.android.lib.network.engine.huc.UrlConnectionEngine { *; }
-keepclassmembers class app.allever.android.lib.network.engine.huc.UrlConnectionEngine {
    <init>(...);
    Companion;
}
