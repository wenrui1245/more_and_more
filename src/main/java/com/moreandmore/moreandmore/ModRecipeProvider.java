package com.moreandmore.moreandmore;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }


    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SHATTERED_SWORD.get())
                .pattern("BNB")
                .pattern("NSN")
                .pattern("BNB")
                .define('N', Items.NETHER_STAR)       // 下界之星
                .define('S', Items.NETHERITE_SWORD)   // 下界合金剑
                .define('B', Items.NETHERITE_BLOCK)   // 下界合金块
                .save(consumer, new ResourceLocation(MoreAndMore.MODID, "shattered_sword_crafting"));
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(Items.WATER_BUCKET), // 原料：水桶
                        RecipeCategory.MISC,
                        ModItems.HOT_SPRING_BUCKET.get(),   // 产物：温泉桶
                        0.35f,                             // 经验
                        200                                // 烧制时间 (200 ticks = 10秒)
                ).unlockedBy("has_water_bucket", has(Items.WATER_BUCKET))
                .save(consumer, new ResourceLocation(MoreAndMore.MODID, "hot_spring_bucket_smelting"));
    }
}
