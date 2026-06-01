package com.hp.jei_gateways.gateway;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record GatewayLootRecipe(
        ResourceLocation gatewayId,
        ItemStack pearl,
        Component pearlTooltipText,
        List<ItemStack> outputs,
        int pageIndex,
        int pageCount,
        int totalOutputCount
) {
    public boolean matchesPearl(ItemStack stack) {
        return !stack.isEmpty() && ItemStack.isSameItemSameTags(pearl, stack);
    }

    public boolean matchesOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (ItemStack output : outputs) {
            if (ItemStack.isSameItemSameTags(output, stack)) {
                return true;
            }
        }
        return false;
    }
}
