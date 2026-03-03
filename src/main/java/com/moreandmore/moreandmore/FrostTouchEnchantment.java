package com.moreandmore.moreandmore;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.FireAspectEnchantment;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber
public class FrostTouchEnchantment extends Enchantment {
    // 免疫该效果的生物类型
    private static final EntityType<?>[] IMMUNE_ENTITIES = {

            EntityType.SNOW_GOLEM,
            EntityType.STRAY,
            EntityType.POLAR_BEAR
    };
    public FrostTouchEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 9; // 类似火焰附加的消耗
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }
    @Override
    public boolean isTreasureOnly() {
        return false; // 必须设为false才能从附魔台获取
    }

    @Override
    public boolean isTradeable() {
        return true;  // 允许村民交易
    }

    @Override
    public boolean isDiscoverable() {
        return true;  // 允许在附魔台显示
    }

    @Override
    public boolean checkCompatibility(@NotNull Enchantment other) {
        return !(other instanceof FireAspectEnchantment) && super.checkCompatibility(other);
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        Entity attacker = event.getSource().getDirectEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        ItemStack weapon = livingAttacker.getMainHandItem();
        int level = weapon.getEnchantmentLevel(ModEnchantments.FROST_TOUCH.get());
        if (level <= 0) return;

        LivingEntity target = event.getEntity();
        if (shouldImmune(target)) return;

        // 基础冻结效果
        applyFreezeEffect(target, level);

        // 对火焰生物额外伤害
        if (target.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
            target.hurt(target.damageSources().freeze(), level * 2.0f);
        }

        // 添加粒子效果
        spawnParticles(target, level);
    }

    private static boolean shouldImmune(Entity entity) {
        // 检查预设免疫列表
        for (EntityType<?> type : IMMUNE_ENTITIES) {
            if (entity.getType() == type) return true;
        }

        // 检查生物是否已有冻结抗性
        if (entity instanceof LivingEntity living) {
            return living.hasEffect(MobEffects.CONDUIT_POWER) ||
                    living.isInvertedHealAndHarm();
        }
        return false;
    }

    private static void applyFreezeEffect(LivingEntity target, int level) {
        // 冻结效果（最高不超过完全冻结所需时间）
        int freezeTicks = Math.min(
                target.getTicksRequiredToFreeze(),
                target.getTicksFrozen() + 50 * level
        );
        target.setTicksFrozen(freezeTicks);

        // 减速效果（5秒 + 2.5秒/级）
        target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                100 + level * 50,
                level - 1,  // 减速等级=附魔等级-1
                false,
                true
        ));
    }

    private static void spawnParticles(LivingEntity target, int level) {
        if (!(target.level() instanceof ServerLevel serverLevel)) return;

        // 雪花粒子
        serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5,
                target.getZ(),
                15 + level * 5,
                target.getBbWidth() * 0.5,
                target.getBbHeight() * 0.5,
                target.getBbWidth() * 0.5,
                0.1
        );

        // 对火焰生物添加蒸汽粒子
        if (target.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    target.getX(),
                    target.getY() + target.getBbHeight(),
                    target.getZ(),
                    10,
                    0.3,
                    0.5,
                    0.3,
                    0.05
            );
        }
    }
}
