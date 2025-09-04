package finance.votis.wallet.core.domain.usecase

import finance.votis.wallet.core.domain.Result
import finance.votis.wallet.core.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

/**
 * Use case for validating usernames according to business rules.
 *
 * Rules:
 * - 3-20 characters long
 * - Only lowercase alphanumerics and underscores
 * - Cannot start with underscore
 * - Must be unique (checked against backend)
 */
class ValidateUsernameUseCase(
    private val checkUsernameAvailability: suspend (String) -> Result<Boolean> = { username ->
        // For now, simulate the API call with a delay
        // In production this would call the actual backend API
        delay(500)

        // Simulate some taken usernames for testing
        val takenUsernames = setOf("admin", "test", "user", "votis", "wallet")
        if (takenUsernames.contains(username.lowercase())) {
            Result.success(false) // Username is taken
        } else {
            Result.success(true) // Username is available
        }
    },
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<String, UsernameValidationResult>(coroutineDispatcher) {
    override suspend fun execute(parameters: String): UsernameValidationResult {
        val username = parameters.trim()

        // Check length
        if (username.length < 3) {
            return UsernameValidationResult.Invalid(UsernameValidationError.TooShort)
        }

        if (username.length > 20) {
            return UsernameValidationResult.Invalid(UsernameValidationError.TooLong)
        }

        // Check for invalid characters
        if (!username.matches(Regex("^[a-z0-9_]+$"))) {
            return UsernameValidationResult.Invalid(UsernameValidationError.InvalidCharacters)
        }

        // Check if starts with underscore
        if (username.startsWith("_")) {
            return UsernameValidationResult.Invalid(UsernameValidationError.StartsWithUnderscore)
        }

        // Check availability with backend
        return when (val result = checkUsernameAvailability(username)) {
            is Result.Success -> {
                if (result.data) {
                    UsernameValidationResult.Valid(username)
                } else {
                    UsernameValidationResult.Invalid(UsernameValidationError.AlreadyTaken)
                }
            }
            is Result.Failure -> {
                UsernameValidationResult.Invalid(UsernameValidationError.NetworkError(result.error))
            }
        }
    }
}

/**
 * Result of username validation.
 */
sealed class UsernameValidationResult {
    data class Valid(
        val username: String,
    ) : UsernameValidationResult()

    data class Invalid(
        val error: UsernameValidationError,
    ) : UsernameValidationResult()
}

/**
 * Different types of username validation errors.
 */
sealed class UsernameValidationError {
    data object TooShort : UsernameValidationError()

    data object TooLong : UsernameValidationError()

    data object InvalidCharacters : UsernameValidationError()

    data object StartsWithUnderscore : UsernameValidationError()

    data object AlreadyTaken : UsernameValidationError()

    data class NetworkError(
        val throwable: Throwable,
    ) : UsernameValidationError()
}
