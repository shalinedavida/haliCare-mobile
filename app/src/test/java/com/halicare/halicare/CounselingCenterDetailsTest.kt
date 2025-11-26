package com.halicare.halicare


import com.halicare.halicare.model.CounselingCenterDetails
import com.halicare.halicare.repository.CounselingCenterRepository
import com.halicare.halicare.viewModel.CounselingCenterViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.collections.emptyList




@OptIn(ExperimentalCoroutinesApi::class)
class CounselingCenterViewModelTest {


    private val testDispatcher = StandardTestDispatcher()


    private val repository: CounselingCenterRepository = mockk()


    private lateinit var viewModel: CounselingCenterViewModel


    private val sampleCenters = listOf(
        CounselingCenterDetails(
            center_id = "1",
            center_name = "Jomo Kenyata Hospital",
            address = "Nairobi",
            latitude = 12.34,
            longitude = 56.78,
            operational_status = "Open",
            image_path = null
        )
    )


    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CounselingCenterViewModel(repository)
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `loadCenters updates centers and loading states on success`() = runTest {
        coEvery { repository.getCenters() } returns sampleCenters

        viewModel.loadCenters()

        advanceUntilIdle()

        assertEquals(sampleCenters, viewModel.centers.value)
        assertEquals(false, viewModel.isLoading.value)
        assertEquals(null, viewModel.errorMessage.value)
    }

    @Test
    fun `loadCenters updates errorMessage and loading states on failure`() = runTest {
        val errorMessage = "Something went wrong"
        coEvery { repository.getCenters() } throws Exception(errorMessage)

        viewModel.loadCenters()

        advanceUntilIdle()

        assertEquals(listOf<CounselingCenterDetails>(), viewModel.centers.value)
        assertEquals(false, viewModel.isLoading.value)
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }
}

