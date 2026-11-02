package com.tochlabs.setlist.feature.setlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.id.IdGenerator
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.SetlistRepository
import com.tochlabs.setlist.core.domain.repository.SettingsRepository
import com.tochlabs.setlist.core.domain.validation.Validated
import com.tochlabs.setlist.core.domain.validation.validateRequiredText
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Setlist
import com.tochlabs.setlist.core.model.SetlistId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class SetlistFormUiState(
    val isNew: Boolean,
    val loading: Boolean = true,
    val name: String = "",
    /** Active bands the setlist can belong to. */
    val bands: List<Band> = emptyList(),
    val bandId: BandId? = null,
    val date: LocalDate? = null,
    val nameError: Boolean = false,
    val bandError: Boolean = false,
    /** Set once saved; a new setlist then opens its detail. */
    val savedId: SetlistId? = null,
)

/**
 * Create a setlist for an active band, or edit [setlistId] (specs/setlists, "Create and edit setlists").
 * [initialBandId] preselects a band, for example the one filtered in the list.
 */
class SetlistFormViewModel(
    private val setlistId: SetlistId?,
    initialBandId: BandId?,
    private val setlists: SetlistRepository,
    bands: BandRepository,
    private val settings: SettingsRepository,
    private val ids: IdGenerator,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SetlistFormUiState(isNew = setlistId == null))
    val state: StateFlow<SetlistFormUiState> = mutableState.asStateFlow()

    private var loaded: Setlist? = null

    init {
        viewModelScope.launch {
            val active = bands.observeActive().first()
            val setlist = setlistId?.let { setlists.observe(it).first() }
            loaded = setlist
            val band =
                setlist?.bandId
                    ?: initialBandId?.takeIf { id -> active.any { it.id == id } }
                    ?: active.singleOrNull()?.id
            mutableState.update {
                it.copy(
                    loading = false,
                    bands = active,
                    bandId = band,
                    name = setlist?.name.orEmpty(),
                    date = setlist?.date,
                )
            }
        }
    }

    fun onNameChange(text: String) = mutableState.update { it.copy(name = text, nameError = false) }

    fun selectBand(id: BandId) = mutableState.update { it.copy(bandId = id, bandError = false) }

    fun onDateChange(date: LocalDate?) = mutableState.update { it.copy(date = date) }

    fun save() {
        val form = mutableState.value
        if (form.loading || form.savedId != null) return
        val name = validateRequiredText(form.name)
        val bandId = form.bandId
        if (name !is Validated.Valid || bandId == null) {
            mutableState.update { it.copy(nameError = name is Validated.Invalid, bandError = bandId == null) }
            return
        }
        viewModelScope.launch {
            val existing = loaded
            val setlist =
                existing?.copy(name = name.value, bandId = bandId, date = form.date)
                    ?: Setlist(
                        id = SetlistId(ids.newId()),
                        bandId = bandId,
                        name = name.value,
                        date = form.date,
                        pauseSeconds = settings.observe().first().defaultPauseSeconds,
                    )
            setlists.save(setlist)
            mutableState.update { it.copy(savedId = setlist.id) }
        }
    }
}
