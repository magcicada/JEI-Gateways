package com.hp.jei_gateways.jei;

import com.hp.jei_gateways.JeiGateways;
import com.hp.jei_gateways.gateway.GatewayEntityRecipe;
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

/**
 * JEI配方界面类别：展示Gateway实体珍珠的配方
 * 显示每个波次包含的刷怪蛋和相关信息
 */
public class GatewayEntityCategory implements IRecipeCategory<GatewayEntityRecipe> {
    // 配方界面整体尺寸
    public static final RecipeType<GatewayEntityRecipe> TYPE = RecipeType.create(JeiGateways.MODID, "gateway_entities", GatewayEntityRecipe.class);
    private static final int WIDTH = 210;                    // 界面宽度
    private static final int HEIGHT = 156;                   // 界面高度

    // 顶部信息框的位置和尺寸（显示珍珠名称）
    private static final int HEADER_BOX_X = 6;               // 信息框X坐标
    private static final int HEADER_BOX_Y = 14;              // 信息框Y坐标
    private static final int HEADER_BOX_WIDTH = 198;         // 信息框宽度
    private static final int HEADER_BOX_HEIGHT = 34;         // 信息框高度

    // 珍珠物品槽的位置（相对于信息框左上角）
    private static final int HEADER_TEXT_X = 34;             // 珍珠名称文字的X偏移
    private static final int HEADER_NAME_Y = 8;              // 珍珠名称文字的Y偏移
    private static final int HEADER_RECIPE_Y = 22;            // 配方波次文字的Y偏移
    private static final int HEADER_SLOT_X = 10;             // 珍珠槽的X偏移
    private static final int HEADER_SLOT_Y = 8;              // 珍珠槽的Y偏移

    // 滚动内容区域的位置和尺寸（显示刷怪蛋和波次修饰符）
    private static final int CONTENT_X = 6;                  // 滚动区域X坐标
    private static final int CONTENT_Y = 52;                  // 滚动区域Y坐标
    private static final int CONTENT_WIDTH = 198;             // 滚动区域宽度
    private static final int CONTENT_HEIGHT = 100;            // 滚动区域高度

    // 刷怪蛋网格的起始位置（相对于滚动区域的左上角）
    private static final int EGG_GRID_X = 10;                // 刷怪蛋网格起始X坐标
    private static final int EGG_GRID_Y = 38;                // 刷怪蛋网格起始Y坐标
    private static final int EGG_GRID_COLUMNS = 7;           // 每行显示的刷怪蛋数量
    private static final int SLOT_SPACING = 18;              // 刷怪蛋槽之间的间距

    // 滚动条相关
    private static final int SCROLLBAR_EXTRA_WIDTH = 16;      // 滚动条占用的额外宽度

    // 绘制的背景和图标
    private final IDrawableStatic background;
    private final IDrawable icon;

    public GatewayEntityCategory(IGuiHelper guiHelper, ItemStack iconStack) {
        // 创建空白的可绘制背景
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        // 使用珍珠物品作为类别图标
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
    }

    @Override
    public RecipeType<GatewayEntityRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        // 从语言文件中获取配方类别的标题
        return Component.translatable("jei.jei_gateways.gateway_entities");
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
    public void setRecipe(IRecipeLayoutBuilder builder, GatewayEntityRecipe recipe, IFocusGroup focuses) {
        // 添加珍珠物品槽（只用于显示）
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, HEADER_BOX_X + HEADER_SLOT_X, HEADER_BOX_Y + HEADER_SLOT_Y)
                .addItemStack(recipe.pearl())
                .setStandardSlotBackground()
                .setSlotName("pearl")
                .addTooltipCallback((slot, tooltip) -> addPearlTooltip(recipe, tooltip));

