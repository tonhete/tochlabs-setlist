package com.tochlabs.setlist.feature.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.dsl.module

// iOS PDF drawing, share sheet, printing and document picker come with task 12.2. Until then the
// export screen produces empty PDFs and the actions do nothing.

actual val sharePlatformModule = module { single<PdfRenderer> { PdfRenderer { ByteArray(0) } } }

@Composable
actual fun rememberDocumentActions(): DocumentActions =
    remember {
        object : DocumentActions {
            override fun share(file: ExportFile) = Unit

            override fun print(file: ExportFile) = Unit
        }
    }

@Composable
actual fun rememberSetlistFilePicker(onPicked: (String?) -> Unit): () -> Unit = {}
