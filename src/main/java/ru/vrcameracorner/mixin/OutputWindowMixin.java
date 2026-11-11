package ru.vrcameracorner.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.deelter.vrcamera.client.desktop.OutputWindow;
import ru.vrcameracorner.CornerConfig;
import ru.vrcameracorner.CornerRenderer;

/**
 * With the addon on, the window of the camera is never opened: its picture goes into a corner of the game window.
 */
@Mixin(value = OutputWindow.class, remap = false)
public abstract class OutputWindowMixin {
    @Inject(method = "open", at = @At("HEAD"), cancellable = true)
    private static void vrcameraCorner$open(Minecraft mc, CallbackInfoReturnable<Boolean> cir) {
        if (CornerConfig.get().cornerShown()) {
            OutputWindow.close();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "size", at = @At("HEAD"), cancellable = true)
    private static void vrcameraCorner$size(CallbackInfoReturnable<int[]> cir) {
        if (CornerConfig.get().cornerShown()) {
            final RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
            cir.setReturnValue(CornerRenderer.cornerSize(main.width, main.height));
        }
    }

    @Inject(method = "show", at = @At("HEAD"), cancellable = true)
    private static void vrcameraCorner$show(Minecraft mc, RenderTarget picture, boolean guides, boolean fill, CallbackInfo ci) {
        if (CornerConfig.get().cornerShown()) {
            CornerRenderer.onShow(mc, picture);
            ci.cancel();
        }
    }

    @Inject(method = "isFocused", at = @At("HEAD"), cancellable = true)
    private static void vrcameraCorner$isFocused(CallbackInfoReturnable<Boolean> cir) {
        if (CornerConfig.get().cornerShown()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    private static void vrcameraCorner$isKeyDown(int key, CallbackInfoReturnable<Boolean> cir) {
        if (CornerConfig.get().cornerShown()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleKeys", at = @At("HEAD"), cancellable = true)
    private static void vrcameraCorner$handleKeys(CallbackInfo ci) {
        if (CornerConfig.get().cornerShown()) {
            ci.cancel();
        }
    }

    @Inject(method = "capture", at = @At("HEAD"), cancellable = true)
    private static void vrcameraCorner$capture(boolean wanted, CallbackInfo ci) {
        if (CornerConfig.get().cornerShown()) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseMoved", at = @At("HEAD"), cancellable = true)
    private static void vrcameraCorner$mouseMoved(CallbackInfoReturnable<double[]> cir) {
        if (CornerConfig.get().cornerShown()) {
            cir.setReturnValue(new double[2]);
        }
    }
}
