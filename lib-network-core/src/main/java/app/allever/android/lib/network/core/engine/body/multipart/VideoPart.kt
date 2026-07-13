package app.allever.android.lib.network.core.engine.body.multipart

import app.allever.android.lib.network.core.engine.body.NetBodyPart
import java.io.File

class VideoPart(
    file: File? = null
): NetBodyPart("video", file?.name, "video/*", null, file)