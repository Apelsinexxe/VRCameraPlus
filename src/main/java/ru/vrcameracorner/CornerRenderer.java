package ru.vrcameracorner;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import ru.deelter.vrcamera.client.desktop.DirectorPass;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class CornerRenderer {
    private static final int BORDER = 2;
    private static int drawFramebuffer;
    private static int readFramebuffer;
    private static Method glIdMethod;
    private static boolean ownView;

    private CornerRenderer() {
    }

    public static int[] cornerSize(int gameWidth, int gameHeight) {
        final CornerConfig cfg = CornerConfig.get();
        if (cfg.format == CornerConfig.Format.VERTICAL) {
            // 9:16, as tall as the game window allows
            final int tall = Math.max(16, Math.min(gameHeight - 2 * cfg.margin, Math.round(gameHeight * cfg.size * 1.6f)));
            return new int[]{Math.max(9, Math.round(tall * 9 / 16.0f)), tall};
        }
        final int width = Math.max(16, Math.round(gameWidth * cfg.size));
        final int height = Math.max(9, Math.round(width * gameHeight / (float) Math.max(1, gameWidth)));
        return new int[]{width, height};
    }

    /**
     * @return x, y from the top, width and height of the corner, in pixels of the game window
     */
    public static int[] cornerRect(int mainWidth, int mainHeight) {
        final CornerConfig config = CornerConfig.get();
        final int[] size = cornerSize(mainWidth, mainHeight);
        final int w = size[0];
        final int h = size[1];
        final int m = config.margin;
        final int x;
        final int top;
        if (config.isFree()) {
            x = Math.round(config.freeX * Math.max(0, mainWidth - w));
            top = Math.round(config.freeY * Math.max(0, mainHeight - h));
        } else {
            final boolean left = config.position == CornerConfig.Position.TOP_LEFT || config.position == CornerConfig.Position.BOTTOM_LEFT;
            final boolean up = config.position == CornerConfig.Position.TOP_LEFT || config.position == CornerConfig.Position.TOP_RIGHT;
            x = left ? m : mainWidth - w - m;
            top = up ? m : mainHeight - h - m;
        }
        return new int[]{x, top, w, h};
    }

    public static void onShow(Minecraft mc, RenderTarget picture) {
        ownView = picture == mc.gameRenderer.mainRenderTarget();
    }

    public static void onFrameEnd(Minecraft mc) {
        if (!CornerConfig.get().cornerShown() || DirectorPass.isActive()) {
            return;
        }
        final RenderTarget picture = DirectorPass.picture();
        if (picture == null) {
            ownView = false;
            return;
        }
        if (ownView) {
            return;
        }
        final Screen current = mc.gui.screen();
        // the settings screen shows the corner as a live preview
        if (current != null && !(current instanceof ChatScreen) && !(current instanceof CornerConfigScreen)) {
            return;
        }
        draw(mc.gameRenderer.mainRenderTarget(), picture);
    }

    private static void draw(RenderTarget main, RenderTarget picture) {
        if (picture == main || picture.width <= 0 || picture.height <= 0) {
            return;
        }
        final int[] rect = cornerRect(main.width, main.height);
        final int x = rect[0];
        final int w = rect[2];
        final int h = rect[3];
        // OpenGL counts from the bottom
        final int y = main.height - rect[1] - h;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            final int prevRead = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
            final int prevDraw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
            final boolean scissors = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
            final IntBuffer viewport = stack.mallocInt(4);
            final IntBuffer scissor = stack.mallocInt(4);
            final FloatBuffer clearColor = stack.mallocFloat(4);
            final ByteBuffer colorMask = stack.malloc(4);
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);
            GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, clearColor);
            GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, colorMask);
            try {
                if (drawFramebuffer == 0) {
                    drawFramebuffer = GL30.glGenFramebuffers();
                    readFramebuffer = GL30.glGenFramebuffers();
                }
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
                GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
                        textureId(main), 0);
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
                GL30.glFramebufferTexture2D(GL30.GL_READ_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D,
                        textureId(picture), 0);
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GL11.glColorMask(true, true, true, true);
                GL30.glBlitFramebuffer(0, 0, picture.width, picture.height, x, y, x + w, y + h,
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR);
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
                strip(x, y + h - BORDER, w, BORDER);
                strip(x, y, w, BORDER);
                strip(x, y, BORDER, h);
                strip(x + w - BORDER, y, BORDER, h);
            } catch (RuntimeException e) {
                VRCameraCornerMod.LOGGER.error("VRCameraCorner: corner draw failed", e);
            } finally {
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevRead);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevDraw);
                GL11.glViewport(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3));
                GL11.glScissor(scissor.get(0), scissor.get(1), scissor.get(2), scissor.get(3));
                if (scissors) GL11.glEnable(GL11.GL_SCISSOR_TEST);
                else          GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GL11.glClearColor(clearColor.get(0), clearColor.get(1), clearColor.get(2), clearColor.get(3));
                GL11.glColorMask(colorMask.get(0) != 0, colorMask.get(1) != 0,
                        colorMask.get(2) != 0, colorMask.get(3) != 0);
            }
        }
    }

    private static void strip(int x, int y, int width, int height) {
        GL11.glScissor(x, y, width, height);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }

    private static int textureId(RenderTarget target) {
        final Object texture = target.getColorTexture();
        try {
            if (glIdMethod == null || glIdMethod.getDeclaringClass() != texture.getClass()) {
                glIdMethod = texture.getClass().getMethod("glId");
            }
            return (int) glIdMethod.invoke(texture);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("VRCameraCorner needs the OpenGL renderer of the game", e);
        }
    }
}
