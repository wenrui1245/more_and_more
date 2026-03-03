package com.moreandmore.moreandmore;

import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod.EventBusSubscriber
public class SilenceEnchantment extends Enchantment {
    private static final Logger LOGGER = LogUtils.getLogger();

    public SilenceEnchantment() {
        super(Rarity.UNCOMMON,
                EnchantmentCategory.WEAPON,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity attacker)) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty() || !weapon.isEnchanted()) {
            return;
        }

        int silenceLevel = weapon.getEnchantmentLevel(ModEnchantments.SILENCE.get());
        if (silenceLevel <= 0) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }

        LOGGER.debug("Silence enchantment triggered with level {}", silenceLevel);

        // 对目标施加虚弱效果
        target.addEffect(new MobEffectInstance(
                MobEffects.WEAKNESS,
                60,    // 3秒
                254,   // 255级虚弱（0-254对应1-255级）
                false, // 不显示粒子
                false, // 不显示图标
                true   // 可被信标清除
        ));

        // 攻击者获得力量效果
        attacker.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_BOOST,
                200,   // 10秒
                2,    // 力量1
                false,
                false
        ));

        // 粒子效果
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5,
                    target.getZ(),
                    15,
                    target.getBbWidth() * 0.5,
                    target.getBbHeight() * 0.25,
                    target.getBbWidth() * 0.5,
                    0.05
            );
        }
    }
}
