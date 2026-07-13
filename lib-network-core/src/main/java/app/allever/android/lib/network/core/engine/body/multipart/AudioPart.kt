package app.allever.android.lib.network.core.engine.body.multipart

import app.allever.android.lib.network.core.engine.body.NetBodyPart
import java.io.File

class AudioPart(
    file: File? = null
): NetBodyPart("audio", file?.name, "audio/*", null, file)