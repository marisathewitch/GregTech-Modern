package com.gregtechceu.gtceu.api.recipe.chance.logic

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues.*
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction
import com.gregtechceu.gtceu.api.recipe.content.Content
import com.gregtechceu.gtceu.gametest.utils.Asserter

import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraftforge.gametest.GameTestHolder
import net.minecraftforge.gametest.PrefixGameTestTemplate

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
object ChanceLogicTests {
    private const val DELAY = 40L

    @JvmStatic
    @GameTest(template = "empty")
    fun `Test Chance Boost Function`(helper: GameTestHelper) {
        val asserter = Asserter(helper)
        val oc = ChanceBoostFunction.OVERCLOCK
        val assertEquals: Int.(Int) -> (Unit) = { asserter.assertEquals(it, this) }

        val max = ChanceLogic.getMaxChancedValue()
        val entry = Content(null, 5000, max, 0, null, null)
        oc.getBoostedChance(entry, LV, LV).assertEquals(5000)
        oc.getBoostedChance(entry, LV, EV).assertEquals(5000)
        oc.getBoostedChance(entry, LV, UV).assertEquals(5000)

        entry.tierChanceBoost = 1000
        oc.getBoostedChance(entry, HV, HV).assertEquals(5000)
        oc.getBoostedChance(entry, ULV, LV).assertEquals(5000)
        oc.getBoostedChance(entry, ULV, HV).assertEquals(5000 + (1000 * 2))
        oc.getBoostedChance(entry, MV, IV).assertEquals(5000 + (1000 * 3))
        oc.getBoostedChance(entry, LV, LuV).assertEquals(5000 + (1000 * 5))
        oc.getBoostedChance(entry, LV, UV).assertEquals(max)

        entry.tierChanceBoost = -1000
        oc.getBoostedChance(entry, HV, HV).assertEquals(5000)
        oc.getBoostedChance(entry, ULV, LV).assertEquals(5000)
        oc.getBoostedChance(entry, ULV, HV).assertEquals(5000 - (1000 * 2))
        oc.getBoostedChance(entry, MV, IV).assertEquals(5000 - (1000 * 3))
        oc.getBoostedChance(entry, LV, LuV).assertEquals(5000 - (1000 * 5))
        oc.getBoostedChance(entry, LV, UV).assertEquals(0)

        entry.maxChance = 7500
        entry.tierChanceBoost = 1000
        oc.getBoostedChance(entry, HV, HV).assertEquals(5000)
        oc.getBoostedChance(entry, ULV, LV).assertEquals(5000)
        oc.getBoostedChance(entry, LV, HV).assertEquals(7000)
        oc.getBoostedChance(entry, LV, EV).assertEquals(7500)
        oc.getBoostedChance(entry, LV, ZPM).assertEquals(7500)

        val none = ChanceBoostFunction.NONE
        none.getBoostedChance(entry, HV, HV).assertEquals(5000)
        none.getBoostedChance(entry, ULV, ZPM).assertEquals(5000)
        none.getBoostedChance(entry, IV, UV).assertEquals(5000)

        helper.succeed()
    }
}
