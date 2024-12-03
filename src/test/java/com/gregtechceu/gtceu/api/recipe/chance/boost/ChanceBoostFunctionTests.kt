package com.gregtechceu.gtceu.api.recipe.chance.boost

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues.*
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.gametest.utils.Asserter

import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraftforge.gametest.GameTestHolder
import net.minecraftforge.gametest.PrefixGameTestTemplate

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
object ChanceBoostFunctionTests {
    private const val DELAY = 40L

    @JvmStatic
    @GameTest(template = "empty")
    fun `OC Chance Boost -- 0 boost`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val func = ChanceBoostFunction.OVERCLOCK
        val assertEquals: Int.(Int) -> (Unit) = { asserter.assertEquals(it, this) }

        val base = 5000
        val max = ChanceLogic.getMaxChancedValue()
        val entry = Content(null, base, max, 0, null, null)
        // No boost, chance should not change regardless of tier diff
        func.getBoostedChance(entry, LV, LV).assertEquals(base)
        func.getBoostedChance(entry, LV, EV).assertEquals(base)
        func.getBoostedChance(entry, LV, UV).assertEquals(base)
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `OC Chance Boost -- +1000 boost`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val func = ChanceBoostFunction.OVERCLOCK
        val assertEquals: Int.(Int) -> (Unit) = { asserter.assertEquals(it, this) }

        val base = 5000
        val boost = 1000
        val max = ChanceLogic.getMaxChancedValue()
        val entry = Content(null, base, max, boost, null, null)
        // Chance should increase by boost * tierDiff
        func.getBoostedChance(entry, HV, HV).assertEquals(base) // Equal tiers do not boost
        func.getBoostedChance(entry, MV, IV).assertEquals(base + boost * (IV - MV)) // IV - MV = 3
        func.getBoostedChance(entry, LV, LuV).assertEquals(base + boost * (LuV - LV)) // LuV - LV = 5
        func.getBoostedChance(entry, LV, UV).assertEquals(max) // UV - LV > 5; Clamped at max chance
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `OC Chance Boost -- -1000 boost`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val func = ChanceBoostFunction.OVERCLOCK
        val assertEquals: Int.(Int) -> (Unit) = { asserter.assertEquals(it, this) }

        val base = 5000
        val boost = -1000
        val max = ChanceLogic.getMaxChancedValue()
        val entry = Content(null, base, max, boost, null, null)
        // Chance should decrease by boost * tierDiff
        func.getBoostedChance(entry, HV, HV).assertEquals(base) // Equal tiers do not boost
        func.getBoostedChance(entry, MV, IV).assertEquals(base + boost * (IV - MV)) // IV - MV = 3
        func.getBoostedChance(entry, LV, LuV).assertEquals(base + boost * (LuV - LV)) // LuV - LV = 5
        func.getBoostedChance(entry, LV, UV).assertEquals(0) // UV - LV > 5; Clamped at 0
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `OC Chance Boost -- 5001 max, +1000 boost`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val func = ChanceBoostFunction.OVERCLOCK
        val assertEquals: Int.(Int) -> (Unit) = { asserter.assertEquals(it, this) }

        val base = 2501
        var boost = 1000
        val max = 5001
        val entry = Content(null, base, max, boost, null, null)
        boost = entry.tierChanceBoost // boost should get adjusted to 500 since non-standard max
        boost.assertEquals(500)
        // Chance should increase by boost * tierDiff but clamp at 5001
        func.getBoostedChance(entry, HV, HV).assertEquals(base) // Equal tiers do not boost
        func.getBoostedChance(entry, LV, HV).assertEquals(base + boost * (HV - LV)) // HV - LV = 2 -> 3501
        func.getBoostedChance(entry, LV, IV).assertEquals(base + boost * (IV - LV)) // IV - LV = 4 -> 4501
        func.getBoostedChance(entry, LV, LuV).assertEquals(base + boost * (LuV - LV)) // ZPM - LV = 5 -> 5001
        func.getBoostedChance(entry, LV, UV).assertEquals(max) // UV - LV > 5; Clamped to max
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `OC Chance Boost -- ULV should not boost`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val func = ChanceBoostFunction.OVERCLOCK
        val assertEquals: Int.(Int) -> (Unit) = { asserter.assertEquals(it, this) }

        val base = 5000
        val boost = 1000
        val max = ChanceLogic.getMaxChancedValue()
        val entry = Content(null, base, max, boost, null, null)
        // ULV should not provide a tier boost
        func.getBoostedChance(entry, ULV, ULV).assertEquals(base) // Equal tiers do not boost
        func.getBoostedChance(entry, ULV, LV).assertEquals(base) // LV - ULV = 1 -> 0 boost

        // Boosted chances should be equal if recipe tier is ULV or LV
        var chance1 = func.getBoostedChance(entry, ULV, HV)
        var chance2 = func.getBoostedChance(entry, LV, HV)
        chance1.assertEquals(chance2)

        chance1 = func.getBoostedChance(entry, ULV, EV)
        chance2 = func.getBoostedChance(entry, LV, EV)
        chance1.assertEquals(chance2)

        chance1 = func.getBoostedChance(entry, ULV, UV)
        chance2 = func.getBoostedChance(entry, LV, UV)
        chance1.assertEquals(chance2)
        helper.succeed()
    }

    @JvmStatic
    @GameTest(template = "empty")
    fun `NONE Chance Boost -- +1000 boost`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val assertEquals: Int.(Int) -> (Unit) = { asserter.assertEquals(it, this) }

        val func = ChanceBoostFunction.NONE
        val base = 5000
        val boost = 1000
        val max = ChanceLogic.getMaxChancedValue()
        val entry = Content(null, base, max, boost, null, null)
        // Chances should not be boosted by this function
        func.getBoostedChance(entry, HV, HV).assertEquals(base)
        func.getBoostedChance(entry, ULV, ZPM).assertEquals(base)
        func.getBoostedChance(entry, IV, UV).assertEquals(base)

        helper.succeed()
    }
}
