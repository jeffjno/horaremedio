package br.com.remediohoracerta.attachments

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

enum class PhotoKind(
    val directoryName: String,
    val filePrefix: String,
) {
    MEDICATION("medication_photos", "medicine_"),
    PRESCRIPTION("prescription_photos", "prescription_"),
}

object PhotoCapture {
    fun createFile(context: Context, kind: PhotoKind): File {
        val directory = File(context.filesDir, kind.directoryName).apply { mkdirs() }
        return File.createTempFile(kind.filePrefix, ".jpg", directory)
    }

    fun contentUri(context: Context, file: File): Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

