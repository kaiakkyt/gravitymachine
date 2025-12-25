package kaiakk.sphericality.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GravityHookItem extends Item {
    public GravityHookItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot equipmentSlot) {
        if (equipmentSlot != null && entity instanceof Player player) {
            double radius = 8.0;
            double strength = 0.07;

            AABB area = player.getBoundingBox().inflate(radius);

            List<Entity> targets = serverLevel.getEntities(player, area, e ->
                    e instanceof ItemEntity || e instanceof ExperienceOrb || e instanceof LivingEntity);

            for (Entity target : targets) {
                Vec3 pull = player.position().subtract(target.position()).normalize().scale(strength);

                target.setDeltaMovement(target.getDeltaMovement().add(pull));

                target.hasImpulse = true;
                target.hurtMarked = true;
            }
        }
    }
}
