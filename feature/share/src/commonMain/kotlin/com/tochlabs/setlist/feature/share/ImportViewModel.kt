package com.tochlabs.setlist.feature.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.transfer.SetlistFileCodec
import com.tochlabs.setlist.core.domain.transfer.SetlistFileResult
import com.tochlabs.setlist.core.domain.transfer.SetlistTransferRepository
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.SetlistId
import com.tochlabs.setlist.core.model.SetlistPackage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ImportError { UNREADABLE, NEWER_VERSION }

/** What the file contains, shown before importing. */
data class ImportPreview(
    val setlistName: String,
    val bandName: String,
    val songCount: Int,
    val stopCount: Int,
)

data class ImportUiState(
    val loading: Boolean = true,
    val preview: ImportPreview? = null,
    val error: ImportError? = null,
    /** Active bands to import into. */
    val bands: List<Band> = emptyList(),
    val bandId: BandId? = null,
    val bandError: Boolean = false,
    val busy: Boolean = false,
    val importedId: SetlistId? = null,
)

private data class Choice(
    val bandId: BandId? = null,
    val bandError: Boolean = false,
    val busy: Boolean = false,
    val importedId: SetlistId? = null,
)

/**
 * Imports the setlist file waiting in [inbox] into an active band (specs/setlist-sharing). A file
 * that cannot be read changes nothing.
 */
class ImportViewModel(
    inbox: IncomingSetlistFiles,
    private val codec: SetlistFileCodec,
    private val transfer: SetlistTransferRepository,
    bands: BandRepository,
) : ViewModel() {
    private val decoded: SetlistFileResult = inbox.take()?.let(codec::decode) ?: SetlistFileResult.Unreadable
    private val choice = MutableStateFlow(Choice())

    val state: StateFlow<ImportUiState> =
        combine(bands.observeActive(), choice) { active, choice ->
            // The only active band is preselected; a band archived meanwhile is no longer valid.
            val band = choice.bandId?.takeIf { id -> active.any { it.id == id } } ?: active.singleOrNull()?.id
            ImportUiState(
                loading = false,
                preview = (decoded as? SetlistFileResult.Valid)?.pkg?.preview(),
                error =
                    when (decoded) {
                        is SetlistFileResult.Valid -> null
                        SetlistFileResult.Unreadable -> ImportError.UNREADABLE
                        SetlistFileResult.NewerVersion -> ImportError.NEWER_VERSION
                    },
                bands = active,
                bandId = band,
                bandError = choice.bandError,
                busy = choice.busy,
                importedId = choice.importedId,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ImportUiState())

    fun selectBand(id: BandId) {
        choice.value = choice.value.copy(bandId = id, bandError = false)
    }

    /** Creates the new setlist in one transaction; nothing happens for an unreadable file. */
    fun import() {
        val pkg = (decoded as? SetlistFileResult.Valid)?.pkg
        val current = state.value
        val bandId = current.bandId
        when {
            pkg == null || current.busy || current.importedId != null -> Unit
            bandId == null -> choice.value = choice.value.copy(bandError = true)
            else -> {
                choice.value = choice.value.copy(busy = true)
                viewModelScope.launch {
                    val id = transfer.import(pkg, bandId)
                    choice.value = choice.value.copy(busy = false, importedId = id)
                }
            }
        }
    }
}

private fun SetlistPackage.preview() = ImportPreview(setlist.name, bandName, songCount, stopCount)

private const val STOP_TIMEOUT_MILLIS = 5_000L
