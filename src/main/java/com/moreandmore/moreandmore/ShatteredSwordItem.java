package com.moreandmore.moreandmore;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.List;

public class ShatteredSwordItem extends SwordItem {
    public ShatteredSwordItem() {
        // Tiers.NETHERITE 是材质，5 是伤害增加值，-2.4f 是攻速速度修正
        super(Tiers.NETHERITE, 5, -2.4f, new Item.Properties().durability(1200).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack sword = player.getItemInHand(hand);

        // 检查冷却
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(sword);

        // 血量检查：如果血量小于或等于 1.0 (半颗心)，则无法发动
        if (player.getHealth() <= 1.0F) {
            if (level.isClientSide) {
                // 在快捷栏上方显示红色的提示文字
                player.displayClientMessage(Component.translatable("message.moreandmore.too_weak").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(sword);
        }

        if (!level.isClientSide()) {
            // 扣除当前生命值的 30%
            float healthToRemove = player.getHealth() * 0.3f;
            // 使用 fellOutOfWorld (虚空伤害) 以绕过护甲减伤
            player.hurt(player.damageSources().fellOutOfWorld(), Math.max(1.0F, healthToRemove));

            // 给玩家添加效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 500, 1)); // 500 ticks = 25秒
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 2));     // 力量 III

            // 设置 5 秒冷却
            player.getCooldowns().addCooldown(this, 100);

            // 消耗 5 点耐久
            sword.hurtAndBreak(5, player, e -> e.broadcastBreakEvent(hand));
        }

        return InteractionResultHolder.success(sword);
    }

    // 在物品信息里显示描述
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        // 从语言文件中读取 item.moreandmore.shattered_sword.desc
        tooltip.add(Component.translatable("item.moreandmore.shattered_sword.desc").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
