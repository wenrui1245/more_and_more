package com.moreandmore.moreandmore;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MoreAndMore.MODID);
    public static final RegistryObject<Enchantment> LAST_STAND = ENCHANTMENTS.register("last_stand", LastStandEnchantment::new);
    public static final RegistryObject<Enchantment> EXPLOSIVE_ARROW =
            ENCHANTMENTS.register("explosive_arrow",
                    ExplosiveArrowEnchantment::new);
    public static final RegistryObject<Enchantment> FROST_TOUCH = ENCHANTMENTS.register(
            "frost_touch",
            FrostTouchEnchantment::new);
    public static final RegistryObject<Enchantment> SILENCE = ENCHANTMENTS.register(
            "silence",
            SilenceEnchantment::new);
    public static final RegistryObject<Enchantment> ROBBERY = ENCHANTMENTS.register(
            "robbery",
            RobberyEnchantment::new
    );

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus); // 确保注册
    }
}



