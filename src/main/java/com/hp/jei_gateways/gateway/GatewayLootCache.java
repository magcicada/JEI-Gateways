package com.hp.jei_gateways.gateway;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GatewayLootCache {
    private static final Map<GatewayEntityCache.ItemStackKey, List<GatewayLootRecipe>> BY_ITEM = new HashMap<>();
    private static List<GatewayLootRecipe> allRecipes = List.of();

    private GatewayLootCache() {
    }

    public static List<GatewayLootRecipe> getRecipes() {
        ensureBuilt();
        return allRecipes;
    }

    public static List<GatewayLootRecipe> getRecipes(ItemStack stack) {
        ensureBuilt();
        if (stack.isEmpty()) {
            return List.of();
        }
        List<GatewayLootRecipe> directMatches = BY_ITEM.getOrDefault(GatewayEntityCache.ItemStackKey.ofStack(stack), List.of());
        if (directMatches.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<GatewayLootRecipe> expanded = new LinkedHashSet<>();
        for (GatewayLootRecipe directMatch : directMatches) {
            for (GatewayLootRecipe recipe : allRecipes) {
                if (recipe.gatewayId().equals(directMatch.gatewayId())
                        && ItemStack.isSameItemSameTags(recipe.pearl(), directMatch.pearl())) {
                    expanded.add(recipe);
                }
            }
        }
        return List.copyOf(expanded);
    }

    public static void clear() {
        BY_ITEM.clear();
        allRecipes = List.of();
    }

    public static void rebuild() {
        clear();
        ensureBuilt();
    }

    private static void ensureBuilt() {
        if (!allRecipes.isEmpty() || !BY_ITEM.isEmpty()) {
            return;
        }

        List<GatewayLootRecipe> built = new ArrayList<>();
        Map<GatewayEntityCache.ItemStackKey, List<GatewayLootRecipe>> byItem = new HashMap<>();
        Set<String> processedPearls = new LinkedHashSet<>();

        for (GatewayEntityRecipe entityRecipe : GatewayEntityCache.getRecipes()) {
            String pearlKey = entityRecipe.gatewayId() + "|" + GatewayEntityCache.ItemStackKey.ofStack(entityRecipe.pearl());
            if (!processedPearls.add(pearlKey)) {
                continue;
            }
            List<ItemStack> outputs = normalizeOutputs(entityRecipe.rewardItems());
            if (outputs.isEmpty()) {
                continue;
            }

            GatewayLootRecipe lootRecipe = new GatewayLootRecipe(
                    entityRecipe.gatewayId(),
                    entityRecipe.pearl().copy(),
                    entityRecipe.pearlTooltipText() == null ? null : entityRecipe.pearlTooltipText().copy(),
                    List.copyOf(outputs),
                    0,
                    1,
                    outputs.size()
            );
            built.add(lootRecipe);
            indexRecipe(byItem, lootRecipe);
        }

        built.sort(Comparator.comparing((GatewayLootRecipe recipe) -> recipe.pearl().getHoverName().getString(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(recipe -> recipe.gatewayId().toString()));
        byItem.replaceAll((key, value) -> value.stream()
                .distinct()
                .sorted(Comparator.comparing((GatewayLootRecipe recipe) -> recipe.gatewayId().toString()))
                .toList());
        BY_ITEM.putAll(byItem);
        allRecipes = List.copyOf(built);
    }

    private static List<ItemStack> normalizeOutputs(List<ItemStack> outputs) {
        LinkedHashSet<GatewayEntityCache.ItemStackKey> unique = new LinkedHashSet<>();
        for (ItemStack output : outputs) {
            if (!output.isEmpty()) {
                unique.add(GatewayEntityCache.ItemStackKey.ofStack(output));
            }
        }
        return unique.stream()
                .map(GatewayEntityCache.ItemStackKey::toStack)
                .filter(stack -> !stack.isEmpty())
                .sorted(Comparator.comparing((ItemStack stack) -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(stack -> stack.getHoverName().getString(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static void indexRecipe(Map<GatewayEntityCache.ItemStackKey, List<GatewayLootRecipe>> byItem, GatewayLootRecipe recipe) {
        indexItem(byItem, recipe.pearl(), recipe);
        for (ItemStack output : recipe.outputs()) {
            indexItem(byItem, output, recipe);
        }
    }

    private static void indexItem(Map<GatewayEntityCache.ItemStackKey, List<GatewayLootRecipe>> byItem, ItemStack stack, GatewayLootRecipe recipe) {
        if (stack.isEmpty()) {
            return;
        }
        byItem.computeIfAbsent(GatewayEntityCache.ItemStackKey.ofStack(stack), ignored -> new ArrayList<>()).add(recipe);
    }
}
