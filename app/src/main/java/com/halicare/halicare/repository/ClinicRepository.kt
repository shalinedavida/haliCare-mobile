package com.halicare.halicare.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.halicare.halicare.api.ApiInterface
import com.halicare.halicare.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.IOException

class ClinicRepository(
    private val api: ApiInterface
) {
    suspend fun getClinics(): List<ClinicDetails> {
        return api.getClinics().body() ?: emptyList()
    }

    suspend fun getClinicWithServices(centerId: String): Clinic? {
        val clinicDetail = api.getClinicDetail(centerId).body()
            ?: return null
        val servicesResponse = api.getServicesByCenterId(centerId)
        val allServices = if (servicesResponse.isSuccessful) {
            servicesResponse.body() ?: emptyList()
        } else {
            emptyList()
        }
        val clinicServices = allServices.filter { it.centerId == centerId }
        return Clinic(
            name = clinicDetail.center_name,
            location = clinicDetail.address,
            address = clinicDetail.address,
            hours = clinicDetail.hours,
            services = clinicServices,
            contact = ContactInfo(clinicDetail.contact_number)
        )
    }

    suspend fun getNearbyClinics(latitude: Double, longitude: Double, maxDistanceKm: Double = 50.0): List<ClinicDetails> {
        val allClinics = getClinics()
        val userLocation = LatLng(latitude, longitude)
        return allClinics.filter { clinic ->
            val distance = computeDistance(userLocation, clinic.location)
            distance <= maxDistanceKm * 1000
        }
    }

    private fun computeDistance(latLng1: LatLng, latLng2: LatLng): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            latLng1.latitude,
            latLng1.longitude,
            latLng2.latitude,
            latLng2.longitude,
            results
        )
        return results[0]
    }

    suspend fun getAppointments(): List<Appointment> {
        return try {
            api.getAppointments().body() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAppointmentsByUserId(userId: String): List<Appointment> {
        return try {
            val response = api.getAppointments()
            if (!response.isSuccessful) {
                throw Exception("Failed to fetch appointments: ${response.code()}")
            }
            val allAppointments = response.body() ?: emptyList()
            val userAppointments = allAppointments.filter { it.userId == userId }
            if (userAppointments.isEmpty()) return emptyList()
            val centerIds = userAppointments.map { it.centerId }.distinct()
            val serviceIds = userAppointments.map { it.serviceId }.distinct()
            return coroutineScope {
                val clinicsDeferred = centerIds.map { centerId ->
                    async { api.getClinicDetail(centerId).body() }
                }
                val servicesDeferred = serviceIds.map { serviceId ->
                    async { api.getServiceById(serviceId).body() }
                }
                val clinics = clinicsDeferred.awaitAll().filterNotNull()
                val services = servicesDeferred.awaitAll().filterNotNull()
                val clinicMap = clinics.associateBy { it.center_id }
                val serviceMap = services.associateBy { it.serviceId }
                userAppointments.map { appointment ->
                    val clinic = clinicMap[appointment.centerId]
                    val service = serviceMap[appointment.serviceId]
                    appointment.apply {
                        clinicName = clinic?.center_name
                        imageUrl = clinic?.image_path
                        serviceName = service?.serviceName
                    }
                    appointment
                }
            }
        } catch (e: Exception) {
            Log.e("ClinicRepository", "Failed to load user appointments", e)
            throw Exception("Failed to load your appointments: ${e.message}")
        }
    }

    suspend fun cancelAppointment(
        appointmentId: String,
        userId: String,
        appointmentDate: String,
        centerId: String,
        serviceId: String
    ): Result<Unit> {
        if (appointmentId.isBlank() || userId.isBlank() || appointmentDate.isBlank() || centerId.isBlank() || serviceId.isBlank()) {
            Log.e("AppointmentCancel", "Missing required details for cancellation.")
            return Result.failure(Exception("Missing required details for cancellation."))
        }
        val statusString = "Cancelled"
        try {
            Log.d("AppointmentCancel", "Attempting to PUT cancellation for ID: $appointmentId")
            val statusPayload = mapOf(
                "booking_status" to statusString,
                "user_id" to userId,
                "appointment_date" to appointmentDate,
                "center_id" to centerId,
                "service_id" to serviceId
            )
            val response = api.updateAppointmentStatus(appointmentId, statusPayload)
            if (response.isSuccessful) {
                Log.d("AppointmentCancel", "PUT successful. Appointment $appointmentId cancelled.")
                return Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.w("AppointmentCancel", "PUT failed. Code: ${response.code()}. Response: $errorBody")
                return Result.failure(Exception("Cancellation failed: Code ${response.code()}. Server response: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("AppointmentCancel", "Exception during PUT request", e)
            return Result.failure(Exception("Network error: Failed to reach the server to cancel the appointment."))
        }
    }

    suspend fun updateAppointmentStatus(
        appointmentId: String,
        status: String,
        userId: String,
        appointmentDate: String,
        centerId: String,
        serviceId: String
    ): Result<Unit> {
        return try {
            val statusMap = mapOf(
                "booking_status" to status,
                "user_id" to userId,
                "appointment_date" to appointmentDate,
                "center_id" to centerId,
                "service_id" to serviceId
            )
            val response = api.updateAppointmentStatus(appointmentId, statusMap)
            if (response.isSuccessful) {
                Log.d("AppointmentUpdate", "Status updated to $status for appointment: $appointmentId")
                Result.success(Unit)
            } else {
                Log.w("AppointmentUpdate", "Failed to update status: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Failed to update appointment status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AppointmentUpdate", "Exception during status update", e)
            Result.failure(e)
        }
    }

    suspend fun bookAppointment(request: AppointmentRequest): Result<AppointmentResponse> {
        return try {
            val response = api.bookAppointment(request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Result.failure(Exception("Booking failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: Could not book appointment. Please try again."))
        }
    }

    suspend fun bookAppointment(
        context: Context,
        request: AppointmentRequest,
        fileUri: Uri?
    ): Result<AppointmentResponse> {
        return withContext(Dispatchers.IO) {
            try {
                if (fileUri == null) {
                    return@withContext bookAppointment(request)
                }

                val resolver = context.contentResolver
                val inputStream = resolver.openInputStream(fileUri)
                    ?: return@withContext Result.failure<AppointmentResponse>(IOException("Cannot open selected file"))
                val bytes = inputStream.readBytes()
                inputStream.close()

                val mimeType = resolver.getType(fileUri) ?: "image/jpeg"
                val reqFile = RequestBody.create(mimeType.toMediaTypeOrNull(), bytes)
                val fileName = queryFileName(context, fileUri) ?: "transfer_letter.jpg"
                val filePart = MultipartBody.Part.createFormData("transfer_letter", fileName, reqFile)

                val centerPart = RequestBody.create("text/plain".toMediaTypeOrNull(), request.centerId)
                val servicePart = RequestBody.create("text/plain".toMediaTypeOrNull(), request.serviceId)
                val userPart = RequestBody.create("text/plain".toMediaTypeOrNull(), request.userId)
                val datePart = RequestBody.create("text/plain".toMediaTypeOrNull(), request.appointmentDate)
                val statusPart = RequestBody.create("text/plain".toMediaTypeOrNull(), request.bookingStatus)

                val response = api.bookAppointmentWithFile(
                    centerId = centerPart,
                    serviceId = servicePart,
                    userId = userPart,
                    appointmentDate = datePart,
                    transfer_letter = filePart,
                    bookingStatus = statusPart
                )

                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    val err = response.errorBody()?.string()
                    Log.e("ClinicRepository", "Multipart booking failed: ${response.code()} - $err")
                    Result.failure(Exception("Booking failed: ${response.code()} - $err"))
                }
            } catch (e: Exception) {
                Log.e("ClinicRepository", "Exception while booking with file", e)
                Result.failure(e)
            }
        }
    }

    private fun queryFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name
    }

    suspend fun hasUserBookedAtClinic(userId: String, clinicId: String): Boolean {
        return try {
            val appointments = getAppointmentsByUserId(userId)
            appointments.any { it.centerId == clinicId }
        } catch (e: Exception) {
            Log.e("ClinicRepository", "Error checking if user booked at clinic", e)
            false
        }
    }

    suspend fun getArvAvailability(): List<ArvAvailability> {
        return try {
            val response = api.getArvAvailability()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("ClinicRepository", "Failed to fetch ARV availability: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ClinicRepository", "Exception fetching ARV availability", e)
            emptyList()
        }
    }

    suspend fun getArvAvailabilityByCenterId(centerId: String): ArvAvailability? {
        return try {
            val response = api.getArvAvailabilityByCenterId(centerId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("ClinicRepository", "Failed to fetch ARV availability for center $centerId: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ClinicRepository", "Exception fetching ARV availability for center $centerId", e)
            null
        }
    }

    suspend fun getServicesByCenterId(centerId: String): List<ClinicService> {
        return try {
            val response = api.getServicesByCenterId(centerId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("ClinicRepository", "Failed to fetch services for center $centerId: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ClinicRepository", "Exception fetching services for center $centerId", e)
            emptyList()
        }
    }
}