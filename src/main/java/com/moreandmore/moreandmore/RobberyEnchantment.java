package com.moreandmore.moreandmore;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.entity.raid.AbstractRaider;

@Mod.EventBusSubscriber(modid = "moreandmore", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RobberyEnchantment extends Enchantment {

    public RobberyEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinLevel() {
        return 1;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isTreasureOnly() {
        return true; // 宝藏附魔
    }

    @Override
    public boolean isTradeable() {
        return true; // 可以通过图书管理员交易获得
    }

    @Override
    public boolean isDiscoverable() {
        return false; // 不能通过附魔台获得
    }

    // 限制只能附魔在剑类武器上
    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack) {
        return false; // 不能在附魔台使用
    }

    // 处理攻击事件（造成伤害时掉落）
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player player &&
                event.getAmount() > 0) {

            ItemStack weapon = player.getMainHandItem();
            if (weapon.getItem() instanceof SwordItem) {
                int robberyLevel = weapon.getEnchantmentLevel(ModEnchantments.ROBBERY.get());

                if (robberyLevel > 0) {
                    // 检查目标是否是村民或灾厄村民
                    if (event.getEntity() instanceof Villager || event.getEntity() instanceof AbstractRaider) {
                        // 现在的 AbstractRaider 包含了：卫道士、掠夺者、唤魔者以及劫掠兽！
                        dropAttackEmeralds(event.getEntity(), robberyLevel);
                    }
                }
            }
        }
    }

    // 处理死亡事件（击杀时额外掉落）
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            ItemStack weapon = player.getMainHandItem();
            if (weapon.getItem() instanceof SwordItem) {
                int robberyLevel = weapon.getEnchantmentLevel(ModEnchantments.ROBBERY.get());

                if (robberyLevel > 0) {
                    // 检查目标是否是村民或灾厄村民
                    if (event.getEntity() instanceof Villager || event.getEntity() instanceof AbstractRaider) {
                        // 现在的 AbstractRaider 包含了：卫道士、掠夺者、唤魔者以及劫掠兽！
                        dropAttackEmeralds(event.getEntity(), robberyLevel);
                    }
                }
            }
        }
    }

    private static void dropAttackEmeralds(net.minecraft.world.entity.Entity entity, int robberyLevel) {
        net.minecraft.world.level.Level level = entity.level();

        if (!level.isClientSide) {
            // 攻击掉落：0 到 (robberyLevel) 颗绿宝石
            int dropCount = level.random.nextInt(robberyLevel + 1);

            if (dropCount > 0) {
                spawnEmeraldEntity(level, entity, dropCount);
            }
        }
    }

    private static void dropKillEmeralds(net.minecraft.world.entity.Entity entity) {
        net.minecraft.world.level.Level level = entity.level();

        if (!level.isClientSide) {
            // 击杀额外掉落：固定2颗绿宝石
            spawnEmeraldEntity(level, entity, 2);
        }
    }

    private static void spawnEmeraldEntity(net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity, int count) {
        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                level,
                entity.getX(),
                entity.getY() + 0.5,
                entity.getZ(),
                new ItemStack(Items.EMERALD, count)
        );

        // 添加一些随机动量使掉落更自然
        itemEntity.setDeltaMovement(
                level.random.nextDouble() * 0.2 - 0.1,
                0.2,
                level.random.nextDouble() * 0.2 - 0.1
        );

        level.addFreshEntity(itemEntity);
    }
}
