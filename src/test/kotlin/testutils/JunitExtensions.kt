package com.github.sszuev.graphs.testutils

import org.junit.jupiter.api.Assertions

internal fun assertFalse(actual: Boolean, message: () -> String = { "" }) {
    Assertions.assertFalse(actual) { "${tmsg()}::${message()}" }
}

internal fun assertTrue(actual: Boolean, message: () -> String = { "" }) {
    Assertions.assertTrue(actual) { "${tmsg()}::${message()}" }
}

internal fun assertEquals(expected: Any, actual: Any, message: () -> String = { "" }) {
    Assertions.assertEquals(expected, actual) { "${tmsg()}::${message()}" }
}

internal fun assertSafe(block: () -> Unit) {
    try {
        block()
    } catch (ex: AssertionError) {
        throw ex
    } catch (ex: Exception) {
        Assertions.fail<Unit>("${tmsg()}::${ex.message}", ex)
    }
}

internal fun <T> Sequence<T>.assertSingle(): T {
    val iterator = iterator()
    if (!iterator.hasNext()) {
        throw AssertionError("${tmsg()}::[$iterator]::Iterator is empty")
    }
    val res = iterator.next()
    if (iterator.hasNext()) {
        val rest = iterator.next()
        throw AssertionError("${tmsg()}::[$iterator]::Sequence has more than one element: $res, $rest, ...")
    }
    return res
}

private fun tmsg(): String = "THREAD::[${Thread.currentThread().id}]"