package ru.vrcameracorner.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.deelter.vrcamera.client.desktop.DirectorPass;
import ru.vrcameracorner.CornerRenderer;

/**
 * The corner is drawn at the end of every frame, also on the frames VRCamera does not draw its picture in.
 */
@Mixin(value = DirectorPass.class, remap = false)
public abstract class DirectorPassMixin {
    @Inject(method = "onFrame", at = @At("RETURN"))
    private static void vrcameraCorner$everyFrame(Minecraft mc, DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        CornerRenderer.onFrameEnd(mc);
    }
}
