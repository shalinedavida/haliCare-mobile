package com.halicare.halicare.api

import com.halicare.halicare.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {
   @GET("clinics")
   suspend fun getClinics(): Response<List<ClinicDetails>>

   @GET("counseling-centers")
   suspend fun getCounselingCenters(): Response<List<CounselingCenterDetails>>

   @POST("login/")
   suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>

   @POST("register/")
   suspend fun registerUser(@Body signUpRequest: SignUpRequest): Response<Unit>

   @GET("clinic")
   suspend fun getClinic(): Response<Clinic>

   @GET("clinics/{center_id}/")
   suspend fun getClinicDetail(
      @Path("center_id") centerId: String
   ): Response<ClinicDetail>

   @GET("services")
   suspend fun getServicesByCenterId(
      @Query("center_id") centerId: String
   ): Response<List<ClinicService>>

   @GET("services/{service_id}/")
   suspend fun getServiceById(
      @Path("service_id") serviceId: String
   ): Response<ClinicService>

   @GET("services")
   suspend fun getServices(): Response<List<ClinicService>>

   @GET("appointment/")
   suspend fun getAppointments(): Response<List<Appointment>>

   @GET("appointment/")
   suspend fun getAppointmentsByUserId(
      @Query("user_id") userId: String
   ): Response<List<Appointment>>

   @PUT("appointment/{appointment_id}/")
   suspend fun updateAppointmentStatus(
      @Path("appointment_id") appointmentId: String,
      @Body status: Map<String, String>
   ): Response<Unit>

   @POST("appointment/")
   suspend fun bookAppointment(@Body request: AppointmentRequest): Response<AppointmentResponse>

   @Multipart
   @POST("appointment/")
   suspend fun bookAppointmentWithFile(
      @Part("center_id") centerId: RequestBody,
      @Part("service_id") serviceId: RequestBody,
      @Part("user_id") userId: RequestBody,
      @Part("appointment_date") appointmentDate: RequestBody,
      @Part transfer_letter: MultipartBody.Part?,
      @Part("booking_status") bookingStatus: RequestBody
   ): Response<AppointmentResponse>

   @GET("arvavailability/")
   suspend fun getArvAvailability(): Response<List<ArvAvailability>>

   @GET("arvavailability/{center_id}/")
   suspend fun getArvAvailabilityByCenterId(
      @Path("center_id") centerId: String
   ): Response<ArvAvailability>
}