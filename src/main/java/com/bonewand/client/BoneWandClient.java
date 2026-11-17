package com.bonewand.client;

import com.bonewand.BoneCastPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class BoneWandClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BoneCastPayload.ID,
            (payload, context) -> BoneWandAnimations.startCast(payload.entityId(), payload.rightArm()));
    }
}
