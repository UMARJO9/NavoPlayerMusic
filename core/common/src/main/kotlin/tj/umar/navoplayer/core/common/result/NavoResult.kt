package tj.umar.navoplayer.core.common.result

import kotlin.coroutines.cancellation.CancellationException

sealed interface NavoResult<out T> {
    data class Success<T>(val data: T) : NavoResult<T>
    data class Error(val throwable: Throwable) : NavoResult<Nothing>
}

inline fun <T, R> NavoResult<T>.map(transform: (T) -> R): NavoResult<R> = when (this) {
    is NavoResult.Success -> NavoResult.Success(transform(data))
    is NavoResult.Error -> this
}

inline fun <T> NavoResult<T>.onSuccess(action: (T) -> Unit): NavoResult<T> {
    if (this is NavoResult.Success) action(data)
    return this
}

inline fun <T> NavoResult<T>.onError(action: (Throwable) -> Unit): NavoResult<T> {
    if (this is NavoResult.Error) action(throwable)
    return this
}

suspend inline fun <T> navoRunCatching(crossinline block: suspend () -> T): NavoResult<T> =
    try {
        NavoResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        NavoResult.Error(e)
    }
