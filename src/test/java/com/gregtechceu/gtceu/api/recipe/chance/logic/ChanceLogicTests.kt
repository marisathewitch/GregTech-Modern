package com.gregtechceu.gtceu.api.recipe.chance.logic

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.gametest.utils.Asserter
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraftforge.gametest.GameTestHolder
import net.minecraftforge.gametest.PrefixGameTestTemplate

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
object ChanceLogicTests {

    private val NONE = ChanceBoostFunction.NONE
    private val MAX_CHANCE = ChanceLogic.getMaxChancedValue()

    private val isCloseTo: Int.(Int) -> Boolean = { expected -> (expected - 1 <= this) && (this <= expected + 1) }

    @JvmStatic
    @GameTest(template = "empty")
    fun `OR Logic -- one roll`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val logic = ChanceLogic.OR

        val entries = listOf(
            Content("a", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("b", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("c", MAX_CHANCE, MAX_CHANCE, 0, null, null))

        val out = logic.roll(entries, NONE, 0, 0, 1)
        asserter.assertTrue(out.isNotEmpty())
        asserter.assertEquals(entries, out)
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `OR Logic -- ten rolls`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val logic = ChanceLogic.OR

        val entries = listOf(
            Content("a", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("b", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("c", MAX_CHANCE, MAX_CHANCE, 0, null, null))

        val out = logic.roll(entries, NONE, 0, 0, 10)
        asserter.assertTrue(out.isNotEmpty())
        asserter.assertEquals(out.size, 30)
        asserter.assertTrue(out.subList(0, 10).all { it == entries[0] }) // 0-9 should be "a" content
        asserter.assertTrue(out.subList(10, 20).all { it == entries[1] }) // 10-19 should be "b" content
        asserter.assertTrue(out.subList(20, 30).all { it == entries[2] }) // 20-29 should be "c" content
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `OR Logic -- determinism`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val logic = ChanceLogic.OR
        val cache = Object2IntOpenHashMap<Any>()

        val entries = listOf(
            Content("a", 2000, MAX_CHANCE, 0, null, null), // 1/5
            Content("b", 5000, MAX_CHANCE, 0, null, null), // 1/2
            Content("c", 8000, MAX_CHANCE, 0, null, null)) // 4/5

        val out = logic.roll(entries, NONE, 0, 0, cache, 1).toMutableList()
        for(i in 1..9) {
            out.addAll(logic.roll(entries, NONE, 0, 0, cache, 1))
        }
        val map = out.groupingBy { it }.eachCount()
        asserter.assertTrue(map.containsKey(entries[0]) && map[entries[0]]!!.isCloseTo(2))
        asserter.assertTrue(map.containsKey(entries[1]) && map[entries[1]]!!.isCloseTo(5))
        asserter.assertTrue(map.containsKey(entries[2]) && map[entries[2]]!!.isCloseTo(8))
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `OR Logic -- guaranteed`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val logic = ChanceLogic.OR
        val cache = Object2IntOpenHashMap<Any>()

        val entries = listOf(
            Content("a", 2000, MAX_CHANCE, 0, null, null), // 1/5
            Content("b", 5000, MAX_CHANCE, 0, null, null), // 1/2
            Content("c", 8000, MAX_CHANCE, 0, null, null)) // 4/5

        val out = logic.roll(entries, NONE, 0, 0, cache, 10);
        val map = out.groupingBy { it }.eachCount()
        asserter.assertTrue(map.containsKey(entries[0]) && map[entries[0]] == 2)
        asserter.assertTrue(map.containsKey(entries[1]) && map[entries[1]] == 5)
        asserter.assertTrue(map.containsKey(entries[2]) && map[entries[2]] == 8)
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `AND Logic`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val logic = ChanceLogic.AND

        var entries = listOf(
            Content("a", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("b", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("c", 0, MAX_CHANCE, 0, null, null))

        var out = logic.roll(entries, NONE, 0, 0, 1)
        asserter.assertTrue(out.isEmpty())

        entries = listOf(
            Content("a", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("b", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("c", MAX_CHANCE, MAX_CHANCE, 0, null, null))
        out = logic.roll(entries, NONE, 0, 0, 1)
        asserter.assertEquals(entries, out)

        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `XOR Logic`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val logic = ChanceLogic.XOR

        val entries = listOf(
            Content("a", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("b", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("c", MAX_CHANCE, MAX_CHANCE, 0, null, null))

        val out = logic.roll(entries, NONE, 0, 0, 1)
        asserter.assertEquals(out.size, 1)
        asserter.assertTrue(out[0] == entries[0])
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `NONE Logic`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val logic = ChanceLogic.NONE

        val entries = listOf(
            Content("a", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("b", MAX_CHANCE, MAX_CHANCE, 0, null, null),
            Content("c", MAX_CHANCE, MAX_CHANCE, 0, null, null))

        val out = logic.roll(entries, NONE, 0, 0, 1)
        asserter.assertTrue(out.isEmpty())
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `Passes Chance`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        asserter.assertTrue(ChanceLogic.passesChance(MAX_CHANCE, MAX_CHANCE))
        asserter.assertTrue(ChanceLogic.passesChance(15_000, MAX_CHANCE))
        asserter.assertFalse(ChanceLogic.passesChance(5000, MAX_CHANCE))
        helper.succeed()
    }
}
