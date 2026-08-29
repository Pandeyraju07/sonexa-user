package com.emerald.loans

import com.emerald.loans.data.api.GeoLocationSnapshot
import com.emerald.loans.data.MobileVerificationRepository
import com.emerald.loans.data.api.MobileVerificationResponseDto
import com.emerald.loans.data.api.OtpVerificationResponseDto
import com.emerald.loans.data.RepositoryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun sendOtpRejectsInvalidMobileNumber() {
        val viewModel = LoginViewModel(FakeMobileVerificationRepository())

        viewModel.sendOtp(validLocation)

        assertEquals(
            "Please enter a valid 10-digit mobile number",
            viewModel.state.value.mobileNumberError
        )
    }

    @Test
    fun sendOtpSuccessMovesToOtpScreenAndStoresVerificationId() = runTest {
        val viewModel = LoginViewModel(FakeMobileVerificationRepository())
        viewModel.onMobileNumberChanged("7762942646")

        viewModel.sendOtp(validLocation)
        runCurrent()

        assertEquals(ScreenState.OTP, viewModel.state.value.screenState)
        assertEquals(99L, viewModel.state.value.mobileVerificationId)
        assertEquals(6, viewModel.state.value.otpValues.size)
    }

    @Test
    fun verifyOtpRequiresSixDigits() {
        val viewModel = LoginViewModel(FakeMobileVerificationRepository())

        viewModel.verifyOtp()

        assertEquals("Please enter all 6 digits of the OTP", viewModel.state.value.otpError)
    }

    @Test
    fun verifyOtpSuccessSetsSuccessState() = runTest {
        val viewModel = LoginViewModel(FakeMobileVerificationRepository())
        viewModel.onMobileNumberChanged("7762942646")
        viewModel.sendOtp(validLocation)
        runCurrent()
        "123456".forEachIndexed { index, digit ->
            viewModel.onOtpDigitChanged(index, digit.toString())
        }

        viewModel.verifyOtp()
        runCurrent()

        assertTrue(viewModel.state.value.uiState is LoginUiState.Success)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeMobileVerificationRepository : MobileVerificationRepository {
    override suspend fun sendOtp(
        mobileNumber: String,
        location: GeoLocationSnapshot
    ): RepositoryResult<MobileVerificationResponseDto> {
        return RepositoryResult.Success(
            MobileVerificationResponseDto(
                mobileVerificationId = 99L,
                mobileNumber = mobileNumber,
                status = "PENDING",
                otpExpiryAt = null,
                resendAvailableAt = null,
                otp = "123456"
            )
        )
    }

    override suspend fun verifyOtp(
        mobileVerificationId: Long,
        otp: String
    ): RepositoryResult<OtpVerificationResponseDto> {
        return RepositoryResult.Success(
            OtpVerificationResponseDto(
                mobileVerificationId = mobileVerificationId,
                mobileNumber = "7762942646",
                status = "VERIFIED",
                verifiedAt = null
            )
        )
    }

    override suspend fun resendOtp(
        mobileVerificationId: Long
    ): RepositoryResult<MobileVerificationResponseDto> {
        return RepositoryResult.Success(
            MobileVerificationResponseDto(
                mobileVerificationId = mobileVerificationId,
                mobileNumber = "7762942646",
                status = "PENDING",
                otpExpiryAt = null,
                resendAvailableAt = null,
                otp = "654321"
            )
        )
    }
}

private val validLocation = GeoLocationSnapshot(
    latitude = 20.0,
    longitude = 78.0,
    altitude = 0.0,
    accuracy = 20.0
)