        // 遍历该波次的所有实体，添加刷怪蛋槽
        int eggIndex = 0;
        for (GatewayEntityRecipe.LinkedEntity entity : recipe.waveEntities()) {
            // 计算刷怪蛋在网格中的位置（行列布局）
            int x = EGG_GRID_X + (eggIndex % EGG_GRID_COLUMNS) * SLOT_SPACING;
            int y = EGG_GRID_Y + (eggIndex / EGG_GRID_COLUMNS) * SLOT_SPACING;
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, y)
                    .addItemStack(entity.spawnEgg())
                    .setStandardSlotBackground()
                    .addTooltipCallback((slot, tooltip) -> addWaveEntityTooltip(entity, tooltip));
            eggIndex++;
        }

        // 添加隐形物品槽用于JEI的搜索和配方查找功能
        // 珍珠作为输入和输出
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(recipe.pearl());
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(recipe.pearl());

        // 所有刷怪蛋作为输入和输出
        List<ItemStack> spawnEggs = recipe.spawnEggs();
        if (!spawnEggs.isEmpty()) {
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(spawnEggs);
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStacks(spawnEggs);
        }
        // 相关物品作为输入和输出
        if (!recipe.relatedItems().isEmpty()) {
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(recipe.relatedItems());
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStacks(recipe.relatedItems());
        }
    }

    @Override
    public void draw(GatewayEntityRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // 绘制固定的顶部信息框
        drawFixedHeader(recipe, guiGraphics, HEADER_BOX_X, HEADER_BOX_Y, HEADER_BOX_WIDTH);
        // 绘制滚动内容区域的背景面板
        drawPanel(guiGraphics, CONTENT_X, CONTENT_Y, CONTENT_WIDTH, CONTENT_HEIGHT);
    }

    @Override
    public void createRecipeExtras(mezz.jei.api.gui.widgets.IRecipeExtrasBuilder builder, GatewayEntityRecipe recipe, IFocusGroup focuses) {
        // 获取所有刷怪蛋槽（排除珍珠槽）
        List<IRecipeSlotDrawable> eggSlots = builder.getRecipeSlots().getSlots().stream()
                .filter(slot -> !"pearl".equals(slot.getSlotName().orElse("")))
                .toList();
        // 创建滚动widget来管理刷怪蛋的显示和交互
        GatewayEntityWaveScrollWidget widget = new GatewayEntityWaveScrollWidget(recipe, CONTENT_X, CONTENT_Y, CONTENT_WIDTH, CONTENT_HEIGHT, eggSlots);
        builder.addSlottedWidget(widget, eggSlots);
        builder.addInputHandler(widget);
    }

    @Override
    public ResourceLocation getRegistryName(GatewayEntityRecipe recipe) {
        // 生成配方的注册名称（用于JEI内部识别）
        return ResourceLocation.fromNamespaceAndPath(JeiGateways.MODID, recipe.gatewayId().getNamespace() + "/" + recipe.gatewayId().getPath() + "/wave_" + recipe.waveLevel());
    }

    /**
     * 绘制固定的顶部信息框（包含珍珠名称）
     */
    private static void drawFixedHeader(GatewayEntityRecipe recipe, GuiGraphics guiGraphics, int x, int y, int width) {
        Font font = Minecraft.getInstance().font;
        // 绘制面板背景
        drawPanel(guiGraphics, x, y, width, HEADER_BOX_HEIGHT);
        // 绘制珍珠名称
        guiGraphics.drawString(font, Component.translatable("jei.jei_gateways.name", recipe.pearl().getHoverName()), x + HEADER_TEXT_X, y + HEADER_NAME_Y, 0xFF1F1F1F, false);
    }

    /**
     * 添加珍珠槽的提示信息
     */
    private static void addPearlTooltip(GatewayEntityRecipe recipe, List<Component> tooltip) {
        tooltip.add(Component.translatable("jei.jei_gateways.name", recipe.pearl().getHoverName()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("jei.jei_gateways.wave_level", recipe.waveLevel(), recipe.waveCount()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("jei.jei_gateways.entity_count", recipe.entityCount()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("jei.jei_gateways.has_recipe_pages", Component.translatable(JeiGatewaysPlugin.hasOtherRecipePages(recipe.pearl()) ? "jei.jei_gateways.yes" : "jei.jei_gateways.no")).withStyle(ChatFormatting.GRAY));
    }

    /**
     * 添加刷怪蛋槽的提示信息（实体名称、数量、修饰符）
     */
    private static void addWaveEntityTooltip(GatewayEntityRecipe.LinkedEntity entity, List<Component> tooltip) {
        tooltip.add(entity.displayName().copy().withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("jei.jei_gateways.entity_stack_count", entity.count()).withStyle(ChatFormatting.GRAY));
        for (Component modifier : entity.modifiers()) {
            tooltip.add(modifier.copy().withStyle(ChatFormatting.GRAY));
        }
    }

    /**
     * 绘制滚动区域内的文本内容（波次等级、刷怪蛋标签、波次修饰符）
     */
    static void drawScrollableContents(GatewayEntityRecipe recipe, GuiGraphics guiGraphics, int x, int y, int width, int modifierStartY) {
        Font font = Minecraft.getInstance().font;
        // 绘制"第X波/共Y波"
        guiGraphics.drawString(font, Component.translatable("jei.jei_gateways.wave_level", recipe.waveLevel(), recipe.waveCount()), x + 4, y + 6, 0xFF1F1F1F, false);
        // 绘制"此波次拥有的实体"
        guiGraphics.drawString(font, Component.translatable("jei.jei_gateways.wave_entities"), x + 4, y + 20, 0xFF2A2A2A, false);
        // 绘制"波次修饰符"
        guiGraphics.drawString(font, Component.translatable("jei.jei_gateways.wave_modifiers"), x + 4, y + modifierStartY, 0xFF2A2A2A, false);

        // 绘制修饰符列表
        int lineY = y + modifierStartY + 12;
        if (recipe.waveModifiers().isEmpty()) {
            guiGraphics.drawString(font, Component.translatable("jei.jei_gateways.no_wave_modifiers"), x + 4, lineY, 0xFF666666, false);
            return;
        }
        for (Component modifier : recipe.waveModifiers()) {
            guiGraphics.drawString(font, modifier, x + 4, lineY, 0xFF1F1F1F, false);
            lineY += 10;  // 每行下移10像素
        }
    }

    /**
     * 计算滚动内容的总高度（用于确定滚动条大小）
     */
    static int getContentHeight(GatewayEntityRecipe recipe) {
        // 计算刷怪蛋占据的行数
        int rows = (recipe.waveEntities().size() + EGG_GRID_COLUMNS - 1) / EGG_GRID_COLUMNS;
        // 计算修饰符行数（至少1行）
        int modifierLines = Math.max(1, recipe.waveModifiers().size());
        // 总高度 = 修饰符起始Y + 修饰符区域高度 + 底部留白
        return getModifierStartY(recipe) + 12 + modifierLines * 10 + 8;
    }

    /**
     * 计算修饰符区域的起始Y坐标
     */
    static int getModifierStartY(GatewayEntityRecipe recipe) {
        int rows = (recipe.waveEntities().size() + EGG_GRID_COLUMNS - 1) / EGG_GRID_COLUMNS;
        return EGG_GRID_Y + rows * SLOT_SPACING + 4;
    }

    // 以下是getter方法，供GatewayEntityWaveScrollWidget使用

    static int getEggGridX() {
        return EGG_GRID_X;
    }

    static int getEggGridY() {
        return EGG_GRID_Y;
    }

    static int getEggGridColumns() {
        return EGG_GRID_COLUMNS;
    }

    static int getSlotSpacing() {
        return SLOT_SPACING;
    }

    static int getContentWidth() {
        return CONTENT_WIDTH;
    }

    /**
     * 绘制带边框的面板（用于分隔不同区域）
     */
    static void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // 填充主体颜色
        guiGraphics.fill(x, y, x + width, y + height, 0xFFE3E3E3);
        // 绘制顶部和左边的高光边框（浅色）
        guiGraphics.fill(x, y, x + width, y + 1, 0xFFF8F8F8);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFFF8F8F8);
        // 绘制底部和右边的阴影边框（深色）
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF8A8A8A);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF8A8A8A);
    }
}
