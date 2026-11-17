package com.bonewand.client;

import java.util.HashMap;
import java.util.Map;

/** Client-side bookkeeping and easing helpers for the Bone Wand animation. */
public final class BoneWandAnimations {
    private record Cast(long startNanos, boolean rightArm) {}

    private static final Map<Integer, Cast> CASTS = new HashMap<>();

    public static final float CHARGE_RAMP = 8.0F;   // ticks to fully raise the arm
    public static final float THRUST_END = 2.5F;    // ticks for the fast forward thrust
    public static final float HOLD_END = 7.0F;      // ticks the arm stays pointing forward
    public static final float TOTAL = 18.0F;        // ticks until the arm is back to normal

    private BoneWandAnimations() {}

    public static void startCast(int entityId, boolean rightArm) {
        CASTS.put(entityId, new Cast(System.nanoTime(), rightArm));
    }

    /** Ticks since the spell was released for this arm, or -1 if no cast animation is running. */
    public static float castTicks(int entityId, boolean rightArm) {
        Cast c = CASTS.get(entityId);
        if (c == null || c.rightArm() != rightArm) return -1.0F;
        float ticks = (System.nanoTime() - c.startNanos()) / 50_000_000.0F;
        if (ticks > TOTAL) {
            CASTS.remove(entityId);
            return -1.0F;
        }
        return ticks;
    }

    public static float clamp01(float v) { return v < 0 ? 0 : Math.min(v, 1); }
    public static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    public static float easeOutCubic(float t) { float u = 1 - t; return 1 - u * u * u; }
    public static float easeInOut(float t) { return t * t * (3 - 2 * t); }
}
