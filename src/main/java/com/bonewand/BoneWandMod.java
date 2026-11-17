package com.bonewand;

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
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;

public class BoneWandMod implements ModInitializer {
    public static final String MOD_ID = "bonewand";
    private static final int CHARGE_TICKS = 24; // 1.2 seconds
    public static final Item BONE_WAND = Registry.register(
        Registries.ITEM,
        Identifier.of(MOD_ID, "bone_wand"),
        new Item(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "bone_wand")))
            .maxCount(1)) {
            @Override public UseAction getUseAction(ItemStack stack) { return UseAction.NONE; }
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
                    if (player instanceof ServerPlayerEntity sp) sendCast(sp, user.getActiveHand());
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
            world.spawnParticles(ParticleTypes.END_ROD, x, center.y + 0.15, z, 1, 0.02, 0.35, 0.02, 0.0);
        }
        Vec3d start = user.getEyePos();
        Vec3d direction = user.getRotationVec(1.0F);
        for (int i = 1; i <= 16; i++) {
            Vec3d point = start.add(direction.multiply(i * 0.32));
            world.spawnParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 2, 0.025, 0.025, 0.025, 0.0);
        }
        world.spawnParticles(ParticleTypes.SOUL, start.x, start.y, start.z, 8, 0.08, 0.08, 0.08, 0.01);
    }
    private static void sendCast(ServerPlayerEntity player, Hand hand) {
        Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        BoneCastPayload payload = new BoneCastPayload(player.getId(), arm == Arm.RIGHT);
        ServerPlayNetworking.send(player, payload);
        for (ServerPlayerEntity other : PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(other, payload);
        }
    }

    @Override public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(BoneCastPayload.ID, BoneCastPayload.CODEC);
    }
}
