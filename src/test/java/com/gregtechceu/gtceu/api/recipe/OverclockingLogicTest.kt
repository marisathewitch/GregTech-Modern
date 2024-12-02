package com.gregtechceu.gtceu.api.recipe

import com.gregtechceu.gtceu.api.GTValues.*
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic.*
import com.gregtechceu.gtceu.api.recipe.logic.OCParams
import com.gregtechceu.gtceu.api.recipe.logic.OCResult
import com.gregtechceu.gtceu.config.ConfigHolder

import org.junit.jupiter.api.BeforeAll

import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OverclockingLogicTest {

    @Test
    fun `test ULV overclocking`() {
        val recipeDuration = 32768
        val tier = ULV
        val eut = V[tier]
        val logic = NON_PERFECT_OVERCLOCK

        var machineTier = LV
        val machineEUt = { V[machineTier] }

        var result = applyOC(recipeDuration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut)
        assertEquals(result.duration, recipeDuration)

        machineTier = MV
        result = applyOC(recipeDuration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut * 4)
        assertEquals(result.duration, recipeDuration / 2)

        machineTier = HV
        result = applyOC(recipeDuration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut * 4 * 4)
        assertEquals(result.duration, recipeDuration / (2 * 2))
    }

    @Test
    fun `test ULV overclocking 2`() {
        val duration = 32768
        val tier = ULV
        val eut = V[tier] * 2
        val logic = NON_PERFECT_OVERCLOCK

        var machineTier = LV
        val machineEUt = { V[machineTier] }
        var result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut)
        assertEquals(result.duration, duration)

        machineTier = MV
        result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut * 4)
        assertEquals(result.duration, duration / 2)

        machineTier = HV
        result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut * 4 * 4)
        assertEquals(result.duration, duration / (2 * 2))
    }

    @Test
    fun `test LV overclocking`() {
        val duration = 32768
        val tier = LV
        val eut = V[tier]
        val logic = NON_PERFECT_OVERCLOCK

        var machineTier = LV
        val machineEUt = { V[machineTier] }
        var result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut)
        assertEquals(result.duration, duration)

        machineTier = MV
        result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut * 4)
        assertEquals(result.duration, duration / 2)

        machineTier = HV
        result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut * 4 * 4)
        assertEquals(result.duration, duration / (2 * 2))
    }

    @Test
    fun `test subtick parallel overclocking`() {
        val duration = 2
        val tier = LV
        val eut = V[tier]
        val logic = NON_PERFECT_OVERCLOCK_SUBTICK

        var machineTier = LV
        val machineEUt = { V[machineTier] }
        var result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut)
        assertEquals(result.duration, duration)
        assertTrue(result.parallel <= 1)

        machineTier = MV
        result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut * 4)
        assertEquals(result.duration, duration / 2)
        assertTrue(result.parallel <= 1)

        machineTier = HV
        result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut * 4)
        assertEquals(result.duration, 1)
        assertEquals(result.parallel, 2)
        assertEquals(result.parallelEUt, eut * 4 * 4)

        machineTier = EV
        result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut * 4)
        assertEquals(result.duration, 1)
        assertEquals(result.parallel, 2 * 2)
        assertEquals(result.parallelEUt, eut * 4 * 4 * 4)
    }

    @Test
    fun `test perfect subtick parallel overclocking`() {
        val duration = 2
        val tier = LV
        val eut = V[tier]
        val logic = PERFECT_OVERCLOCK_SUBTICK

        var machineTier = LV
        val machineEUt = { V[machineTier] }
        var result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut)
        assertEquals(result.duration, duration)
        assertTrue(result.parallel <= 1)

        machineTier = MV
        result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut)
        assertEquals(result.duration, duration)
        assertEquals(result.parallel, 4)
        assertEquals(result.parallelEUt, eut * 4)

        machineTier = HV
        result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut)
        assertEquals(result.duration, duration)
        assertEquals(result.parallel, 4 * 4)
        assertEquals(result.parallelEUt, eut * 4 * 4)

        machineTier = EV
        result = applyOC(duration, tier, eut, machineTier, machineEUt(), logic)
        assertEquals(result.eut, eut)
        assertEquals(result.duration, duration)
        assertEquals(result.parallel, 4 * 4 * 4)
        assertEquals(result.parallelEUt, eut * 4 * 4 * 4)
    }

    @Test
    fun `test heating coil subtick overclocking`() {
        val duration = 4
        val tier = LV
        val eut = V[LV]

        var machineTier = HV
        val machineEUt = { V[machineTier] }

        var recipeHeat = 1800
        var machineHeat = 1800 // 0 discount, 2 OC
        var result =
            applyCoilOC(duration, tier, eut, machineTier, machineEUt(), recipeHeat, machineHeat)
        assertEquals(result.eut, eut * 4 * 4)
        assertEquals(result.duration, duration / (2 * 2))

        recipeHeat = 1800
        machineHeat = 2700 // 1 discount, 2 OC
        result =
            applyCoilOC(duration, tier, eut, machineTier, machineEUt(), recipeHeat, machineHeat)
        assertEquals(result.eut, (eut * 0.95.pow(1)).toLong() * 4 * 4)
        assertEquals(result.duration, duration / (2 * 2))

        recipeHeat = 1800
        machineHeat = 3600 // 2 discount, 1 perfect OC, 1 OC -> 2 subtick div
        result =
            applyCoilOC(duration, tier, eut, machineTier, machineEUt(), recipeHeat, machineHeat)
        assertEquals(result.eut, (eut * 0.95.pow(2)).toLong() * 4)
        assertEquals(result.duration, 1)
        assertEquals(result.parallel, 2)
        assertEquals(result.parallelEUt, (eut * 0.95.pow(2)).toLong() * 4 * 4)

        recipeHeat = 1800
        machineHeat = 5400 // 4 discount, 2 perfect OC -> 4 subtick div
        result =
            applyCoilOC(duration, tier, eut, machineTier, machineEUt(), recipeHeat, machineHeat)
        assertEquals(result.eut, (eut * 0.95.pow(4)).toLong() * 4)
        assertEquals(result.duration, 1)
        assertEquals(result.parallel, 4)
        assertEquals(result.parallelEUt, (eut * 0.95.pow(4)).toLong() * 4 * 4)

        machineTier = EV

        recipeHeat = 1800
        machineHeat = 1800 // 0 discount, 3 OC -> 2 subtick div
        result =
            applyCoilOC(duration, tier, eut, machineTier, machineEUt(), recipeHeat, machineHeat)
        assertEquals(result.eut, eut * 4 * 4)
        assertEquals(result.duration, duration / (2 * 2))
        assertEquals(result.parallel, 2)
        assertEquals(result.parallelEUt, eut * 4 * 4 * 4)

        recipeHeat = 1800
        machineHeat = 2700 // 1 discount, 3 OC -> 2 subtick div
        result =
            applyCoilOC(duration, tier, eut, machineTier, machineEUt(), recipeHeat, machineHeat)
        assertEquals(result.eut, (eut * 0.95.pow(1)).toLong() * 4 * 4)
        assertEquals(result.duration, duration / (2 * 2))
        assertEquals(result.parallel, 2)
        assertEquals(result.parallelEUt, (eut * 0.95.pow(1)).toLong() * 4 * 4 * 4)

        recipeHeat = 1800
        machineHeat = 3600 // 2 discount, 1 perfect OC, 2 OC -> 2*2 subtick
        result =
            applyCoilOC(duration, tier, eut, machineTier, machineEUt(), recipeHeat, machineHeat)
        assertEquals(result.eut, (eut * 0.95.pow(2)).toLong() * 4)
        assertEquals(result.duration, 1)
        assertEquals(result.parallel, 2 * 2)
        assertEquals(result.parallelEUt, (eut * 0.95.pow(2)).toLong() * 4 * 4 * 4)

        recipeHeat = 1800
        machineHeat = 5400 // 4 discount, 2 perfect OC, 1 OC -> 4*2 subtick
        result =
            applyCoilOC(duration, tier, eut, machineTier, machineEUt(), recipeHeat, machineHeat)
        assertEquals(result.eut, (eut * 0.95.pow(4)).toLong() * 4)
        assertEquals(result.duration, 1)
        assertEquals(result.parallel, 4 * 2)
        assertEquals(result.parallelEUt, (eut * 0.95.pow(4)).toLong() * 4 * 4 * 4)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            ConfigHolder.INSTANCE = ConfigHolder()
        }

        private fun applyOC(
            duration: Int,
            recipeTier: Int,
            recipeEUt: Long,
            machineTier: Int,
            machineEUt: Long,
            logic: OverclockingLogic,
        ): OCResult {
            var ocAmount = machineTier - recipeTier
            if (recipeTier == ULV) {
                ocAmount--
            }
            val result = OCResult()
            if (ocAmount <= 0) {
                result.init(recipeEUt, duration, ocAmount)
                return result
            }

            val params = OCParams()
            params.initialize(recipeEUt, duration, ocAmount)
            logic.logic.runOverclockingLogic(params, result, machineEUt)
            return result
        }

        private fun applyCoilOC(
            duration: Int,
            recipeTier: Int,
            recipeEUt: Long,
            machineTier: Int,
            machineEUt: Long,
            recipeHeat: Int,
            machineHeat: Int,
        ): OCResult {
            var ocAmount = machineTier - recipeTier
            if (recipeTier == ULV) {
                ocAmount--
            }
            val result = OCResult()

            val newEUt = applyCoilEUtDiscount(recipeEUt, machineHeat, recipeHeat)
            if (ocAmount <= 0) {
                result.init(newEUt, duration, ocAmount)
                return result
            }

            val params = OCParams()
            params.initialize(newEUt, duration, ocAmount)
            heatingCoilOC(params, result, machineEUt, machineHeat, recipeHeat)
            return result
        }
    }
}
