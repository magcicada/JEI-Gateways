package com.hp.jei_gateways.gateway;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record GatewayEntityRecipe(
        ResourceLocation gatewayId,
        ItemStack pearl,
        Component pearlTooltipText,
        int waveLevel,
        int waveCount,
        int entityCount,
        List<LinkedEntity> waveEntities,
        List<Component> waveModifiers,
        List<ItemStack> rewardItems,
        List<ItemStack> relatedItems
) {
    public List<ItemStack> spawnEggs() {
        return waveEntities.stream()
                .map(LinkedEntity::spawnEgg)
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
    }

    public record LinkedEntity(
            EntityType<?> entityType,
            ItemStack spawnEgg,
            int count,
            List<Component> modifiers
    ) {
        public Component displayName() {
            if (!spawnEgg.isEmpty()) {
                return spawnEgg.getHoverName();
            }
            return Component.translatable(entityType.getDescriptionId());
        }
    }
}
