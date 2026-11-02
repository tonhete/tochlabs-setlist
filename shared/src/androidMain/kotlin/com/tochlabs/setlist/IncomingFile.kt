package com.tochlabs.setlist

import android.content.Context
import android.content.Intent
import com.tochlabs.setlist.feature.share.IncomingSetlistFiles
import com.tochlabs.setlist.feature.share.readSetlistText
import org.koin.mp.KoinPlatform

/**
 * Hands a `.setlist` file opened from another app to the import screen. A file that cannot be read
 * is still handed over, as empty text, so the import screen can say so.
 */
fun receiveSetlistFile(
    context: Context,
    intent: Intent?,
) {
    val uri = intent?.takeIf { it.action == Intent.ACTION_VIEW }?.data ?: return
    val text = readSetlistText(context, uri).orEmpty()
    KoinPlatform.getKoin().get<IncomingSetlistFiles>().offer(text)
}
