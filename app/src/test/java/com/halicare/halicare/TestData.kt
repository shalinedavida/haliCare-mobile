package com.halicare.halicare

import com.halicare.halicare.model.*
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

object TestData {

    val validLoginRequest = LoginRequest("1234567890", "password")

    val loginResponse = LoginResponse(
        token = "dummy_token",
        userId = "user123",
        userType = "PATIENT",
        firstName = "Shir",
        lastName = "Ley",
        phoneNumber = "0712345678"
    )

    val signUpRequest = SignUpRequest(
        first_name = "Mary",
        last_name = "Njeri",
        phone_number = "0734567890",
        password = "password",
        confirmPassword = "password"
    )

    val successfulLoginResponse: Response<LoginResponse> = Response.success(loginResponse)
    val unsuccessfulLoginResponse: Response<LoginResponse> = Response.error(401, "{\"error\": \"Invalid credentials\"}".toResponseBody())
    val successfulRegisterResponse: Response<Unit> = Response.success(Unit)
    val unsuccessfulRegisterResponse: Response<Unit> = Response.error(400, "{\"error\": \"Phone already exists\"}".toResponseBody())

    val clinicDetails1 = ClinicDetails(
        center_id = "1",
        center_name = "Test Clinic 1",
        center_type = "Hospital",
        image_path = "https://example.com/clinic1.jpg",
        address = "123 Test Street",
        latitude = -1.286389,
        longitude = 36.817223,
        contact_number = "+254712345678",
        operational_status = "Open",
        opening_time = "08:00",
        closing_time = "17:00",
        updated_at = "2025-10-01T08:00:00Z",
        user = "admin"
    )

    val clinicDetails2 = ClinicDetails(
        center_id = "2",
        center_name = "Test Clinic 2",
        center_type = "Clinic",
        image_path = "https://example.com/clinic2.jpg",
        address = "456 Test Avenue",
        latitude = -1.276389,
        longitude = 36.827223,
        contact_number = "+254712345679",
        operational_status = "Open",
        opening_time = "09:00",
        closing_time = "18:00",
        updated_at = "2025-10-01T08:00:00Z",
        user = "admin"
    )

    val clinicDetailsList = listOf(clinicDetails1, clinicDetails2)

    val clinicService1 = ClinicService(
        serviceId = "service1",
        serviceName = "ARV Refills",
        centerId = "1",
        hours = "24/7",
        description = "Antiretroviral drugs offered after screening",
        status = "Available"
    )

    val clinicService2 = ClinicService(
        serviceId = "service2",
        serviceName = "General Consultation",
        centerId = "1",
        hours = "8:00 - 17:00",
        description = "General medical consultation services",
        status = "Available"
    )

    val clinicServicesList = listOf(clinicService1, clinicService2)

    val clinicDetail1 = ClinicDetail(
        center_id = "1",
        center_name = "Test Clinic 1",
        address = "123 Test Street",
        latitude = -1.286389,
        longitude = 36.817223,
        contact_number = "+254712345678",
        opening_time = "08:00",
        closing_time = "17:00",
        image_path = "https://example.com/clinic1.jpg"
    )


    val clinic1 = Clinic(
        name = "Test Clinic 1",
        location = "123 Test Street",
        address = "123 Test Street",
        hours = "08:00 – 17:00",
        services = clinicServicesList,
        contact = ContactInfo("+254712345678")
    )

    val appointment1 = Appointment(
        appointmentId = "1",
        bookingStatus = "Upcoming",
        transferLetter = null,
        appointmentDate = "2025-10-07T08:00:00Z",
        userId = "test_user_id",
        centerId = "1",
        serviceId = "service1"
    ).apply {
        clinicName = "Test Clinic 1"
        serviceName = "ARV Refills"
        imageUrl = "https://example.com/clinic1.jpg"
    }

    val appointment2 = Appointment(
        appointmentId = "2",
        bookingStatus = "Completed",
        transferLetter = null,
        appointmentDate = "2025-10-08T09:00:00Z",
        userId = "test_user_id",
        centerId = "2",
        serviceId = "service2"
    ).apply {
        clinicName = "Test Clinic 2"
        serviceName = "General Consultation"
        imageUrl = "https://example.com/clinic2.jpg"
    }

    val appointment3 = Appointment(
        appointmentId = "3",
        bookingStatus = "Cancelled",
        transferLetter = null,
        appointmentDate = "2025-10-09T10:00:00Z",
        userId = "test_user_id",
        centerId = "1",
        serviceId = "service3"
    ).apply {
        clinicName = "Test Clinic 1"
        serviceName = "Lab Test"
        imageUrl = "https://example.com/clinic1.jpg"
    }

    val appointment4 = Appointment(
        appointmentId = "4",
        bookingStatus = "Completed",
        transferLetter = null,
        appointmentDate = "2025-10-10T11:00:00Z",
        userId = "different_user_id",
        centerId = "1",
        serviceId = "service3"
    ).apply {
        clinicName = "Test Clinic 1"
        serviceName = "Lab Test"
        imageUrl = "https://example.com/clinic1.jpg"
    }

    val appointmentsList = listOf(appointment1, appointment2, appointment3)

    val appointmentRequest = AppointmentRequest(
        centerId = "1",
        serviceId = "service1",
        userId = "test_user_id",
        appointmentDate = "2025-10-07T08:00:00Z"
    )

    val appointmentResponse = AppointmentResponse(
        appointmentId = "123",
        detail = "Appointment booked successfully"
    )
}