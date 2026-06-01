package com.hp.jei_gateways.jei;

import com.hp.jei_gateways.JeiGateways;
import com.hp.jei_gateways.gateway.GatewayLootRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class GatewayLootCategory implements IRecipeCategory<GatewayLootRecipe> {
    public static final RecipeType<GatewayLootRecipe> TYPE = RecipeType.create(JeiGateways.MODID, "gateway_loot", GatewayLootRecipe.class);
    private static final int WIDTH = 180;
    private static final int HEIGHT = 138;
    private static final int SLOT_SIZE = 18;
    private static final int PEARL_X = 8;
    private static final int PEARL_Y = 8;
    private static final int CONTENT_X = 8;
    private static final int CONTENT_Y = 44;
    private static final int CONTENT_WIDTH = 160;
    private static final int CONTENT_HEIGHT = 82;

    private final IDrawableStatic background;
    private final IDrawable icon;

    public GatewayLootCategory(IGuiHelper guiHelper, ItemStack iconStack) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
    }

    @Override
    public RecipeType<GatewayLootRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.jei_gateways.gateway_loot");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GatewayLootRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, PEARL_X, PEARL_Y)
                .addItemStack(recipe.pearl())
                .setSlotName("pearl")
                .addTooltipCallback((slot, tooltip) -> addPearlTooltip(recipe, tooltip));

        for (ItemStack output : recipe.outputs()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, CONTENT_X, CONTENT_Y)
                    .setStandardSlotBackground()
                    .addItemStack(output);
        }
    }

    @Override
    public void draw(GatewayLootRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        drawSlotFrame(guiGraphics, PEARL_X, PEARL_Y);
        GatewayEntityCategory.drawPanel(guiGraphics, CONTENT_X, CONTENT_Y, CONTENT_WIDTH, CONTENT_HEIGHT);
        guiGraphics.drawString(font, Component.translatable("jei.jei_gateways.name", recipe.pearl().getHoverName()), 30, 10, 0xFF1F1F1F, false);
        guiGraphics.drawString(font, Component.translatable("jei.jei_gateways.loot_total", recipe.totalOutputCount()), 30, 24, 0xFF2A2A2A, false);
    }

    @Override
    public void createRecipeExtras(mezz.jei.api.gui.widgets.IRecipeExtrasBuilder builder, GatewayLootRecipe recipe, IFocusGroup focuses) {
        List<IRecipeSlotDrawable> outputSlots = builder.getRecipeSlots().getSlots().stream()
                .filter(slot -> !"pearl".equals(slot.getSlotName().orElse("")))
                .toList();
        GatewayLootScrollWidget widget = new GatewayLootScrollWidget(recipe, CONTENT_X, CONTENT_Y, CONTENT_WIDTH, CONTENT_HEIGHT, outputSlots);
        builder.addSlottedWidget(widget, outputSlots);
        builder.addInputHandler(widget);
    }

    @Override
    public ResourceLocation getRegistryName(GatewayLootRecipe recipe) {
        return ResourceLocation.fromNamespaceAndPath(
                JeiGateways.MODID,
                "loot/" + recipe.gatewayId().getNamespace() + "/" + recipe.gatewayId().getPath() + "/" + recipe.pageIndex()
        );
    }

    private static void addPearlTooltip(GatewayLootRecipe recipe, List<Component> tooltip) {
        tooltip.add(Component.translatable("jei.jei_gateways.name", recipe.pearl().getHoverName()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("jei.jei_gateways.loot_total", recipe.totalOutputCount()).withStyle(ChatFormatting.GRAY));
        if (recipe.pearlTooltipText() != null) {
            tooltip.add(recipe.pearlTooltipText().copy().withStyle(ChatFormatting.GRAY));
        }
    }

    private static void drawSlotFrame(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF6F6F6F);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFFCACACA);
        guiGraphics.fill(x + 1, y + 1, x + SLOT_SIZE - 2, y + 2, 0xFFE7E7E7);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + SLOT_SIZE - 2, 0xFFE7E7E7);
        guiGraphics.fill(x + 2, y + SLOT_SIZE - 2, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xFF8B8B8B);
        guiGraphics.fill(x + SLOT_SIZE - 2, y + 2, x + SLOT_SIZE - 1, y + SLOT_SIZE - 2, 0xFF8B8B8B);
    }
}
