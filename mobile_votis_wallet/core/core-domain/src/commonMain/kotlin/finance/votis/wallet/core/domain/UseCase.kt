package finance.votis.wallet.core.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Base class for use cases that execute business logic.
 * All use cases should extend this class to ensure consistent error handling and threading.
 */
abstract class UseCase<in P, out R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    /**
     * Executes the use case with the given parameters.
     * Automatically handles threading and wraps results in Result type.
     */
    suspend operator fun invoke(parameters: P): Result<R> =
        try {
            withContext(coroutineDispatcher) {
                Result.success(execute(parameters))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    /**
     * Override this method to implement the use case logic.
     */
    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(parameters: P): R
}

/**
 * Base class for use cases that don't require parameters.
 */
abstract class NoParameterUseCase<out R>(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UseCase<Unit, R>(coroutineDispatcher) {
    suspend operator fun invoke(): Result<R> = invoke(Unit)
}

/**
 * Base class for use cases that return a Flow.
 */
abstract class FlowUseCase<in P, out R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    operator fun invoke(parameters: P): Flow<R> =
        execute(parameters)
            .flowOn(coroutineDispatcher)

    protected abstract fun execute(parameters: P): Flow<R>
}

/**
 * Base class for use cases that return a Flow and don't require parameters.
 */
abstract class NoParameterFlowUseCase<out R>(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : FlowUseCase<Unit, R>(coroutineDispatcher) {
    operator fun invoke(): Flow<R> = invoke(Unit)
}
