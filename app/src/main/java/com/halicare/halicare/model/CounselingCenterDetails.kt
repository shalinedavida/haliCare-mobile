package com.halicare.halicare.model

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class CounselingCenterDetails(
    @SerializedName("center_id") val center_id: String,
    @SerializedName("center_name") val center_name: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("operational_status") val operational_status: String,
    @SerializedName("image_path") val image_path: String? = null,
    @SerializedName("contact_number") val contact: String = "",
    @SerializedName("opening_time") val opening_hours: String = "",
    @SerializedName("closing_time") val closing_hours: String = ""
) {
    val location: LatLng get() = LatLng(latitude, longitude)
    val imageUrl: String get() = image_path ?: "https://via.placeholder.com/300"
}