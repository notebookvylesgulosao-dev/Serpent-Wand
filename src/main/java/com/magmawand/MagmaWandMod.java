package com.magmawand;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;

public class MagmaWandMod implements ModInitializer {
    public static final String MOD_ID = "magmawand";
    private static final int CHARGE_TICKS = 24; // 1.2 seconds
    public static final Item MAGMA_WAND = Registry.register(
        Registries.ITEM,
        Identifier.of(MOD_ID, "magma_wand"),
        new Item(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "magma_wand")))
            .maxCount(1)) {
            @Override public UseAction getUseAction(ItemStack stack) { return UseAction.BOW; }
            @Override public int getMaxUseTime(ItemStack stack, LivingEntity user) { return 72; }
            @Override public ActionResult use(World world, PlayerEntity user, Hand hand) {
                user.setCurrentHand(hand);
                return ActionResult.CONSUME;
            }
            @Override public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
                int heldTicks = getMaxUseTime(stack, user) - remainingUseTicks;
                if (heldTicks < CHARGE_TICKS) return false;
                if (!world.isClient() && world instanceof ServerWorld serverWorld && user instanceof PlayerEntity player) {
                    castVisualSpell(serverWorld, player);
                    player.getItemCooldownManager().set(stack, 16);
                }
                return true;
            }
        }
    );
    private static void castVisualSpell(ServerWorld world, PlayerEntity user) {
        Vec3d center = new Vec3d(user.getX(), user.getY(), user.getZ());
        for (int i = 0; i < 28; i++) {
            double angle = (Math.PI * 2.0 * i) / 28.0;
            double x = center.x + Math.cos(angle) * 1.05;
            double z = center.z + Math.sin(angle) * 1.05;
            world.spawnParticles(ParticleTypes.FLAME, x, center.y + 0.15, z, 1, 0.02, 0.35, 0.02, 0.0);
        }
        Vec3d start = user.getEyePos();
        Vec3d direction = user.getRotationVec(1.0F);
        for (int i = 1; i <= 16; i++) {
            Vec3d point = start.add(direction.multiply(i * 0.32));
            world.spawnParticles(ParticleTypes.LAVA, point.x, point.y, point.z, 2, 0.025, 0.025, 0.025, 0.0);
        }
        world.spawnParticles(ParticleTypes.SMOKE, start.x, start.y, start.z, 8, 0.08, 0.08, 0.08, 0.01);
    }
    @Override public void onInitialize() { }
}
