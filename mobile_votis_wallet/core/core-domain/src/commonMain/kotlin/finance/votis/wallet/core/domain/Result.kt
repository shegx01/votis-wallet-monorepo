package finance.votis.wallet.core.domain

/**
 * A sealed class representing the result of an operation that can either succeed or fail.
 * This provides a type-safe way to handle errors without exceptions.
 */
sealed class Result<out T> {
    data class Success<T>(
        val data: T,
    ) : Result<T>()

    data class Failure(
        val error: Throwable,
    ) : Result<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isFailure: Boolean
        get() = this is Failure

    fun getOrNull(): T? =
        when (this) {
            is Success -> data
            is Failure -> null
        }

    fun getOrThrow(): T =
        when (this) {
            is Success -> data
            is Failure -> throw error
        }

    fun exceptionOrNull(): Throwable? =
        when (this) {
            is Success -> null
            is Failure -> error
        }

    inline fun <R> map(transform: (T) -> R): Result<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Failure -> this
        }

    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> =
        when (this) {
            is Success -> transform(data)
            is Failure -> this
        }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (Throwable) -> Unit): Result<T> {
        if (this is Failure) action(error)
        return this
    }

    companion object {
        /**
         * Creates a successful result.
         */
        fun <T> success(value: T): Result<T> = Success(value)

        /**
         * Creates a failed result.
         */
        fun <T> failure(error: Throwable): Result<T> = Failure(error)

        /**
         * Executes the given block and wraps the result in a Result.
         */
        inline fun <T> runCatching(block: () -> T): Result<T> =
            try {
                success(block())
            } catch (e: Throwable) {
                failure(e)
            }
    }
}
