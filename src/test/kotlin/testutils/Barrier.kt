package com.github.sszuev.graphs.testutils

internal interface Barrier {
    fun await()
}

internal class ThreadBarrier(numberOfTasks: Int) : Barrier {
    private val semaphore = java.util.concurrent.Semaphore(numberOfTasks, true)
    override fun await() {
        semaphore.tryAcquire()
    }
}

internal class CoroutineBarrier(numberOfTasks: Int) : Barrier {
    private val semaphore = kotlinx.coroutines.sync.Semaphore(numberOfTasks)
    override fun await() {
        semaphore.tryAcquire()
    }
}