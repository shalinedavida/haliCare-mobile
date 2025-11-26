package com.halicare.halicare

import android.os.Build
import com.halicare.halicare.api.ApiInterface
import com.halicare.halicare.model.*
import com.halicare.halicare.repository.ClinicRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O])
class ClinicRepositoryTest {

    private lateinit var repository: ClinicRepository
    private lateinit var api: ApiInterface

    @Before
    fun setup() {
        api = mockk()
        repository = ClinicRepository(api)
    }

    @Test
    fun getClinics_returnsClinicsList() = runTest {
        coEvery { api.getClinics() } returns Response.success(TestData.clinicDetailsList)

        val result = repository.getClinics()

        coVerify { api.getClinics() }
        assertEquals(TestData.clinicDetailsList, result)
    }

    @Test
    fun getClinics_returnsEmptyListWhenNull() = runTest {
        coEvery { api.getClinics() } returns Response.success(null)

        val result = repository.getClinics()

        coVerify { api.getClinics() }
        assertTrue(result.isEmpty())
    }

    @Test
    fun getClinicWithServices_returnsClinic() = runTest {
        coEvery { api.getClinicDetail(TestData.clinicDetails1.center_id) } returns Response.success(TestData.clinicDetail1)
        coEvery { api.getServicesByCenterId(TestData.clinicDetails1.center_id) } returns Response.success(TestData.clinicServicesList)

        val result = repository.getClinicWithServices(TestData.clinicDetails1.center_id)

        coVerify { api.getClinicDetail(TestData.clinicDetails1.center_id) }
        coVerify { api.getServicesByCenterId(TestData.clinicDetails1.center_id) }
        assertNotNull(result)
        assertEquals(TestData.clinicDetail1.center_name, result?.name)
        assertEquals(
            TestData.clinicServicesList.filter { it.centerId == TestData.clinicDetails1.center_id }.size,
            result?.services?.size
        )
    }

    @Test
    fun getClinicWithServices_returnsNullWhenClinicNotFound() = runTest {
        coEvery { api.getClinicDetail(TestData.clinicDetails1.center_id) } returns Response.success(null)

        val result = repository.getClinicWithServices(TestData.clinicDetails1.center_id)

        coVerify { api.getClinicDetail(TestData.clinicDetails1.center_id) }
        assertNull(result)
    }

    @Test
    fun getAppointments_returnsAppointmentsList() = runTest {
        coEvery { api.getAppointments() } returns Response.success(TestData.appointmentsList)

        val result = repository.getAppointments()

        coVerify { api.getAppointments() }
        assertEquals(TestData.appointmentsList, result)
    }

    @Test
    fun getAppointments_returnsEmptyListWhenException() = runTest {
        coEvery { api.getAppointments() } throws Exception("Network error")

        val result = repository.getAppointments()

        coVerify { api.getAppointments() }
        assertTrue(result.isEmpty())
    }

    @Test
    fun getAppointmentsByUserId_throwsExceptionWhenApiFails() = runTest {
        coEvery { api.getAppointments() } returns Response.error(404, ResponseBody.create(null, ""))

        try {
            repository.getAppointmentsByUserId("test_user_id")
            fail("Expected exception was not thrown")
        } catch (e: Exception) {
            assertTrue(e.message!!.startsWith("Failed to load your appointments: Failed to fetch appointments: 404"))
        }

        coVerify { api.getAppointments() }
    }

    @Test
    fun bookAppointment_returnsSuccess() = runTest {
        coEvery { api.bookAppointment(TestData.appointmentRequest) } returns Response.success(TestData.appointmentResponse)

        val result = repository.bookAppointment(TestData.appointmentRequest)

        coVerify { api.bookAppointment(TestData.appointmentRequest) }
        assertTrue(result.isSuccess)
        assertEquals(TestData.appointmentResponse, result.getOrNull())
    }

    @Test
    fun bookAppointment_returnsFailureWhenApiFails() = runTest {
        coEvery { api.bookAppointment(TestData.appointmentRequest) } returns Response.error(
            400,
            ResponseBody.create(null, "Bad request")
        )

        val result = repository.bookAppointment(TestData.appointmentRequest)

        coVerify { api.bookAppointment(TestData.appointmentRequest) }
        assertTrue(result.isFailure)
        assertEquals("Booking failed: 400 - Bad request", result.exceptionOrNull()?.message)
    }

