package com.tochlabs.setlist.feature.share

import com.tochlabs.setlist.feature.share.pdf.ExportTexts
import com.tochlabs.setlist.feature.share.pdf.PdfLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Platform work behind small interfaces (design.md, decision 7).

/** Draws a layout into PDF bytes. Android: PdfDocument; iOS: UIGraphicsPDFRenderer (task 12.2). */
fun interface PdfRenderer {
    fun render(layout: PdfLayout): ByteArray
}

/** A file ready to share or print. */
class ExportFile(
    val name: String,
    val mimeType: String,
    val bytes: ByteArray,
)

/** Translated PDF texts in the current language. */
fun interface ExportTextsProvider {
    suspend fun texts(): ExportTexts
}

/**
 * A setlist file opened from outside the app (another app, the file manager) or picked with the
 * import button, waiting for the import screen. Holds the file's text.
 */
class IncomingSetlistFiles {
    private val pending = MutableStateFlow<String?>(null)

    /** True while a file waits to be imported; the app shows the import screen. */
    val hasPending: StateFlow<Boolean> get() = hasPendingState.asStateFlow()
    private val hasPendingState = MutableStateFlow(false)

    fun offer(text: String) {
        pending.value = text
        hasPendingState.value = true
    }

    /** Hands the file over once. */
    fun take(): String? =
        pending.value.also {
            pending.value = null
            hasPendingState.value = false
        }
}

const val PDF_MIME_TYPE = "application/pdf"

/** Setlist files are readable JSON (design.md, decision 8). */
const val SETLIST_MIME_TYPE = "application/json"
