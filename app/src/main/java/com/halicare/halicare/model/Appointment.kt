package com.halicare.halicare.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
data class Appointment(
    @SerializedName("appointment_id") val appointmentId: String,
    @SerializedName("booking_status") val bookingStatus: String,
    @SerializedName("transfer_letter") val transferLetter: String,
    @SerializedName("appointment_date") val appointmentDate: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("center_id") val centerId: String,
    @SerializedName("service_id") val serviceId: String
) {

    var clinicName: String? = null
    var serviceName: String? = null
    var imageUrl: String? = null

    val id: String get() = appointmentId
    val status: AppointmentStatus get() = AppointmentStatus.fromString(bookingStatus)

    val date: String
        get() = try {
            val utc = LocalDateTime.parse(
                appointmentDate.replace("Z", "+00:00"),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
            val local = utc.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
            local.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        } catch (e: Exception) {
            "Invalid Date"
        }

    val time: String
        get() = try {
            val utc = LocalDateTime.parse(
                appointmentDate.replace("Z", "+00:00"),
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
            val local = utc.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()).toLocalTime()
            local.format(DateTimeFormatter.ofPattern("h:mm a"))
        } catch (e: Exception) {
            "Invalid Time"
        }

    val service: String get() = serviceName ?: "Unknown Service"
    val clinic: String get() = clinicName ?: "Unknown Clinic"
    val image: String get() = imageUrl ?: "https://via.placeholder.com/100"
}

enum class AppointmentStatus {
    Upcoming,
    Completed,
    Cancelled;

    companion object {
        fun fromString(status: String?): AppointmentStatus {
            return when (status?.trim()?.lowercase()) {
                "Completed" -> Completed
                "Cancelled", "Canceled" -> Cancelled
                "Upcoming", "Confirmed"-> Upcoming
                else -> Upcoming
            }
        }
    }
}


data class AppointmentRequest(
    @SerializedName("center_id") val centerId: String,
    @SerializedName("service_id") val serviceId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("appointment_date") val appointmentDate: String,
    @SerializedName("transfer_letter") val transferLetter: String?,
    @SerializedName("booking_status") val bookingStatus: String = "Upcoming"
)

data class AppointmentResponse(
    @SerializedName("appointment_id") val appointmentId: String,
    @SerializedName("detail") val detail: String? = null
)

