package com.gregtechceu.gtceu.gametest.utils

import net.minecraft.gametest.framework.GameTestHelper

class Asserter(private val helper: GameTestHelper) {
    private var index = 0

    private fun number() = "Assertion #${++index}: "

    fun assertEquals(expected: Number, actual: Number) =
        helper.assertTrue(expected == actual, "${number()} Expected: $expected, Actual: $this".trim())

    fun assertTrue(actual: Boolean) = helper.assertTrue(actual, "${number()} Expected TRUE")
    fun assertFalse(actual: Boolean) = helper.assertFalse(actual, "${number()} Expected FALSE")
}
