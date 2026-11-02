package com.tochlabs.setlist.core.ui.format

import com.tochlabs.setlist.core.model.StopType
import com.tochlabs.setlist.core.ui.resources.Res
import com.tochlabs.setlist.core.ui.resources.stop_break
import com.tochlabs.setlist.core.ui.resources.stop_encore
import com.tochlabs.setlist.core.ui.resources.stop_speech
import com.tochlabs.setlist.core.ui.resources.stop_tuning
import org.jetbrains.compose.resources.StringResource

/** Translated name of a stop type. */
fun StopType.label(): StringResource =
    when (this) {
        StopType.SPEECH -> Res.string.stop_speech
        StopType.BREAK -> Res.string.stop_break
        StopType.TUNING -> Res.string.stop_tuning
        StopType.ENCORE -> Res.string.stop_encore
    }
