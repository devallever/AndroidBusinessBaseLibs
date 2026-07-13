package app.allever.android.lib.network.core.engine.body.multipart

import app.allever.android.lib.network.core.engine.body.NetBodyPart
import java.io.File

class ImagePart(
    file: File? = null
): NetBodyPart("image", file?.name, "image/*", null, file)