    @Test
    fun bookAppointment_returnsFailureWhenException() = runTest {
        coEvery { api.bookAppointment(TestData.appointmentRequest) } throws Exception("Network error")

        val result = repository.bookAppointment(TestData.appointmentRequest)

        coVerify { api.bookAppointment(TestData.appointmentRequest) }
        assertTrue(result.isFailure)
        assertEquals(
            "Network error: Could not book appointment. Please try again.",
            result.exceptionOrNull()?.message
        )
    }

    @Test
    fun hasUserBookedAtClinic_returnsTrueWhenUserHasBooked() = runTest {
        coEvery { api.getAppointments() } returns Response.success(TestData.appointmentsList)
        coEvery { api.getClinicDetail(any()) } returns Response.success(TestData.clinicDetail1)
        coEvery { api.getServiceById(any()) } returns Response.success(TestData.clinicService1)

        val result = repository.hasUserBookedAtClinic("test_user_id", TestData.clinicDetails1.center_id)

        coVerify { api.getAppointments() }
        assertTrue(result)
    }

    @Test
    fun hasUserBookedAtClinic_returnsFalseWhenNoBooking() = runTest {
        val noMatchAppointments = listOf(
            Appointment(
                appointmentId = "1",
                userId = "test_user_id",
                centerId = "OTHER_CLINIC",
                serviceId = "S1",
                bookingStatus = "Confirmed",
                appointmentDate = "2025-10-01T00:00:00Z",
                transferLetter = null
            )
        )
        coEvery { api.getAppointments() } returns Response.success(noMatchAppointments)

        val result = repository.hasUserBookedAtClinic("test_user_id", TestData.clinicDetails1.center_id)

        coVerify { api.getAppointments() }
        assertFalse(result)
    }

    @Test
    fun hasUserBookedAtClinic_returnsFalseWhenException() = runTest {
        coEvery { api.getAppointments() } throws Exception("Network error")

        val result = repository.hasUserBookedAtClinic("test_user_id", TestData.clinicDetails1.center_id)

        coVerify { api.getAppointments() }
        assertFalse(result)
    }


    @Test
    fun cancelAppointment_returnsSuccess() = runTest {
        coEvery { api.updateAppointmentStatus(any(), any()) } returns Response.success(Unit)

        val result = repository.cancelAppointment(
            appointmentId = "123",
            userId = "user1",
            appointmentDate = "2025-10-16",
            centerId = "1",
            serviceId = "service1"
        )

        coVerify(exactly = 1) { api.updateAppointmentStatus("123", any()) }
        assertTrue(result.isSuccess)
    }

    @Test
    fun cancelAppointment_returnsFailureOnApiError() = runTest {
        coEvery { api.updateAppointmentStatus(any(), any()) } returns Response.error(
            400,
            ResponseBody.create(null, "Bad request")
        )

        val result = repository.cancelAppointment(
            appointmentId = "123",
            userId = "user1",
            appointmentDate = "2025-10-16",
            centerId = "1",
            serviceId = "service1"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Cancellation failed"))
    }

    @Test
    fun cancelAppointment_returnsFailureOnMissingFields() = runTest {
        val result = repository.cancelAppointment("", "user", "date", "center", "service")
        assertTrue(result.isFailure)
        assertEquals("Missing required details for cancellation.", result.exceptionOrNull()?.message)
    }

    @Test
    fun updateAppointmentStatus_returnsSuccess() = runTest {
        coEvery { api.updateAppointmentStatus(any(), any()) } returns Response.success(Unit)

        val result = repository.updateAppointmentStatus(
            appointmentId = "1",
            status = "Confirmed",
            userId = "user",
            appointmentDate = "date",
            centerId = "1",
            serviceId = "service1"
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun updateAppointmentStatus_returnsFailureOnApiError() = runTest {
        coEvery { api.updateAppointmentStatus(any(), any()) } returns Response.error(
            500,
            ResponseBody.create(null, "")
        )

        val result = repository.updateAppointmentStatus(
            appointmentId = "1",
            status = "Confirmed",
            userId = "user",
            appointmentDate = "date",
            centerId = "1",
            serviceId = "service1"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Failed to update appointment status"))
    }
}