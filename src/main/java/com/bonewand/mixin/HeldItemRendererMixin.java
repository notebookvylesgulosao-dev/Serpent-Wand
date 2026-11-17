package com.bonewand.mixin;

import com.bonewand.BoneWandMod;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.bonewand.client.BoneWandAnimations.*;

/** First-person animation: the wand is raised while charging, thrust forward on release, then settles back. */
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void bonewand$animate(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                                  float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                                  OrderedRenderCommandQueue queue, int light, CallbackInfo ci) {
        if (!item.isOf(BoneWandMod.BONE_WAND)) return;
        Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean right = arm == Arm.RIGHT;
        float sign = right ? 1.0F : -1.0F;
        boolean charging = player.isUsingItem() && player.getActiveHand() == hand;
        float cast = castTicks(player.getId(), right);
        if (cast < 0 && !charging) return;

        // Pose vectors: x, y, z offset (camera space) and tilt in degrees.
        float[] raised = {-0.12F * sign, 0.30F, -0.02F, 18.0F, -10.0F * sign};
        float[] thrust = {-0.06F * sign, 0.10F, -0.50F, -25.0F, 0.0F};

        float[] pose;
        float w;
        if (cast < 0) {
            float useTicks = player.getItemUseTime() + tickDelta;
            w = easeOutCubic(clamp01(useTicks / CHARGE_RAMP));
            pose = raised.clone();
            pose[1] += (float) Math.sin(useTicks * 1.3F) * 0.006F * w;
            pose[0] += (float) Math.cos(useTicks * 1.1F) * 0.004F * w;
        } else if (cast < THRUST_END) {
            float u = easeOutCubic(cast / THRUST_END);
            w = 1.0F;
            pose = new float[5];
            for (int i = 0; i < 5; i++) pose[i] = lerp(raised[i], thrust[i], u);
        } else if (cast < HOLD_END) {
            w = 1.0F;
            pose = thrust;
        } else {
            w = 1.0F - easeInOut(clamp01((cast - HOLD_END) / (TOTAL - HOLD_END)));
            pose = thrust;
        }

        // Rotate around roughly where the hand sits so the wand pivots instead of orbiting the camera.
        float px = 0.56F * sign, py = -0.52F, pz = -0.72F;
        matrices.translate(pose[0] * w, pose[1] * w, pose[2] * w);
        matrices.translate(px, py, pz);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pose[3] * w));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(pose[4] * w));
        matrices.translate(-px, -py, -pz);
    }
}
