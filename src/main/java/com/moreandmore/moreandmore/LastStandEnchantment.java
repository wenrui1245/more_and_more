package com.moreandmore.moreandmore;

// 必须导入以下这些“外部零件”，否则代码会报“无法解析”的错误
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// @Mod.EventBusSubscriber 告诉 Forge 这个类是用来监听游戏事件的
@Mod.EventBusSubscriber(modid = MoreAndMore.MODID)
public class LastStandEnchantment extends Enchantment {

    // 存储玩家冷却时间的名单
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    public LastStandEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    // 当生物受伤时触发
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        // 只有玩家受伤才处理
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return;

        // 检查身上是否有“屹立不倒”附魔
        if (chestplate.getEnchantmentLevel(ModEnchantments.LAST_STAND.get()) <= 0) return;

        // 冷却检查 (使用游戏 Tick)
        long currentTime = player.level().getGameTime();
        long lastUsed = cooldowns.getOrDefault(player.getUUID(), -600L);
        if (currentTime - lastUsed < 600) return; // 30秒冷却

        // 检查受到的伤害是否会导致死亡（玩家剩余血量 - 本次伤害 <= 0）
        if (player.getHealth() - event.getAmount() <= 0.5f) {

            // 检查经验值是否够 3 级
            if (player.experienceLevel < 3) {
                if (player.level().isClientSide) {
                    player.displayClientMessage(Component.translatable("message.moreandmore.no_xp").withStyle(ChatFormatting.YELLOW), true);
                }
                return;
            }

            // 核心功能：【取消伤害】。注意：Forge 里是 setCanceled (一个L)
            event.setCanceled(true);

            // 扣除 3 级经验
            player.giveExperienceLevels(-3);

            // 添加各种保命效果
            player.setHealth(2.0f); // 强行保留 1 颗心
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 3));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));

            // 播放音效和粒子
            player.playSound(SoundEvents.TOTEM_USE, 1.0F, 1.0F);
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 64, 0.5, 0.5, 0.5, 0.5);
            }

            // 记录冷却时间
            cooldowns.put(player.getUUID(), currentTime);
        }
    }
}
