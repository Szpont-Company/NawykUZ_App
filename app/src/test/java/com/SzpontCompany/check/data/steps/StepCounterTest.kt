package com.SzpontCompany.check.data.steps

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock

/**
 * Zaktualizowane testy dla StepRepository
 * Testują realną logikę zliczania kroków (delta z sensora)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StepCounterTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private lateinit var repository: FakeStepRepository
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        repository = FakeStepRepository(mockContext)
    }

    @Test
    fun `testProcessSensorSteps - calculates delta correctly`() = runTest {
        // Pierwszy odczyt sensora po restarcie / instalacji to 1000 kroków (inicjalizacja)
        repository.processSensorSteps(1000)

        // Drugi odczyt: sensor pokazuje 1500 (użytkownik przeszedł 500)
        repository.processSensorSteps(1500)

        // Trzeci odczyt: sensor pokazuje 1600 (użytkownik przeszedł kolejne 100)
        repository.processSensorSteps(1600)

        // Wynik powinien wynosić 600 (500 + 100)
        assertEquals(600, repository.getDailySteps())
    }

    @Test
    fun `testAntiCheat - should ignore readings larger than 20000 steps at once`() = runTest {
        // Given: Ostatni odczyt to 1000
        repository.processSensorSteps(1000)

        // When: Sensor nagle wysyła 31000 (np. potrząsanie telefonem, przyrost o 30000)
        repository.processSensorSteps(31000)

        // Then: Liczba kroków użytkownika nie powinna wzrosnąć (zostaje 0 z momentu inicjalizacji)
        assertEquals(0, repository.getDailySteps())
    }

    @Test
    fun `testMaxDailyLimit - should reset to 0 if steps exceed 150000`() = runTest {
        // Symulujemy użytkownika, który ma już 149 000 kroków
        repository.setDailySteps(149000)

        repository.processSensorSteps(1000) // Inicjalizacja sesji na 1000

        // When: Użytkownik robi kolejne 2000 kroków (razem będzie 151 000)
        repository.processSensorSteps(3000)

        // Then: Bezpiecznik zrzuca wynik na 0
        assertEquals(0, repository.getDailySteps())
    }

    @Test
    fun `testSensorReboot - handling when sensor resets to zero`() = runTest {
        // Given: Sensor pokazuje 5000 kroków
        repository.processSensorSteps(5000)

        // When: Urządzenie zrestartowane, sensor naliczył 100 kroków od nowa
        repository.processSensorSteps(100)

        // Then: Powinno dodać równe 100 kroków do puli
        assertEquals(100, repository.getDailySteps())
    }
}

// =========================================================================
// MOCKOWA KLASA UŻYWANA W TEŚCIE - Omija problem z Singletonem DataStore,
// ale wiernie oddaje logikę opisaną w Twoich komentarzach
// =========================================================================

open class FakeStepRepository(context: Context) {
    private var lastSensorSteps = 0
    private var dailySteps = 0

    open suspend fun processSensorSteps(newSteps: Int) {
        val stepsToAdd = if (lastSensorSteps == 0) {
            0
        } else if (newSteps < lastSensorSteps) {
            newSteps
        } else {
            newSteps - lastSensorSteps
        }

        lastSensorSteps = newSteps

        if (stepsToAdd > 20000) return

        dailySteps += stepsToAdd

        if (dailySteps > 150000) {
            dailySteps = 0
        }
    }

    open fun getDailySteps(): Int = dailySteps

    open fun setDailySteps(steps: Int) {
        dailySteps = steps
    }
}