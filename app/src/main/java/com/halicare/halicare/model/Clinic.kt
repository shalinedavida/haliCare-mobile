package com.halicare.halicare.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class Clinic(
    val name: String,
    val location: String,
    val address: String,
    val hours: String,
    val services: List<ClinicService>,
    val contact: ContactInfo
)

data class ClinicDetails(
    val center_id: String,
    val center_name: String,
    val center_type: String,
    val image_path: String?,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val contact_number: String,
    val operational_status: String,
    val opening_time: String,
    val closing_time: String,
    val updated_at: String,
    val user: String?
) {
    val location: LatLng get() = LatLng(latitude, longitude)
    val imageUrl: String? get() = image_path
    val hours: String get() = "$opening_time - $closing_time"
}

data class ClinicService(
    @SerializedName("service_id") val serviceId: String,
    @SerializedName("service_name") val serviceName: String,
    @SerializedName("center_id") val centerId: String,
    val hours: String,
    val description: String,
    val status: String? = null,
    @SerializedName("last_updated") val last_updated: String? = null
) {
    val title: String get() = serviceName
}

data class ContactInfo(
    val phone: String
)

data class ClinicDetail(
    val center_id: String,
    val center_name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val contact_number: String,
    val opening_time: String,
    val closing_time: String,
    val image_path: String? = null
) {
    val hours: String get() = "$opening_time – $closing_time"
}

data class ArvAvailability(
    val id: Int,
    @SerializedName("arv_availability") val arvAvailability: String,
    @SerializedName("last_updated") val lastUpdated: String,
    @SerializedName("center") val centerId: String
) {
    val isAvailable: Boolean
        get() = arvAvailability.equals("available", ignoreCase = true)
}