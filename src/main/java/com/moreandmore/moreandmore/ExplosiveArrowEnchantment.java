package com.moreandmore.moreandmore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Mod.EventBusSubscriber
public class ExplosiveArrowEnchantment extends Enchantment {

    public ExplosiveArrowEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 12 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.getItem() instanceof BowItem;
    }

    // 添加与力量附魔的冲突检查
    @Override
    protected boolean checkCompatibility(@NotNull Enchantment other) {
        return other != Enchantments.POWER_ARROWS;
    }

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;

        Entity shooter = arrow.getOwner();
        if (!(shooter instanceof Player player)) return;

        // 检查主手或副手是否有附魔的弓
        ItemStack bow = player.getMainHandItem();
        if (!(bow.getItem() instanceof BowItem)) {
            bow = player.getOffhandItem();
            if (!(bow.getItem() instanceof BowItem)) return;
        }

        // 获取附魔等级
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(bow);
        int explosionLevel = enchantments.getOrDefault(ModEnchantments.EXPLOSIVE_ARROW.get(), 0);

        // 如果同时存在力量附魔，不触发爆炸效果
        if (explosionLevel <= 0 || enchantments.containsKey(Enchantments.POWER_ARROWS)) {
            return;
        }

        // 防止箭矢造成原始伤害
        event.setCanceled(true);

        // 获取碰撞位置
        double x = arrow.getX();
        double y = arrow.getY();
        double z = arrow.getZ();

        // 播放爆炸效果（客户端和服务器都会执行）
        playExplosionEffects(arrow.level(), x, y, z, explosionLevel);

        // 只在服务器端计算伤害
        if (!arrow.level().isClientSide() && arrow.level() instanceof ServerLevel serverLevel) {
            applyExplosionDamage(serverLevel, player, x, y, z, explosionLevel);
        }

        // 移除箭矢
        arrow.discard();
    }

    // 播放爆炸效果（客户端和服务器）
    private static void playExplosionEffects(net.minecraft.world.level.Level world, double x, double y, double z, int explosionLevel) {
        // 播放爆炸音效
        world.playSound(
                null,
                new BlockPos((int) x, (int) y, (int) z),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                4.0F,
                (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F
        );

        // 使用更可靠的方式生成粒子效果
        if (world instanceof ServerLevel serverLevel) {
            // 使用数据包向客户端发送粒子效果
            for (ServerPlayer player : serverLevel.players()) {
                // 发送爆炸粒子效果
                player.connection.send(new ClientboundLevelEventPacket(2004, new BlockPos((int) x, (int) y, (int) z), 0, false));

                // 发送火焰粒子效果
                for (int i = 0; i < 20 * explosionLevel; i++) {
                    double offsetX = (world.random.nextDouble() - 0.5) * 4.0;
                    double offsetY = world.random.nextDouble() * 2.0;
                    double offsetZ = (world.random.nextDouble() - 0.5) * 4.0;

                    serverLevel.sendParticles(
                            player,
                            ParticleTypes.FLAME,
                            true,
                            x + offsetX,
                            y + offsetY,
                            z + offsetZ,
                            1, // 粒子数量
                            0.0, 0.0, 0.0, // 扩散
                            0.0 // 速度
                    );
                }
            }
        } else {
            // 客户端备用方案
            // 生成大量爆炸粒子
            for (int i = 0; i < 50 * explosionLevel; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * 5.0;
                double offsetY = world.random.nextDouble() * 3.0;
                double offsetZ = (world.random.nextDouble() - 0.5) * 5.0;

                world.addParticle(
                        ParticleTypes.EXPLOSION,
                        x + offsetX,
                        y + offsetY,
                        z + offsetZ,
                        0.0, 0.0, 0.0
                );
            }

            // 添加火焰粒子
            for (int i = 0; i < 20 * explosionLevel; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * 4.0;
                double offsetY = world.random.nextDouble() * 2.0;
                double offsetZ = (world.random.nextDouble() - 0.5) * 4.0;

                world.addParticle(
                        ParticleTypes.FLAME,
                        x + offsetX,
                        y + offsetY,
                        z + offsetZ,
                        0.0, 0.0, 0.0
                );
            }
        }
    }

    // 应用爆炸伤害（仅服务器端）
    private static void applyExplosionDamage(ServerLevel serverLevel, Player player, double x, double y, double z, int explosionLevel) {
        // 计算爆炸参数
        float damageMultiplier = 1.0F + (explosionLevel * 0.75F);
        double damageRange = 2.0 + (explosionLevel * 1.5);

        // 创建检测范围
        AABB aabb = new AABB(
                x - damageRange, y - damageRange, z - damageRange,
                x + damageRange, y + damageRange, z + damageRange
        );

        // 获取DamageSources实例
        DamageSources damageSources = serverLevel.damageSources();

        // 创建爆炸伤害源 - 使用正确的API
        DamageSource damageSource = damageSources.explosion(player, null);

        // 遍历范围内的所有实体
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, aabb)) {
            if (target == player) continue; // 不伤害玩家自身

            // 计算与爆炸中心的距离
            double distance = target.distanceToSqr(x, y, z);
            double maxDistance = damageRange * damageRange;

            if (distance <= maxDistance) {
                // 根据距离计算伤害（距离越近伤害越高）
                float damage = (float) (8.0 * damageMultiplier * (1.0 - Math.sqrt(distance) / damageRange));

                // 造成爆炸伤害
                target.hurt(damageSource, damage);

                // 添加燃烧效果（如果附魔等级足够高）
                if (explosionLevel >= 2) {
                    target.setSecondsOnFire(2 * explosionLevel);
                }

                // 添加击退效果（如果附魔等级足够高）
                if (explosionLevel >= 3) {
                    double knockbackFactor = 0.5 * (1.0 - Math.sqrt(distance) / damageRange);
                    target.knockback(
                            knockbackFactor,
                            target.getX() - x,
                            target.getZ() - z
                    );
                }
            }
        }
    }
}
