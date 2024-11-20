package com.gregtechceu.gtceu.gametest.api.machine.trait

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic
import com.gregtechceu.gtceu.common.data.GTMaterials.Acetone
import com.gregtechceu.gtceu.common.data.GTMaterials.Water
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder
import com.gregtechceu.gtceu.gametest.utils.getMultiblock

import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction
import net.minecraftforge.gametest.GameTestHolder
import net.minecraftforge.gametest.PrefixGameTestTemplate

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
object ParallelLogicTests {

    private const val DELAY = 35L

    @GameTest(template = "lcr", setupTicks = DELAY)
    @JvmStatic
    fun `Max Ratio by Input Item`(helper: GameTestHelper) {
        val maxLimit = 4
        val (machine, input, _) = getMultiblock(helper)!!
        input.inventory.insertItem(0, ItemStack(Blocks.COBBLESTONE, 3), false)
        input.tank.fill(Acetone.getFluid(8000), FluidAction.EXECUTE)

        // Recipe should be limited to 3 based on inputs
        val recipe1 = GTRecipeBuilder.ofRaw()
            .inputItems(ItemStack(Blocks.COBBLESTONE))
            .inputFluids(Acetone.getFluid(100))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()
        var ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe1, maxLimit)
        helper.assertTrue(ratio == 3, "wrong mult buddy A: %d".format(ratio))

