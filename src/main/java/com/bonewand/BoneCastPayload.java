package com.bonewand;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Sent by the server when a Bone Wand spell is released, so clients can play the cast animation. */
public record BoneCastPayload(int entityId, boolean rightArm) implements CustomPayload {
    public static final CustomPayload.Id<BoneCastPayload> ID =
        new CustomPayload.Id<>(Identifier.of(BoneWandMod.MOD_ID, "cast"));

    public static final PacketCodec<net.minecraft.network.RegistryByteBuf, BoneCastPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.VAR_INT, BoneCastPayload::entityId,
            PacketCodecs.BOOLEAN, BoneCastPayload::rightArm,
            BoneCastPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
