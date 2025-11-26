package com.halicare.halicare

import com.halicare.halicare.api.ApiInterface
import com.halicare.halicare.api.AuthTokenProvider
import com.halicare.halicare.model.LoginResponse
import com.halicare.halicare.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private lateinit var mockApiInterface: ApiInterface
    private lateinit var mockTokenProvider: AuthTokenProvider
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        mockkStatic(android.util.Log::class)

        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0
        every { android.util.Log.d(any(), any()) } returns 0

        mockApiInterface = mockk(relaxed = true)
        mockTokenProvider = mockk(relaxed = true)
        repository = AuthRepository(mockApiInterface, mockTokenProvider)
    }

    @After
    fun tearDown() {
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `login should return success when API call is successful`() = runTest {
        coEvery { mockApiInterface.loginUser(TestData.validLoginRequest) } returns TestData.successfulLoginResponse

        val result = repository.login(
            TestData.validLoginRequest.phone_number,
            TestData.validLoginRequest.password
        )
        assertTrue(result.isSuccess)
        assertEquals(TestData.loginResponse, result.getOrNull())
        coVerify { mockApiInterface.loginUser(TestData.validLoginRequest) }
    }

    @Test
    fun `login should return failure when API returns an error response`() = runTest {
        coEvery { mockApiInterface.loginUser(TestData.validLoginRequest) } returns TestData.unsuccessfulLoginResponse

        val result = repository.login(
            TestData.validLoginRequest.phone_number,
            TestData.validLoginRequest.password
        )
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception?.message?.contains("Invalid phone number or password") == true)
        coVerify { mockApiInterface.loginUser(TestData.validLoginRequest) }
    }

    @Test
    fun `login should return failure with specific error when API returns Invalid credentials`() =
        runTest {
            val errorResponse = retrofit2.Response.error<LoginResponse>(
                401,
                okhttp3.ResponseBody.create(
                    "application/json".toMediaTypeOrNull(),
                    "{\"detail\":\"Invalid credentials\"}"
                )
            )
            coEvery { mockApiInterface.loginUser(TestData.validLoginRequest) } returns errorResponse

            val result = repository.login(
                TestData.validLoginRequest.phone_number,
                TestData.validLoginRequest.password
            )
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertNotNull(exception)
            assertEquals("Invalid phone number or password.", exception?.message)
            coVerify { mockApiInterface.loginUser(TestData.validLoginRequest) }
        }

    @Test
    fun `login should return failure with specific error when API returns No active account found`() =
        runTest {
            val errorResponse = retrofit2.Response.error<LoginResponse>(
                401,
                okhttp3.ResponseBody.create(
                    "application/json".toMediaTypeOrNull(),
                    "{\"detail\":\"No active account found\"}"
                )
            )
            coEvery { mockApiInterface.loginUser(TestData.validLoginRequest) } returns errorResponse

            val result = repository.login(
                TestData.validLoginRequest.phone_number,
                TestData.validLoginRequest.password
            )
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertNotNull(exception)
            assertEquals("Invalid phone number or password.", exception?.message)
            coVerify { mockApiInterface.loginUser(TestData.validLoginRequest) }
        }

    @Test
    fun `register should return success when API call is successful`() = runTest {
        coEvery { mockApiInterface.registerUser(TestData.signUpRequest) } returns TestData.successfulRegisterResponse
        val result = repository.register(TestData.signUpRequest)
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        coVerify { mockApiInterface.registerUser(TestData.signUpRequest) }
    }

    @Test
    fun `register should return failure when API returns an error response`() = runTest {
        coEvery { mockApiInterface.registerUser(TestData.signUpRequest) } returns TestData.unsuccessfulRegisterResponse
        val result = repository.register(TestData.signUpRequest)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception?.message?.contains("already exists") == true)
        coVerify { mockApiInterface.registerUser(TestData.signUpRequest) }
    }

    @Test
    fun `register should return failure with specific error when API returns already exists`() =
        runTest {
            val errorResponse = retrofit2.Response.error<Unit>(
                400,
                okhttp3.ResponseBody.create(
                    "application/json".toMediaTypeOrNull(),
                    "{\"detail\":\"User with this phone number already exists\"}"
                )
            )
            coEvery { mockApiInterface.registerUser(TestData.signUpRequest) } returns errorResponse

            val result = repository.register(TestData.signUpRequest)
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertNotNull(exception)
            assertEquals("An account with this phone number already exists.", exception?.message)
            coVerify { mockApiInterface.registerUser(TestData.signUpRequest) }
        }

    @Test
    fun `login should return failure when a network exception occurs`() = runTest {
        val networkException = IOException("Network unreachable")
        coEvery { mockApiInterface.loginUser(TestData.validLoginRequest) } throws networkException
        val result = repository.login(
            TestData.validLoginRequest.phone_number,
            TestData.validLoginRequest.password
        )
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception?.message?.contains("Login Network error: Network unreachable") == true)
        coVerify { mockApiInterface.loginUser(TestData.validLoginRequest) }
    }

    @Test
    fun `register should return failure when a network exception occurs`() = runTest {
        val networkException = SocketTimeoutException("timeout")
        coEvery { mockApiInterface.registerUser(TestData.signUpRequest) } throws networkException
        val result = repository.register(TestData.signUpRequest)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception?.message?.contains("Register Network error: timeout") == true)
        coVerify { mockApiInterface.registerUser(TestData.signUpRequest) }
    }

    @Test
    fun `login should return failure when response body is empty`() = runTest {
        val emptyResponse = retrofit2.Response.success<LoginResponse>(null)
        coEvery { mockApiInterface.loginUser(TestData.validLoginRequest) } returns emptyResponse

        val result = repository.login(
            TestData.validLoginRequest.phone_number,
            TestData.validLoginRequest.password
        )
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertEquals("Login: Empty response body", exception?.message)
        coVerify { mockApiInterface.loginUser(TestData.validLoginRequest) }
    }

}