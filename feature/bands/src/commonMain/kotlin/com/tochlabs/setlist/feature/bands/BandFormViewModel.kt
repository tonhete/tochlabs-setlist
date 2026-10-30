package com.tochlabs.setlist.feature.bands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.id.IdGenerator
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.validation.Validated
import com.tochlabs.setlist.core.domain.validation.validateRequiredText
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandColor
import com.tochlabs.setlist.core.model.BandId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BandFormUiState(
    val isNew: Boolean,
    val loading: Boolean = !isNew,
    val name: String = "",
    val color: BandColor = bandPalette.first(),
    val nameError: Boolean = false,
    /** True once saved; the screen then closes. */
    val saved: Boolean = false,
)

/** Create a band, or edit one when [bandId] is given (specs/bands, "Create and edit bands"). */
class BandFormViewModel(
    private val bandId: BandId?,
    private val bands: BandRepository,
    private val ids: IdGenerator,
) : ViewModel() {
    private val mutableState = MutableStateFlow(BandFormUiState(isNew = bandId == null))
    val state: StateFlow<BandFormUiState> = mutableState.asStateFlow()

    init {
        if (bandId != null) {
            viewModelScope.launch {
                val band = bands.observe(bandId).first()
                mutableState.update { state ->
                    band?.let { state.copy(loading = false, name = it.name, color = it.color) }
                        ?: state.copy(loading = false)
                }
            }
        }
    }

    fun onNameChange(name: String) = mutableState.update { it.copy(name = name, nameError = false) }

    fun onColorChange(color: BandColor) = mutableState.update { it.copy(color = color) }

    fun save() {
        val current = mutableState.value
        if (current.loading || current.saved) return
        when (val name = validateRequiredText(current.name)) {
            is Validated.Invalid -> mutableState.update { it.copy(nameError = true) }
            is Validated.Valid ->
                viewModelScope.launch {
                    bands.save(Band(bandId ?: BandId(ids.newId()), name.value, current.color))
                    mutableState.update { it.copy(saved = true) }
                }
        }
    }
}
