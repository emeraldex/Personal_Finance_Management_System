package com.expense.android.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.expense.android.viewmodel.ExportPayload
import java.io.File

/**
 * Writes a rendered CSV into the app's private cache and hands it to the system
 * share sheet through the app's [FileProvider]. Keeping this out of the
 * ViewModel means the export logic itself stays framework-free and testable.
 *
 * The directory matches `res/xml/file_paths.xml`; nothing outside it is ever
 * exposed, and the receiving app only gets a one-shot read grant.
 */
fun shareCsv(context: Context, payload: ExportPayload) {
    val dir = File(context.cacheDir, "exports")
    dir.mkdirs()
    val file = File(dir, payload.fileName)
    file.writeText(payload.content)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, "Share ${payload.fileName}"))
}
