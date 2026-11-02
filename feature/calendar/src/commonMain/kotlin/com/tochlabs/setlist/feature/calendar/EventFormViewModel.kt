package com.tochlabs.setlist.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tochlabs.setlist.core.domain.id.IdGenerator
import com.tochlabs.setlist.core.domain.repository.BandRepository
import com.tochlabs.setlist.core.domain.repository.EventRepository
import com.tochlabs.setlist.core.domain.validation.Validated
import com.tochlabs.setlist.core.domain.validation.validateRequiredText
import com.tochlabs.setlist.core.model.Band
import com.tochlabs.setlist.core.model.BandId
import com.tochlabs.setlist.core.model.Event
import com.tochlabs.setlist.core.model.EventId
import com.tochlabs.setlist.core.model.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class EventFormUiState(
    val isNew: Boolean,
    val loading: Boolean = true,
    /** Active bands the event can belong to. */
    val bands: List<Band> = emptyList(),
    val bandId: BandId? = null,
    val type: EventType = EventType.GIG,
    val date: LocalDate? = null,
    val title: String = "",
    val venue: String = "",
    val address: String = "",
    val parking: String = "",
    val contact: String = "",
    val titleError: Boolean = false,
    val dateError: Boolean = false,
    val bandError: Boolean = false,
    val savedId: EventId? = null,
)

/**
 * Create an event for an active band, or edit [eventId] (specs/event-calendar, "Create and
 * edit events"). [initialDate] and [initialBandId] prefill a new event, for example from the calendar.
 */
class EventFormViewModel(
    private val eventId: EventId?,
    initialDate: LocalDate?,
    initialBandId: BandId?,
    private val events: EventRepository,
    bands: BandRepository,
    private val ids: IdGenerator,
) : ViewModel() {
    private val mutableState = MutableStateFlow(EventFormUiState(isNew = eventId == null, date = initialDate))
    val state: StateFlow<EventFormUiState> = mutableState.asStateFlow()

    private var loaded: Event? = null

    init {
        viewModelScope.launch {
            val active = bands.observeActive().first()
            val event = eventId?.let { events.observe(it).first() }
            loaded = event
            mutableState.update { form ->
                val base =
                    form.copy(
                        loading = false,
                        bands = active,
                        bandId =
                            initialBandId?.takeIf { id ->
                                active.any { it.id == id }
                            } ?: active.singleOrNull()?.id,
                    )
                if (event == null) base else base.withEvent(event)
            }
        }
    }

    private fun EventFormUiState.withEvent(event: Event) =
        copy(
            bandId = event.bandId,
            type = event.type,
            date = event.date,
            title = event.title,
            venue = event.venue.orEmpty(),
            address = event.address.orEmpty(),
            parking = event.parking.orEmpty(),
            contact = event.contact.orEmpty(),
        )

    fun selectBand(id: BandId) = mutableState.update { it.copy(bandId = id, bandError = false) }

    fun selectType(type: EventType) = mutableState.update { it.copy(type = type) }

    fun onDateChange(date: LocalDate) = mutableState.update { it.copy(date = date, dateError = false) }

    fun onTitleChange(text: String) = mutableState.update { it.copy(title = text, titleError = false) }

    fun onVenueChange(text: String) = mutableState.update { it.copy(venue = text) }

    fun onAddressChange(text: String) = mutableState.update { it.copy(address = text) }

    fun onParkingChange(text: String) = mutableState.update { it.copy(parking = text) }

    fun onContactChange(text: String) = mutableState.update { it.copy(contact = text) }

    /** Title, date and band are required. */
    fun save() {
        val form = mutableState.value
        if (form.loading || form.savedId != null) return
        val title = validateRequiredText(form.title)
        val date = form.date
        val bandId = form.bandId
        if (title !is Validated.Valid || date == null || bandId == null) {
            mutableState.update {
                it.copy(titleError = title is Validated.Invalid, dateError = date == null, bandError = bandId == null)
            }
            return
        }
        val existing = loaded
        val event =
            Event(
                id = existing?.id ?: EventId(ids.newId()),
                bandId = bandId,
                type = form.type,
                date = date,
                title = title.value,
                venue = form.venue.optional(),
                address = form.address.optional(),
                parking = form.parking.optional(),
                contact = form.contact.optional(),
                // The linked setlist must belong to the event's band.
                setlistId = existing?.setlistId?.takeIf { existing.bandId == bandId },
            )
        viewModelScope.launch {
            events.save(event)
            mutableState.update { it.copy(savedId = event.id) }
        }
    }
}

private fun String.optional(): String? = trim().ifEmpty { null }
