package finance.votis.wallet.feature.auth

import finance.votis.wallet.core.auth.AuthResult
import finance.votis.wallet.core.auth.AuthUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private lateinit var authClientStub: GoogleAuthClientStub
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authClientStub = GoogleAuthClientStub()
        viewModel = AuthViewModel(authClientStub)
        // Let the ViewModel initialize
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `initial state should be empty`() =
        runTest(testDispatcher) {
            val state = viewModel.uiState.first()

            assertFalse(state.isLoading)
            assertNull(state.user)
            assertNull(state.error)
            assertFalse(state.isSignedIn)
            assertTrue(state.canSignIn)
            assertFalse(state.showError)
        }

    @Test
    fun `successful sign in should update state correctly`() =
        runTest(testDispatcher) {
            val testUser =
                AuthUser(
                    id = "test_id",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                    idToken = "test_token",
                )
            authClientStub.setSignInResult(AuthResult.Success(testUser))

            viewModel.signInWithGoogle()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertFalse(state.isLoading)
            assertEquals(testUser, state.user)
            assertNull(state.error)
            assertTrue(state.isSignedIn)
            assertFalse(state.canSignIn)
            assertFalse(state.showError)
        }

    @Test
    fun `cancelled sign in should not show error`() =
        runTest(testDispatcher) {
            authClientStub.setSignInResult(AuthResult.Cancelled)

            viewModel.signInWithGoogle()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertFalse(state.isLoading)
            assertNull(state.user)
            assertNull(state.error)
            assertFalse(state.isSignedIn)
            assertTrue(state.canSignIn)
            assertFalse(state.showError)
        }

    @Test
    fun `error sign in should show error message`() =
        runTest(testDispatcher) {
            val errorMessage = "Sign in failed"
            authClientStub.setSignInResult(AuthResult.Error(message = errorMessage))

            viewModel.signInWithGoogle()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertFalse(state.isLoading)
            assertNull(state.user)
            assertEquals(errorMessage, state.error)
            assertFalse(state.isSignedIn)
            assertTrue(state.canSignIn)
            assertTrue(state.showError)
        }

    @Test
    fun `sign out should reset state`() =
        runTest(testDispatcher) {
            // First sign in
            val testUser =
                AuthUser(
                    id = "test_id",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                    idToken = "test_token",
                )
            authClientStub.setSignInResult(AuthResult.Success(testUser))
            viewModel.signInWithGoogle()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then sign out
            viewModel.signOut()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertFalse(state.isLoading)
            assertNull(state.user)
            assertNull(state.error)
            assertFalse(state.isSignedIn)
            assertTrue(state.canSignIn)
            assertFalse(state.showError)
        }

    @Test
    fun `clear error should remove error message`() =
        runTest(testDispatcher) {
            // Set error first
            authClientStub.setSignInResult(AuthResult.Error(message = "Test error"))
            viewModel.signInWithGoogle()
            testDispatcher.scheduler.advanceUntilIdle()

            // Clear error
            viewModel.clearError()

            val state = viewModel.uiState.first()
            assertNull(state.error)
            assertFalse(state.showError)
        }

    @Test
    fun `signed in user should disable sign in button`() =
        runTest(testDispatcher) {
            // Set up a user as already signed in
            val testUser =
                AuthUser(
                    id = "test_id",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                    idToken = "test_token",
                )
            authClientStub.setCurrentUser(testUser)
            authClientStub.setSignInResult(AuthResult.Success(testUser))

            viewModel.signInWithGoogle()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isSignedIn, "Should be signed in")
            assertFalse(state.canSignIn, "Sign in should be disabled when already signed in")
        }
}
