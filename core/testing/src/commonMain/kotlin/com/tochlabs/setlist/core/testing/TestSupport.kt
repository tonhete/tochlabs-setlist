package com.tochlabs.setlist.core.testing

import com.tochlabs.setlist.core.domain.id.IdGenerator
import com.tochlabs.setlist.core.domain.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

private val DEFAULT_NOW = Instant.parse("2026-11-14T12:00:00Z")
private val DEFAULT_TODAY = LocalDate.parse("2026-11-14")

/** Fixed clock: 14 Nov 2026, 12:00 UTC unless given. Each call to [now] moves one second forward. */
class FakeClock(
    private var current: Instant = DEFAULT_NOW,
    private val today: LocalDate = DEFAULT_TODAY,
) : Clock {
    override fun now(): Instant = current.also { current = Instant.fromEpochSeconds(it.epochSeconds + 1) }

    override fun today(): LocalDate = today
}

/** Ids "id-1", "id-2", ... */
class SequentialIdGenerator : IdGenerator {
    private var next = 1

    override fun newId(): String = "id-${next++}"
}

/**
 * Runs a ViewModel test. `viewModelScope` uses Dispatchers.Main, so it is replaced by an
 * unconfined test dispatcher: work started by the ViewModel runs immediately.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runViewModelTest(block: suspend TestScope.() -> Unit): TestResult {
    val dispatcher = UnconfinedTestDispatcher()
    Dispatchers.setMain(dispatcher)
    // runTest blocks on the JVM and on native, so Main is reset after the background collectors stop.
    try {
        return runTest(dispatcher) { block() }
    } finally {
        Dispatchers.resetMain()
    }
}

/** Keeps a lazily shared flow (stateIn with WhileSubscribed) active for the rest of the test. */
fun TestScope.keepActive(flow: Flow<*>) {
    backgroundScope.launch { flow.collect {} }
}