        // Recipe should be limited to 0 based on inputs
        val recipe2 = GTRecipeBuilder.ofRaw()
            .inputItems(ItemStack(Blocks.DIRT))
            .inputFluids(Acetone.getFluid(100))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()
        ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe2, maxLimit)
        helper.assertTrue(ratio == 0, "wrong mult buddy B: %d".format(ratio))

        helper.succeed()
    }

    @GameTest(template = "lcr", setupTicks = DELAY)
    @JvmStatic
    fun `Max Ratio by Input Fluid`(helper: GameTestHelper) {
        val limit = 4
        val (machine, input, _) = getMultiblock(helper)!!
        input.inventory.insertItem(0, ItemStack(Blocks.COBBLESTONE, 16), false)
        input.tank.fill(Acetone.getFluid(8000), FluidAction.EXECUTE)

        // Recipe should be limited to 2 based on inputs
        val recipe1 = GTRecipeBuilder.ofRaw()
            .inputItems(ItemStack(Blocks.COBBLESTONE))
            .inputFluids(Acetone.getFluid(4000))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()
        var ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe1, limit)
        helper.assertTrue(ratio == 2, "wrong mult buddy A: %d".format(ratio))

        // Recipe should be limited to 0 based on inputs
        val recipe2 = GTRecipeBuilder.ofRaw()
            .inputItems(ItemStack(Blocks.COBBLESTONE))
            .inputFluids(Water.getFluid(4000))
            .outputItems(ItemStack(Blocks.STONE)).EUt(30)
            .duration(100)
            .buildRawRecipe()
        ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe2, limit)
        helper.assertTrue(ratio == 0, "wrong mult buddy B: %d".format(ratio))

        helper.succeed()
    }

    @GameTest(template = "lcr", setupTicks = DELAY)
    @JvmStatic
    fun `Parallel Limit by Output Item`(helper: GameTestHelper) {
        val maxLimit = 8
        val (machine, input, output) = getMultiblock(helper)!!
        input.inventory.insertItem(0, ItemStack(Blocks.COBBLESTONE, 16), false)
        input.tank.fill(Acetone.getFluid(8000), FluidAction.EXECUTE)

        // Recipe should limit to 8 since enough space exists
        val recipe = GTRecipeBuilder.ofRaw()
            .inputItems(ItemStack(Blocks.COBBLESTONE))
            .inputFluids(Acetone.getFluid(100))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()

        fun limitOutput() = ParallelLogic.limitByOutputMerging(recipe, machine, maxLimit, machine::canVoidRecipeOutputs)

        var limit = limitOutput()
        helper.assertTrue(limit == maxLimit, "wrong limit buddy A: %d".format(limit))

        // Fill output space - last two slots have Stone x62
        for (i in 0..13) output.inventory.insertItemInternal(i, ItemStack(Blocks.ICE), false)
        output.inventory.insertItemInternal(14, ItemStack(Blocks.STONE, 62), false)
        output.inventory.insertItemInternal(15, ItemStack(Blocks.STONE, 62), false)

        // Recipe should limit to 4 based on output space - across two slots
        limit = limitOutput()
        helper.assertTrue(limit == 4, "wrong limit buddy B: %d".format(limit))

        // Fill slot 14
        output.inventory.insertItemInternal(14, ItemStack(Blocks.STONE, 2), false)

        // Recipe should limit to 2 based on output space - last slot
        limit = limitOutput()
        helper.assertTrue(limit == 2, "wrong limit buddy C: %d".format(limit))

        // Fill slot 15
        output.inventory.insertItemInternal(15, ItemStack(Blocks.STONE, 2), false)

        // Recipe should limit to 0 based on output space - no space
        limit = limitOutput()
        helper.assertTrue(limit == 0, "wrong limit buddy D: %d".format(limit))

        helper.succeed()
    }

    //    @GameTest(template = "lcr", setupTicks = DELAY)
    // TODO: Fix OverlayedFluidHandler and FluidRecipeCapability#limitParallel
    @JvmStatic
    fun `Parallel Limit by Output Fluid`(helper: GameTestHelper) {
        val maxLimit = 8
        val (machine, input, output) = getMultiblock(helper)!!
        input.inventory.insertItem(0, ItemStack(Blocks.COBBLESTONE, 16), false)

        // Recipe should limit to 8 since enough space exists
        val recipe = GTRecipeBuilder.ofRaw()
            .inputItems(ItemStack(Blocks.COBBLESTONE))
            .outputFluids(Acetone.getFluid(1000))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()

        fun limitOutput() = ParallelLogic.limitByOutputMerging(recipe, machine, maxLimit, machine::canVoidRecipeOutputs)

        var limit = limitOutput()
        helper.assertTrue(limit == maxLimit, "wrong limit buddy A: %d".format(limit))

        // Fill output space
        output.tank.fillInternal(Acetone.getFluid(60000), FluidAction.EXECUTE)

        // Recipe should limit to 4 based on output space
        limit = limitOutput()
        helper.assertTrue(limit == 4, "wrong limit buddy B: %d".format(limit))

        output.tank.fillInternal(Acetone.getFluid(4000), FluidAction.EXECUTE)

        // Recipe should limit to 0 - no space
        limit = limitOutput()
        helper.assertTrue(limit == 0, "wrong limit buddy C: %d".format(limit))

        helper.succeed()
    }

    @GameTest(template = "lcr", setupTicks = DELAY)
    @JvmStatic
    fun `Max Ratio by NC Input Item`(helper: GameTestHelper) {
        val limit = 4
        val (machine, input, _) = getMultiblock(helper)!!

        // Test with NC == other ingredient
        val recipe1 = GTRecipeBuilder.ofRaw()
            .inputItems(ItemStack(Blocks.COBBLESTONE))
            .notConsumable(ItemStack(Blocks.COBBLESTONE))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()

        // 1 for NC + 0 to consume -> 0
        input.inventory.insertItem(0, ItemStack(Blocks.COBBLESTONE, 1), false)
        var ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe1, limit)
        helper.assertTrue(ratio == 0, "wrong ratio buddy A: %d".format(ratio))

        // 1 for NC + 2 to consume -> 2
        input.inventory.insertItem(0, ItemStack(Blocks.COBBLESTONE, 2), false)
        ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe1, limit)
        helper.assertTrue(ratio == 2, "wrong ratio buddy B: %d".format(ratio))

        // 1 for NC + 4 to consume -> 4
        input.inventory.insertItem(0, ItemStack(Blocks.COBBLESTONE, 2), false)
        ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe1, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy C: %d".format(ratio))

        // 1 for NC + 6 to consume -> limited at 4
        input.inventory.insertItem(0, ItemStack(Blocks.COBBLESTONE, 2), false)
        ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe1, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy D: %d".format(ratio))

        // Test with NC != other ingredient
        val recipe2 = GTRecipeBuilder.ofRaw()
            .inputItems(ItemStack(Blocks.COBBLESTONE))
            .notConsumable(ItemStack(Blocks.DIRT))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()

        // No NC -> 0
        ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe2, limit)
        helper.assertTrue(ratio == 0, "wrong ratio buddy E: %d".format(ratio))

        // NC dirt + 7 cobblestone -> limited to 4
        input.inventory.insertItem(1, ItemStack(Blocks.DIRT, 1), false)
        ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe2, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy F: %d".format(ratio))

        // Test with only NC ingredient
        val recipe3 = GTRecipeBuilder.ofRaw()
            .notConsumable(ItemStack(Blocks.COBBLESTONE))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()

        // NC cobblestone -> limited to 4
        ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe3, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy G: %d".format(ratio))

        // Test with only NC ingredient with stacksize
        val recipe4 = GTRecipeBuilder.ofRaw()
            .notConsumable(ItemStack(Blocks.DIRT, 2))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()

        // Only 1 dirt -> limited to 0
        ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe4, limit)
        helper.assertTrue(ratio == 0, "wrong ratio buddy H: %d".format(ratio))

        // Add one more dirt -> NC fulfilled -> limited to 4
        input.inventory.insertItem(1, ItemStack(Blocks.DIRT, 1), false)
        ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe4, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy I: %d".format(ratio))

        // Add more dirt -> NC fulfilled -> limited to 4
        input.inventory.insertItem(1, ItemStack(Blocks.DIRT, 3), false)
        ratio = ItemRecipeCapability.CAP.getMaxParallelRatio(machine, recipe4, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy J: %d".format(ratio))

        helper.succeed()
    }

    @GameTest(template = "lcr", setupTicks = DELAY)
    @JvmStatic
    fun `Max Ratio by NC Input Fluid`(helper: GameTestHelper) {
        val limit = 4
        val (machine, input, _) = getMultiblock(helper)!!

        // Test with NC == other ingredient
        val recipe1 = GTRecipeBuilder.ofRaw()
            .inputFluids(Water.getFluid(1000))
            .notConsumableFluid(Water.getFluid(1))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()

        // 1 mB NC + 499mB to consume -> 0
        input.tank.fill(Water.getFluid(500), FluidAction.EXECUTE)
        var ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe1, limit)
        helper.assertTrue(ratio == 0, "wrong ratio buddy A: %d".format(ratio))

        // 1mB NC + 2000mB to consume -> 2
        input.tank.fill(Water.getFluid(1501), FluidAction.EXECUTE)
        ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe1, limit)
        helper.assertTrue(ratio == 2, "wrong ratio buddy B: %d".format(ratio))

        // 1mB NC + 4000mB to consume -> 4
        input.tank.fill(Water.getFluid(2000), FluidAction.EXECUTE)
        ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe1, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy C: %d".format(ratio))

        // 1mB NC + 6000mB to consume -> limited to 4
        input.tank.fill(Water.getFluid(2000), FluidAction.EXECUTE)
        ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe1, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy D: %d".format(ratio))

        // Test with NC != other ingredient
        val recipe2 = GTRecipeBuilder.ofRaw()
            .inputFluids(Water.getFluid(1000))
            .notConsumableFluid(Acetone.getFluid(1))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()

        // No NC input
        ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe2, limit)
        helper.assertTrue(ratio == 0, "wrong ratio buddy E: %d".format(ratio))

        // 1mB NC + 6000mB to consume -> limited to 4
        input.tank.fill(Acetone.getFluid(1), FluidAction.EXECUTE)
        ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe2, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy F: %d".format(ratio))

        // Test with only NC ingredient
        val recipe3 = GTRecipeBuilder.ofRaw()
            .notConsumableFluid(Acetone.getFluid(1))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()

        // NC Acetone -> limit
        ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe3, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy G: %d".format(ratio))

        // Test with only NC ingredient with stack size
        val recipe4 = GTRecipeBuilder.ofRaw()
            .notConsumableFluid(Acetone.getFluid(10))
            .outputItems(ItemStack(Blocks.STONE))
            .EUt(30)
            .duration(100)
            .buildRawRecipe()

        // 1mB NC -> 0
        ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe4, limit)
        helper.assertTrue(ratio == 0, "wrong ratio buddy H: %d".format(ratio))

        // 10mB NC -> limit
        input.tank.fill(Acetone.getFluid(9), FluidAction.EXECUTE)
        ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe4, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy I: %d".format(ratio))

        // 20mB NC -> limit
        input.tank.fill(Acetone.getFluid(10), FluidAction.EXECUTE)
        ratio = FluidRecipeCapability.CAP.getMaxParallelRatio(machine, recipe4, limit)
        helper.assertTrue(ratio == limit, "wrong ratio buddy J: %d".format(ratio))

        helper.succeed()
    }
}
