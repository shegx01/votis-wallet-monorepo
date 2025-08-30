package finance.votis.wallet.core.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base interface for all user intents in MVI pattern.
 */
interface BaseIntent

/**
 * Base interface for all UI states in MVI pattern.
 */
interface BaseUiState

/**
 * Base interface for one-time UI effects (navigation, snackbar, etc.).
 */
interface BaseUiEffect

/**
 * Base ViewModel that implements MVI pattern with StateFlow.
 * All feature ViewModels should extend this class.
 */
abstract class BaseViewModel<State : BaseUiState, Intent : BaseIntent>(
    initialState: State,
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    protected val currentState: State
        get() = _uiState.value

    /**
     * Updates the UI state.
     */
    protected fun updateState(newState: State) {
        _uiState.value = newState
    }

    /**
     * Updates the UI state using a reducer function.
     */
    protected fun updateState(reducer: State.() -> State) {
        _uiState.value = currentState.reducer()
    }

    /**
     * Handles user intents. Override this in your ViewModels.
     */
    abstract fun handleIntent(intent: Intent)

    /**
     * Launches a coroutine in the ViewModel scope.
     */
    protected fun launchViewModelScope(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}

/**
 * Base ViewModel with UI effects support.
 */
abstract class BaseViewModelWithEffects<State : BaseUiState, Intent : BaseIntent, Effect : BaseUiEffect>(
    initialState: State,
) : BaseViewModel<State, Intent>(initialState) {
    private val _uiEffect = MutableStateFlow<Effect?>(null)
    val uiEffect: StateFlow<Effect?> = _uiEffect.asStateFlow()

    /**
     * Sends a one-time UI effect.
     */
    protected fun sendEffect(effect: Effect) {
        _uiEffect.value = effect
    }

    /**
     * Clears the current UI effect.
     */
    fun clearEffect() {
        _uiEffect.value = null
    }
}

/**
 * Common loading state for UI operations.
 */
data class LoadingState(
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val hasError: Boolean get() = error != null

    companion object {
        val Idle = LoadingState()
        val Loading = LoadingState(isLoading = true)

        fun Error(message: String) = LoadingState(error = message)
    }
}
