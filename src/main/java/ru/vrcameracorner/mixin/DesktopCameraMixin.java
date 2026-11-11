package ru.vrcameracorner.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.deelter.vrcamera.client.desktop.DesktopCamera;
import ru.deelter.vrcamera.client.desktop.DirectorPass;
import ru.vrcameracorner.CornerConfig;
import ru.vrcameracorner.network.CameraData;
import ru.vrcameracorner.network.CameraTracker;

import java.util.UUID;

/**
 * While VRCamera draws its own picture, it is drawn from another player's camera instead, when one is chosen.
 * Only the view is taken: that camera is not steered. While watching another camera, the own camera model
 * and its icon are hidden, so it does not hang around in the world.
 */
@Mixin(value = DesktopCamera.class, remap = false)
public abstract class DesktopCameraMixin {

    private static CameraData vrcameraCorner$watched() {
        final CornerConfig cfg = CornerConfig.get();
        if (cfg == null || !cfg.enabled) {
            return null;
        }
        final UUID owner = cfg.getSelectedCameraOwner();
        if (owner == null) {
            return null;
        }
        return CameraTracker.getInstance().getCamera(owner);
    }

    @Inject(method = "update", at = @At("RETURN"), cancellable = true)
    private void vrcameraCorner$otherCamera(float partialTick, CallbackInfoReturnable<DesktopCamera.Pose> cir) {
        if (!DirectorPass.isActive()) {
            return;
        }
        final DesktopCamera.Pose own = cir.getReturnValue();
        if (own == null) {
            return;
        }
        final CameraData other = vrcameraCorner$watched();
        if (other == null) {
            return;
        }
        cir.setReturnValue(new DesktopCamera.Pose(new Vec3(other.x, other.y, other.z),
                new Quaternionf(other.qx, other.qy, other.qz, other.qw).normalize(), own.fov()));
    }

    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true, require = 0)
    private void vrcameraCorner$hideOwnModel(SubmitNodeCollector output, Vec3 viewPosition, PoseStack poseStack,
                                             CallbackInfo ci) {
        if (vrcameraCorner$watched() != null) {
            ci.cancel();
        }
    }

    @Inject(method = "drawLabel", at = @At("HEAD"), cancellable = true, require = 0)
    private void vrcameraCorner$hideOwnLabel(CallbackInfo ci) {
        if (vrcameraCorner$watched() != null) {
            ci.cancel();
        }
    }
}
