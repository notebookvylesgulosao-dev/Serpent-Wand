package com.bonewand.mixin;

import com.bonewand.BoneWandMod;
import com.bonewand.client.BoneWandAnimations;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.bonewand.client.BoneWandAnimations.*;

/** Third-person (and other players') arm animation for the Bone Wand. */
@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin {
    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
    private void bonewand$animateArms(PlayerEntityRenderState state, CallbackInfo ci) {
        if (state.isGliding || state.isSwimming) return;
        BipedEntityModel<?> self = (BipedEntityModel<?>) (Object) this;
        apply(self, state, Arm.RIGHT);
        apply(self, state, Arm.LEFT);
    }

    private static void apply(BipedEntityModel<?> model, PlayerEntityRenderState state, Arm arm) {
        if (!state.getItemStackForArm(arm).isOf(BoneWandMod.BONE_WAND)) return;
        boolean right = arm == Arm.RIGHT;
        Arm usingArm = state.activeHand == Hand.MAIN_HAND ? state.mainArm : state.mainArm.getOpposite();
        boolean charging = state.isUsingItem && usingArm == arm;
        float cast = castTicks(state.id, right);
        if (cast < 0 && !charging) return;

        ModelPart part = right ? model.rightArm : model.leftArm;
        float inward = right ? -1.0F : 1.0F;
        float headYaw = model.head.yaw;
        float headPitch = model.head.pitch;

        // Raised ("concentrating") pose and forward ("casting") pose.
        float raisePitch = -2.55F + headPitch * 0.3F;
        float raiseYaw = inward * 0.35F;
        float raiseRoll = -inward * 0.12F;
        float fwdPitch = -(float) (Math.PI / 2) + headPitch;
        float fwdYaw = headYaw + inward * 0.1F;
        float fwdRoll = 0.0F;

        float pitch, yaw, roll, w;
        if (cast < 0) {
            w = easeOutCubic(clamp01(state.itemUseTime / CHARGE_RAMP));
            float tremble = (float) Math.sin(state.age * 1.3F);
            pitch = raisePitch + tremble * 0.025F * w;
            yaw = raiseYaw + (float) Math.cos(state.age * 1.1F) * 0.02F * w;
            roll = raiseRoll;
        } else if (cast < THRUST_END) {
            float u = easeOutCubic(cast / THRUST_END);
            w = 1.0F;
            pitch = lerp(raisePitch, fwdPitch, u);
            yaw = lerp(raiseYaw, fwdYaw, u);
            roll = lerp(raiseRoll, fwdRoll, u);
        } else if (cast < HOLD_END) {
            w = 1.0F;
            pitch = fwdPitch + (float) Math.sin((cast - THRUST_END) * 0.8F) * 0.03F;
            yaw = fwdYaw;
            roll = fwdRoll;
        } else {
            w = 1.0F - easeInOut(clamp01((cast - HOLD_END) / (TOTAL - HOLD_END)));
            pitch = fwdPitch;
            yaw = fwdYaw;
            roll = fwdRoll;
        }

        part.pitch = lerp(part.pitch, pitch, w);
        part.yaw = lerp(part.yaw, yaw, w);
        part.roll = lerp(part.roll, roll, w);
    }
}
