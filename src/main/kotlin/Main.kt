import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.coroutineContext

@JvmInline
value class Option<out T> internal constructor(val data: Any?) {
    companion object {
        private val NoData = Any()
        val Empty: Option<Nothing> = Option(NoData)
        fun <T> of(value: T) = Option<T>(value)
    }

    fun isEmpty() = data === NoData
    val value: T
        get() = if (isEmpty()) throw IllegalArgumentException()
                else data as T


}

@OptIn(ExperimentalContracts::class)
inline fun <T> Option<T>.forValue(body: (T) -> Unit) {
    contract { callsInPlace(body, InvocationKind.AT_MOST_ONCE) }
    if(!isEmpty()) body(value)
}

context(CoroutineScope)
@OptIn(ExperimentalContracts::class)
suspend inline
fun <T, R> Collection<T>.mapAsync(crossinline body: suspend context(CoroutineScope) (T) -> R): List<R> {
    contract {
        callsInPlace(body)
    }
    return map { async { body(this, it) } }.awaitAll()
}

context(CoroutineScope)
suspend inline
fun <T, R> Collection<T>.flatMapAsync(crossinline body: suspend context(CoroutineScope) (T) -> Iterable<R>): List<R> =
    mapAsync(body).flatten()

context(CoroutineScope)
@OptIn(ExperimentalContracts::class)
suspend inline
fun <T> Collection<T>.filterAsync(crossinline body: suspend context(CoroutineScope) (T) -> Boolean): List<T> =
    mapAsync { t -> if (body(this@CoroutineScope, t)) Option.of(t) else Option.Empty }
        .fold(mutableListOf()) { acc, option -> option.forValue { acc.add(it) }; acc }

@OptIn(DelicateCoroutinesApi::class)
suspend fun main() {
    newFixedThreadPoolContext(10, "moo").use { dispatcher ->
        coroutineScope {
            launch(dispatcher) {
                val cc = (0..30000).toList()
                    .filterAsync { println(it); it % 4 == 0 }
                    .mapAsync { "x$it".also { println(it) } }
                println(cc)
            }.join()
        }
    }
}