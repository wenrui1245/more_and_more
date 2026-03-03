package com.moreandmore.moreandmore;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = MoreAndMore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModBlocks {
    private static final int REGEN_DURATION = 200;
    private static final int REGEN_AMPLIFIER = 1;

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MoreAndMore.MODID);


    public static final RegistryObject<LiquidBlock> HOT_SPRING_BLOCK = BLOCKS.register(
            "hot_spring_block",
            () -> new LiquidBlock(
                    ModFluids.SOURCE_HOT_SPRING,
                    BlockBehaviour.Properties.copy(Blocks.WATER) // 直接复制水的属性
                            .lightLevel(state -> 0) // 移除发光
            ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (level.isClientSide || !(entity instanceof Player player)) return;

        boolean isInHotSpring = checkHotSpringImmersion(entity);
        handleRegenerationEffect(player, isInHotSpring);
    }

    private static boolean checkHotSpringImmersion(LivingEntity entity) {
        FluidType hotSpringType = ModFluids.HOT_SPRING_TYPE.get();
        BlockPos pos = entity.blockPosition();
        BlockState state = entity.level().getBlockState(pos);

        return entity.isInFluidType(ModFluids.HOT_SPRING_TYPE.get()) &&
                entity.getFluidTypeHeight(ModFluids.HOT_SPRING_TYPE.get()) > 0.5;
    }

    private static void handleRegenerationEffect(Player player, boolean isInHotSpring) {
        MobEffectInstance currentEffect = player.getEffect(MobEffects.REGENERATION);

        if (isInHotSpring && currentEffect == null) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION,
                    REGEN_DURATION,
                    REGEN_AMPLIFIER,
                    false, true));
        } else if (!isInHotSpring && currentEffect != null &&
                currentEffect.getAmplifier() == REGEN_AMPLIFIER) {
            player.removeEffect(MobEffects.REGENERATION);
        }
    }
}