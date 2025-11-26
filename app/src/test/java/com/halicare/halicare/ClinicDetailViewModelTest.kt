package com.halicare.halicare

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.halicare.halicare.api.ApiInterface
import com.halicare.halicare.api.AuthTokenProvider
import com.halicare.halicare.model.AppointmentRequest
import com.halicare.halicare.model.Clinic
import com.halicare.halicare.model.ClinicDetail
import com.halicare.halicare.model.ClinicService
import com.halicare.halicare.model.ContactInfo
import com.halicare.halicare.repository.ClinicRepository
import com.halicare.halicare.viewModel.BookingStatus
import com.halicare.halicare.viewModel.ClinicDetailViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class ClinicDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val api: ApiInterface = mockk()
    private val tokenProvider: AuthTokenProvider = mockk(relaxed = true)
    private val context: Context = mockk()
    private val sharedPreferences: SharedPreferences = mockk()

    private lateinit var repository: ClinicRepository
    private lateinit var viewModel: ClinicDetailViewModel

    private val mockClinicDetail = ClinicDetail(
        center_id = "123",
        center_name = "Test Clinic",
        address = "123 Test St",
        latitude = 0.0,
        longitude = 0.0,
        contact_number = "123-456-7890",
        opening_time = "09:00",
        closing_time = "17:00"
    )

    private val testServices = listOf(
        ClinicService(serviceName = "Test Service", hours = "9-5", description = "Test Description", status = "Available", centerId = "123", serviceId = "service1")
    )

    private val testClinic = Clinic(
        name = "Test Clinic",
        location = "123 Test St",
        address = "123 Test St",
        hours = "09:00 – 17:00",
        services = testServices,
        contact = ContactInfo(phone = "123-456-7890")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0

        every { context.getSharedPreferences("HALICARE_PREFS", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.getString("USER_ID", null) } returns "test_user_id"

        repository = ClinicRepository(api)
        viewModel = ClinicDetailViewModel(repository, tokenProvider, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }


    @Test
    fun bookAppointmentWithDetails_setsErrorWhenUserNotLoggedIn() = runTest {
        val centerId = "123"
        val serviceId = "service1"
        val date = LocalDate.now()
        val time = LocalTime.now()

        coEvery { repository.getClinicWithServices(centerId) } returns testClinic
        coEvery { repository.hasUserBookedAtClinic("test_user_id", centerId) } returns false

        every { sharedPreferences.getString("USER_ID", null) } returns null

        viewModel.loadClinicDetails(centerId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.bookAppointmentWithDetails(centerId, serviceId, date, time)
        testDispatcher.scheduler.advanceUntilIdle()

        val status = viewModel.bookingStatus.value as BookingStatus.Error
        assertEquals("Error: User not logged in", status.message)
    }

    @Test
    fun resetBookingStatus_setsStatusToIdle() = runTest {
        viewModel.resetBookingStatus()
        assertEquals(BookingStatus.Idle, viewModel.bookingStatus.value)
    }

    @Test
    fun clearError_setsErrorMessageToNull() = runTest {
        viewModel.clearError()
        assertNull(viewModel.errorMessage.value)
    }
}