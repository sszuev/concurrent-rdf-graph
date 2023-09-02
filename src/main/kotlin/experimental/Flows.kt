package com.github.sszuev.graphs.experimental

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.apache.jena.util.iterator.ExtendedIterator
import org.apache.jena.util.iterator.WrappedIterator
import kotlin.coroutines.CoroutineContext

@Suppress("OPT_IN_USAGE")
fun <T> Flow<T>.asExtendedIterator(coroutineContext: CoroutineContext = Dispatchers.IO): ExtendedIterator<T> {
    val receiveChannel: ReceiveChannel<T> = CoroutineScope(coroutineContext).produce(capacity = 0) {
        this@asExtendedIterator.collect {
            send(it)
        }
    }
    val sequence = sequence<T> {
        while (!receiveChannel.isClosedForReceive) {
            val next = runBlocking(coroutineContext) {
                try {
                    receiveChannel.receive()
                } catch (ex: ClosedReceiveChannelException) {
                    null
                }
            } ?: break
            yield(next)
        }
    }
    return object : WrappedIterator<T>(sequence.iterator()) {
        override fun close() {
            receiveChannel.cancel()
        }
    }
}