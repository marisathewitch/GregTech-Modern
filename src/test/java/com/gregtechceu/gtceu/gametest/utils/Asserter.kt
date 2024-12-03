package com.gregtechceu.gtceu.gametest.utils

import net.minecraft.gametest.framework.GameTestHelper

class Asserter(private val helper: GameTestHelper) {
    private var index = 0

    fun index() = "Assertion #${index}:"
    fun inc() = "Assertion #${++index}:"

    fun assertEquals(expected: Number, actual: Number) =
        helper.assertTrue(expected == actual, "${inc()} Expected: $expected, Actual: $actual".trim())

    fun <T> assertEquals(expected: List<T>, actual: List<T>) {
        helper.assertTrue(expected.size == actual.size, "${inc()} Lists are not the same size")
        for(e in expected) {
            helper.assertTrue(actual.contains(e), "${index()} $e expected in list, not found")
        }
    }

    fun assertTrue(actual: Boolean) = helper.assertTrue(actual, "${inc()} Expected TRUE")
    fun assertFalse(actual: Boolean) = helper.assertFalse(actual, "${inc()} Expected FALSE")
}
