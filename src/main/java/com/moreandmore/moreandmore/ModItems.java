package com.moreandmore.moreandmore;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
        public static final DeferredRegister<Item> ITEMS =
                DeferredRegister.create(ForgeRegistries.ITEMS, MoreAndMore.MODID);

        // 温泉桶注册（无需tab()方法）
        public static final RegistryObject<Item> HOT_SPRING_BUCKET = ITEMS.register(
                "hot_spring_bucket",
                () -> new BucketItem(
                        ModFluids.SOURCE_HOT_SPRING, // 关联流体
                        new Item.Properties()
                                .craftRemainder(Items.BUCKET) // 使用后返还空桶
                                .stacksTo(1)                  // 只能堆叠1个
                                .rarity(Rarity.UNCOMMON)
                )
        );
        public static final RegistryObject<Item> SHATTERED_SWORD = ITEMS.register("shattered_sword",
                ShatteredSwordItem::new
        );

        public static void register(IEventBus eventBus) {
                ITEMS.register(eventBus);
        }
}