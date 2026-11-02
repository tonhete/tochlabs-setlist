package com.tochlabs.setlist.feature.share

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

/** Share sheet and print dialog of the platform (design.md, decision 7). */
interface DocumentActions {
    fun share(file: ExportFile)

    fun print(file: ExportFile)
}

/** Needs the current screen: Android prints only from an activity. */
@Composable
expect fun rememberDocumentActions(): DocumentActions

/**
 * Opens the system file picker. [onPicked] receives the file's text, or null when it could not be
 * read (the import screen then says so).
 */
@Composable
expect fun rememberSetlistFilePicker(onPicked: (String?) -> Unit): () -> Unit

/** Platform implementations: the PDF renderer. */
expect val sharePlatformModule: Module
