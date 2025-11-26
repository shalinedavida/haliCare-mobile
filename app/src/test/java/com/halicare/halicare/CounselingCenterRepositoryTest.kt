package com.halicare.halicare


import com.halicare.halicare.api.ApiInterface
import com.halicare.halicare.model.CounselingCenterDetails
import com.halicare.halicare.repository.CounselingCenterRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response


class CounselingCenterRepositoryTest {


    private val api = mockk<ApiInterface>()
    private val repository = CounselingCenterRepository(api)


    private val sampleCenters = listOf(
        CounselingCenterDetails(
            center_id = "1",
            center_name = "Jomo Kenyata hosptital",
            address = "Nairobi",
            latitude = 12.34,
            longitude = 56.78,
            operational_status = "Open",
            image_path = null
        )
    )


    @Test
    fun `getCenters returns list on success`() = runTest {
        coEvery { api.getCounselingCenters() } returns Response.success(sampleCenters)


        val result = repository.getCenters()


        assertEquals(sampleCenters, result)
    }


    @Test(expected = Exception::class)
    fun `getCenters throws exception on API failure`() = runTest {
        coEvery { api.getCounselingCenters() } returns Response.error(
            500,
            "Error".toResponseBody("text/plain".toMediaTypeOrNull())
        )


        repository.getCenters()
    }
}

