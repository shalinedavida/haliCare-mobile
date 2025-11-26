package com.halicare.halicare.repository


import com.halicare.halicare.api.ApiInterface
import com.halicare.halicare.model.CounselingCenterDetails

class CounselingCenterRepository(private val api: ApiInterface) {
    suspend fun getCenters(): List<CounselingCenterDetails> {
        val response = api.getCounselingCenters()
        return if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception("API failed with code: ${response.code()}")
        }
    }
}

