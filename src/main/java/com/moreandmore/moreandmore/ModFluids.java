package com.moreandmore.moreandmore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class ModFluids {
    // 注册表
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, MoreAndMore.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, MoreAndMore.MODID);

    // 使用原版水的纹理
    private static final ResourceLocation WATER_STILL = new ResourceLocation("block/water_still");
    private static final ResourceLocation WATER_FLOW = new ResourceLocation("block/water_flow");
    private static final int TINT_COLOR = 0x7AD9FF; // 浅蓝色调

    // 流体类型定义
    public static final RegistryObject<FluidType> HOT_SPRING_TYPE = FLUID_TYPES.register(
            "hot_spring_fluid_type",
            () -> new FluidType(FluidType.Properties.create()
                    .density(1000)
                    .viscosity(1000)
                    .temperature(350)
                    .lightLevel(0)
                    .canHydrate(true)
                    .canSwim(true)
                    .canExtinguish(true)
                    .canConvertToSource(true)
                    .supportsBoating(true)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
            ) {
                @Override
                @OnlyIn(Dist.CLIENT)
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() { return WATER_STILL; }
                        @Override
                        public ResourceLocation getFlowingTexture() { return WATER_FLOW; }
                        @Override
                        public int getTintColor() { return TINT_COLOR; }
                    });
                }

                public void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
                    if (!level.isClientSide) return;

                    double x = pos.getX() + random.nextDouble();
                    double z = pos.getZ() + random.nextDouble();

                    // 统一概率控制：每5tick生成一次粒子（20%概率）
                    if (level.getGameTime() % 5 != 0) return;

                    if (random.nextFloat() < 0.3f) { // 30%概率生成泡泡
                        level.addParticle(ParticleTypes.BUBBLE, x, pos.getY() + 0.1, z, 0, 0.05, 0);
                    }

                    if (random.nextFloat() < 0.1f && level.isEmptyBlock(pos.above())) { // 10%概率生成村民粒子
                        level.addParticle(ParticleTypes.HAPPY_VILLAGER, x, pos.getY() + 0.9, z, 0, 0.02, 0);
                    }

                    if (level.dimensionType().ultraWarm() && random.nextInt(20) == 0) { // 5%概率生成云
                        level.addParticle(ParticleTypes.CLOUD, x, pos.getY() + 0.5, z, 0, 0.1, 0);
                    }
                }


                // 正确的方法名 - onEntityInside
                public void onEntityInside(FluidState state, Level level, BlockPos pos, Entity entity) {
                    if (!level.isClientSide && entity instanceof LivingEntity livingEntity) {
                        livingEntity.addEffect(new MobEffectInstance(
                                MobEffects.REGENERATION,
                                100,
                                0,
                                false,
                                true
                        ));
                    }
                }
            });

    // 流体本体 - 修正后的注册
    public static final RegistryObject<FlowingFluid> SOURCE_HOT_SPRING = FLUIDS.register(
            "hot_spring_fluid",
            () -> new ForgeFlowingFluid.Source(createHotSpringProperties()));

    public static final RegistryObject<FlowingFluid> FLOWING_HOT_SPRING = FLUIDS.register(
            "flowing_hot_spring",
            () -> new ForgeFlowingFluid.Flowing(createHotSpringProperties()));

    // 流体属性配置
    private static ForgeFlowingFluid.Properties createHotSpringProperties() {
        return new ForgeFlowingFluid.Properties(
                HOT_SPRING_TYPE,
                SOURCE_HOT_SPRING,
                FLOWING_HOT_SPRING
        )
                .bucket(() -> ModItems.HOT_SPRING_BUCKET.get())
                .block(() -> ModBlocks.HOT_SPRING_BLOCK.get())
                .tickRate(5)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .explosionResistance(100.0F);
    }

    // 注册方法
    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}
