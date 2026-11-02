package com.tochlabs.setlist.feature.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import org.koin.dsl.module
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

actual val sharePlatformModule = module { single<PdfRenderer> { AndroidPdfRenderer() } }

@Composable
actual fun rememberDocumentActions(): DocumentActions {
    val context = LocalContext.current
    return remember(context) { AndroidDocumentActions(context) }
}

/** Share through a content URI of the app's FileProvider; print through PrintManager (needs an activity). */
private class AndroidDocumentActions(
    private val context: Context,
) : DocumentActions {
    override fun share(file: ExportFile) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}$EXPORTS_AUTHORITY_SUFFIX", write(file))
        val send =
            Intent(Intent.ACTION_SEND).apply {
                type = file.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TITLE, file.name)
                clipData = ClipData.newRawUri(file.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(Intent.createChooser(send, file.name))
    }

    override fun print(file: ExportFile) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val attributes = PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build()
        printManager.print(file.name, PdfPrintAdapter(file), attributes)
    }

    /** Files go to the cache, replacing earlier exports, so they do not pile up. */
    private fun write(file: ExportFile): File {
        val dir = File(context.cacheDir, EXPORTS_DIR).apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        return File(dir, file.name).apply { writeBytes(file.bytes) }
    }
}

/** Hands the already rendered PDF to the print system. */
private class PdfPrintAdapter(
    private val file: ExportFile,
) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }
        val info = PrintDocumentInfo.Builder(file.name).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build()
        callback.onLayoutFinished(info, oldAttributes != newAttributes)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback,
    ) {
        try {
            FileOutputStream(destination.fileDescriptor).use { it.write(file.bytes) }
            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: IOException) {
            callback.onWriteFailed(e.message)
        }
    }
}

@Composable
actual fun rememberSetlistFilePicker(onPicked: (String?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val current by rememberUpdatedState(onPicked)
    // Some file managers label JSON as plain text or binary, so the picker shows every file and the import checks it.
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) current(readSetlistText(context, uri))
        }
    return { launcher.launch(arrayOf("*/*")) }
}

/** The text of a setlist file, or null when it cannot be read or is too large to be one. */
fun readSetlistText(
    context: Context,
    uri: Uri,
): String? =
    try {
        context.contentResolver.openInputStream(uri)?.use { input -> readLimited(input)?.decodeToString() }
    } catch (ignored: IOException) {
        null
    } catch (ignored: SecurityException) {
        // The other app withdrew access to the file.
        null
    }

/** Reads at most [MAX_FILE_BYTES]; null when the stream is longer. Works on every Android version. */
private fun readLimited(input: InputStream): ByteArray? {
    val out = ByteArrayOutputStream()
    val chunk = ByteArray(READ_CHUNK)
    while (true) {
        val read = input.read(chunk)
        if (read < 0) return out.toByteArray()
        if (out.size() + read > MAX_FILE_BYTES) return null
        out.write(chunk, 0, read)
    }
}

/** Must match the provider authority and path in the app manifest and res/xml/export_paths.xml. */
private const val EXPORTS_AUTHORITY_SUFFIX = ".exports"
private const val EXPORTS_DIR = "exports"

/** A setlist of a few hundred songs is far below this. */
private const val MAX_FILE_BYTES = 2 * 1024 * 1024
private const val READ_CHUNK = 8 * 1024
