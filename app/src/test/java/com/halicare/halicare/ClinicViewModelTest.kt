package com.halicare.halicare

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.halicare.halicare.repository.ClinicRepository
import com.halicare.halicare.viewModel.ClinicViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ClinicViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ClinicViewModel
    private lateinit var repository: ClinicRepository
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Log::class)
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0


        context = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)

        every { context.getSharedPreferences("HALICARE_PREFS", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.getString("USER_ID", null) } returns "test_user_id"

        repository = mockk(relaxed = true)
        viewModel = ClinicViewModel(repository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun fetchNearbyClinics_setsErrorWhenException() = runTest {
        val errorMessage = "Network error"
        val latitude = 1.23
        val longitude = 4.56
        coEvery { repository.getNearbyClinics(latitude, longitude) } throws Exception(errorMessage)

        viewModel.fetchNearbyClinics(latitude, longitude)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.getNearbyClinics(latitude, longitude) }
        assertTrue(viewModel.clinics.value.isEmpty())
        assertEquals("Failed to load clinics: $errorMessage", viewModel.errorMessage.value)
    }

    @Test
    fun clearError_clearsErrorMessage() = runTest {
        val errorMessage = "Network error"
        val latitude = 1.23
        val longitude = 4.56
        coEvery { repository.getNearbyClinics(latitude, longitude) } throws Exception(errorMessage)

        viewModel.fetchNearbyClinics(latitude, longitude)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Failed to load clinics: $errorMessage", viewModel.errorMessage.value)

        viewModel.clearError()
        assertEquals(null, viewModel.errorMessage.value)
    }

    @Test
    fun initial_state_isCorrect() {
        assertTrue(viewModel.clinics.value.isEmpty())
        assertTrue(viewModel.appointments.value.isEmpty())
        assertEquals(null, viewModel.errorMessage.value)
        assertEquals(false, viewModel.isLoading.value)
        assertEquals(false, viewModel.isCancelling.value)
    }
}