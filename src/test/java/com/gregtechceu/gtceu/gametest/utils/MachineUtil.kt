@file:Suppress("ktlint:standard:filename")

package com.gregtechceu.gtceu.gametest.utils

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine
import com.gregtechceu.gtceu.common.machine.multiblock.part.DualHatchPartMachine

import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper

private val CONTROLLER_POS_DEFAULT = BlockPos(1, 2, 0)

data class MultiblockData(
    val controller: WorkableMultiblockMachine,
    val input: DualHatchPartMachine,
    val output: DualHatchPartMachine,
)

fun getMultiblock(helper: GameTestHelper, pos: BlockPos): MultiblockData? {
    val controller = helper.getBlockEntity(pos)
    val input = helper.getBlockEntity(pos.offset(1, 0, 0))
    val output = helper.getBlockEntity(pos.offset(-1, 0, 0))
    if (controller !is MetaMachineBlockEntity ||
        input !is MetaMachineBlockEntity ||
        output !is MetaMachineBlockEntity
    ) {
        helper.fail("not standard layout", pos)
        return null
    }
    val controllerMM = controller.metaMachine
    if (controllerMM !is WorkableMultiblockMachine) {
        helper.fail("not multiblock machine", pos)
        return null
    }
    return MultiblockData(
        controllerMM,
        input.metaMachine as DualHatchPartMachine,
        output.metaMachine as DualHatchPartMachine,
    )
}

fun getMultiblock(helper: GameTestHelper): MultiblockData? = getMultiblock(helper, CONTROLLER_POS_DEFAULT)
