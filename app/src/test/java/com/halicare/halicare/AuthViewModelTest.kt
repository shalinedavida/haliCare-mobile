package com.halicare.halicare

import com.halicare.halicare.api.AuthTokenProvider
import com.halicare.halicare.repository.AuthRepository
import com.halicare.halicare.viewModel.AuthViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest : KoinTest {

    private lateinit var mockRepository: AuthRepository
    private lateinit var mockTokenProvider: AuthTokenProvider
    private lateinit var viewModel: AuthViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(android.util.Log::class)

        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0
        every { android.util.Log.d(any(), any()) } returns 0

        mockRepository = mockk()
        mockTokenProvider = mockk(relaxed = true)

        startKoin {
            modules(
                module {
                    single { mockTokenProvider }
                }
            )
        }

        viewModel = AuthViewModel(mockRepository, mockTokenProvider)
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `loginUser should update state to Success on successful login`() = runTest {
        coEvery { mockRepository.login(any(), any()) } returns Result.success(TestData.loginResponse)

        viewModel.loginUser("123", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.loginState.first()
        assertTrue(state is AuthViewModel.LoginUiState.Success)
        assertEquals(TestData.loginResponse, (state as AuthViewModel.LoginUiState.Success).response)
        coVerify { mockRepository.login("123", "password") }
        coVerify { mockTokenProvider.token = TestData.loginResponse.token }
    }

    @Test
    fun `loginUser should update state to Error on API failure`() = runTest {
        val error = Exception("Invalid credentials")
        coEvery { mockRepository.login(any(), any()) } returns Result.failure(error)

        viewModel.loginUser("123", "wrong_password")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.loginState.first()
        assertTrue(state is AuthViewModel.LoginUiState.Error)
        assertEquals("Network error: Invalid credentials", (state as AuthViewModel.LoginUiState.Error).errorMessage)
    }


    @Test
    fun `loginUser should update state to Error on network error`() = runTest {
        val networkError = Exception("Network error: Unable to resolve host")
        coEvery { mockRepository.login(any(), any()) } returns Result.failure(networkError)

        viewModel.loginUser("123", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.loginState.first()
        assertTrue(state is AuthViewModel.LoginUiState.Error)
        assertEquals("Network error: Network error: Unable to resolve host", (state as AuthViewModel.LoginUiState.Error).errorMessage)
    }

    @Test
    fun `registerUser should update state to Success on successful registration`() = runTest {
        coEvery { mockRepository.register(any()) } returns Result.success(Unit)

        viewModel.registerUser("John", "Doe", "123", "password", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.signUpState.first()
        assertTrue(state is AuthViewModel.SignUpUiState.Success)
        assertEquals("User registered successfully", (state as AuthViewModel.SignUpUiState.Success).message)
    }

    @Test
    fun `registerUser should update state to Error on API failure`() = runTest {
        val error = Exception("Phone already exists")
        coEvery { mockRepository.register(any()) } returns Result.failure(error)

        viewModel.registerUser("John", "Doe", "123", "password", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.signUpState.first()
        assertTrue(state is AuthViewModel.SignUpUiState.Error)
        assertEquals("Registration failed: Phone already exists", (state as AuthViewModel.SignUpUiState.Error).errorMessage)
    }

    @Test
    fun `registerUser should update state to Error on network error`() = runTest {
        val networkError = Exception("Network error: Request timed out")
        coEvery { mockRepository.register(any()) } returns Result.failure(networkError)

        viewModel.registerUser("John", "Doe", "123", "password", "password")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.signUpState.first()
        assertTrue(state is AuthViewModel.SignUpUiState.Error)
        assertEquals("Registration failed: Network error: Request timed out", (state as AuthViewModel.SignUpUiState.Error).errorMessage)
    }
}