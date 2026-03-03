package com.moreandmore.moreandmore;

import com.mojang.logging.LogUtils;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MoreAndMore.MODID)
public class MoreAndMore {
    public static final String MODID = "moreandmore";
    private static final Logger LOGGER = LogUtils.getLogger();


public MoreAndMore() {
        LOGGER.info("Initializing MoreAndMore Mod...");

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册顺序优化
        ModEnchantments.register(bus);
        ModFluids.register(bus);
        ModBlocks.register(bus);
        ModItems.register(bus);
        ModCreativeModeTabs.register(bus);

        // 事件监听
        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);

        // 配置注册（如果有）
        // ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("MoreAndMore Mod initialized successfully!");
    }
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class DataGen {
        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            var generator = event.getGenerator();
            var existingFileHelper = event.getExistingFileHelper();

            // 添加配方生成器
            generator.addProvider(event.includeServer(), new ModRecipeProvider(generator.getPackOutput()));
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 后期初始化代码
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // 客户端专用注册
    }
}

