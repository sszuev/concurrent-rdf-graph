package com.github.sszuev.graphs.testutils

import org.junit.jupiter.api.Assertions

internal fun assertFalse(actual: Boolean, message: () -> String = { "" }) {
    Assertions.assertFalse(actual) { "${threadInfo()}::${message()}" }
}

internal fun assertTrue(actual: Boolean, message: () -> String = { "" }) {
    Assertions.assertTrue(actual) { "${threadInfo()}::${message()}" }
}

internal fun assertEquals(expected: Any, actual: Any, message: () -> String = { "" }) {
    Assertions.assertEquals(expected, actual) { "${threadInfo()}::${message()}" }
}

internal fun assertThrows(vararg expectedErrors: Class<out Throwable>, block: () -> Unit) {
    try {
        block()
    } catch (ex: Throwable) {
        if (expectedErrors.any { it.isAssignableFrom(ex.javaClass) }) {
            return
        }
        println("${threadInfo()}::exception: $ex")
    }
    Assertions.fail<Unit>("${threadInfo()}::unexpected success")
}

internal fun <T> Sequence<T>.assertSingleOrEmpty(expectSingle: Boolean): T? {
    return if (expectSingle) {
        assertSingle()
    } else {
        assertEmpty()
        null
    }
}

internal fun <T> Sequence<T>.assertSingle(): T {
    val iterator = iterator()
    if (!iterator.hasNext()) {
        throw AssertionError("${threadInfo()}::[$iterator]::Iterator is empty")
    }
    val res = iterator.next()
    if (iterator.hasNext()) {
        val rest = iterator.next()
        throw AssertionError("${threadInfo()}::[$iterator]::Sequence has more than one element: $res, $rest, ...")
    }
    return res
}

internal fun <T> Sequence<T>.assertEmpty() {
    val iterator = iterator()
    if (iterator.hasNext()) {
        val res = iterator.next()
        throw AssertionError("${threadInfo()}::[$iterator]::Iterator is empty: $res, ...")
    }
}

private fun threadInfo(): String = "THREAD::[${Thread.currentThread().id}]"