package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.api.item.ComponentItem;

import net.minecraft.world.item.Item;

public interface IItemComponent {

    default void onAttached(Item item) {}
}
