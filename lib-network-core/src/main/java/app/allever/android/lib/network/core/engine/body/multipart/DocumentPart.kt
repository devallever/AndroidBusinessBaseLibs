package app.allever.android.lib.network.core.engine.body.multipart

import app.allever.android.lib.network.core.engine.body.NetBodyPart
import java.io.File

class DocumentPart(
    file: File? = null
): NetBodyPart("document", file?.name, "application/octet-stream", null, file)