package com.moreandmore.moreandmore;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MoreAndMore.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register(
            "main_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group." + MoreAndMore.MODID + ".main"))
                    .icon(() -> new ItemStack(ModItems.HOT_SPRING_BUCKET.get()))
                    .displayItems((params, output) -> {
                        // 添加所有需要显示的物品
                        output.accept(ModItems.HOT_SPRING_BUCKET.get());
                        output.accept(ModItems.SHATTERED_SWORD.get());

                        // 添加所有已注册的附魔书
                        ModEnchantments.ENCHANTMENTS.getEntries().forEach(enchantment -> {
                            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                            EnchantmentHelper.setEnchantments(
                                    Map.of(enchantment.get(), enchantment.get().getMaxLevel()),
                                    enchantedBook
                            );
                            output.accept(enchantedBook);
                        });
                    })
                    .build()
    );

    // 确保在主类中被调用
    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

    // 新增方法：主动获取创造标签（解决"未使用"警告）
    public static CreativeModeTab getMainTab() {
        return MAIN_TAB.get();
    }
}
