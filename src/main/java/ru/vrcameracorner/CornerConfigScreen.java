package ru.vrcameracorner;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import ru.deelter.vrcamera.client.config.CameraConfig;
import ru.deelter.vrcamera.client.config.ScreenOutput;
import ru.deelter.vrcamera.client.desktop.OutputWindow;

import java.util.Locale;
import java.util.UUID;

/**
 * Settings screen: camera choice, on/off, corner or own window, format, position, size.
 * The corner is shown live here and can be dragged with the mouse.
 */
public class CornerConfigScreen extends Screen {
    private static final int VERTICAL_WIDTH = 540;
    private static final int VERTICAL_HEIGHT = 960;

    private final Screen parent;
    private Button positionButton;
    private boolean dragging;
    private double grabX;
    private double grabY;

    public CornerConfigScreen(Screen parent) {
        super(Component.translatable("vrcamera_corner.config.title"));
        this.parent = parent;
    }

    public Screen parent() {
        return this.parent;
    }

    @Override
    protected void init() {
        final CornerConfig cfg = CornerConfig.get();
        final int cx = this.width / 2;
        final int bw = 200;
        int y = Math.max(10, this.height / 4 - 40);

        this.addRenderableWidget(Button.builder(cameraLabel(cfg),
                btn -> this.minecraft.gui.setScreen(new CameraSelectionScreen(this)))
                .bounds(cx - bw / 2, y, bw, 20).build());
        y += 24;

        this.addRenderableWidget(Button.builder(enableLabel(cfg.enabled), btn -> {
            cfg.enabled = !cfg.enabled;
            CornerConfig.save();
            if (cfg.enabled) {
                wantWindowOutput();
                applyFormat(cfg);
            }
            btn.setMessage(enableLabel(cfg.enabled));
        }).bounds(cx - bw / 2, y, bw, 20).build());
        y += 24;

        this.addRenderableWidget(Button.builder(outputLabel(cfg.output), btn -> {
            cfg.output = cfg.output == CornerConfig.Output.WINDOW ? CornerConfig.Output.CORNER : CornerConfig.Output.WINDOW;
            CornerConfig.save();
            wantWindowOutput();
            applyFormat(cfg);
            btn.setMessage(outputLabel(cfg.output));
        }).bounds(cx - bw / 2, y, bw, 20).build());
        y += 24;

        this.addRenderableWidget(Button.builder(formatLabel(cfg.format), btn -> {
            cfg.format = cfg.format == CornerConfig.Format.VERTICAL ? CornerConfig.Format.NORMAL : CornerConfig.Format.VERTICAL;
            CornerConfig.save();
            applyFormat(cfg);
            btn.setMessage(formatLabel(cfg.format));
        }).bounds(cx - bw / 2, y, bw, 20).build());
        y += 24;

        positionButton = this.addRenderableWidget(Button.builder(positionLabel(cfg), btn -> {
            cfg.position = cfg.isFree() ? cfg.position : nextPosition(cfg.position);
            cfg.unfree();
            CornerConfig.save();
            btn.setMessage(positionLabel(cfg));
        }).bounds(cx - bw / 2, y, bw, 20).build());
        y += 24;

        final Button sizeBtn = Button.builder(sizeLabel(cfg.size), btn -> { })
                .bounds(cx - bw / 2 + 44, y, bw - 88, 20).build();
        sizeBtn.active = false;

        this.addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
            cfg.size = Math.max(0.1f, Math.round((cfg.size - 0.05f) * 100) / 100.0f);
            CornerConfig.save();
            sizeBtn.setMessage(sizeLabel(cfg.size));
        }).bounds(cx - bw / 2, y, 40, 20).build());

        this.addRenderableWidget(sizeBtn);

        this.addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
            cfg.size = Math.min(0.6f, Math.round((cfg.size + 0.05f) * 100) / 100.0f);
            CornerConfig.save();
            sizeBtn.setMessage(sizeLabel(cfg.size));
        }).bounds(cx + bw / 2 - 40, y, 40, 20).build());
        y += 30;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
                btn -> this.minecraft.gui.setScreen(this.parent))
                .bounds(cx - bw / 2, y, bw, 20).build());
        y += 22;

        this.addRenderableWidget(new StringWidget(cx - 160, y, 320, 20,
                Component.translatable("vrcamera_corner.config.drag_hint"), this.font));
    }

    /**
     * the corner in the coordinates of this screen: x, y, width, height
     */
    private int[] cornerOnScreen() {
        final RenderTarget main = this.minecraft.gameRenderer.mainRenderTarget();
        final int[] r = CornerRenderer.cornerRect(main.width, main.height);
        final double s = this.width / (double) Math.max(1, main.width);
        return new int[]{(int) (r[0] * s), (int) (r[1] * s), (int) Math.ceil(r[2] * s), (int) Math.ceil(r[3] * s)};
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (event.button() == 0 && CornerConfig.get().cornerShown()) {
            final int[] r = cornerOnScreen();
            if (event.x() >= r[0] && event.x() < r[0] + r[2] && event.y() >= r[1] && event.y() < r[1] + r[3]) {
                dragging = true;
                grabX = event.x() - r[0];
                grabY = event.y() - r[1];
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging) {
            final CornerConfig cfg = CornerConfig.get();
            final int[] r = cornerOnScreen();
            final double roomX = Math.max(1, this.width - r[2]);
            final double roomY = Math.max(1, this.height - r[3]);
            cfg.freeX = (float) Math.clamp((event.x() - grabX) / roomX, 0.0, 1.0);
            cfg.freeY = (float) Math.clamp((event.y() - grabY) / roomY, 0.0, 1.0);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging) {
            dragging = false;
            CornerConfig.save();
            if (positionButton != null) {
                positionButton.setMessage(positionLabel(CornerConfig.get()));
            }
            return true;
        }
        return super.mouseReleased(event);
    }

    /**
     * both the corner and the own window need VRCamera to draw its picture apart from the player's view
     */
    private static void wantWindowOutput() {
        final CameraConfig camera = CameraConfig.current();
        if (camera.screenOutput != ScreenOutput.WINDOW) {
            camera.screenOutput = ScreenOutput.WINDOW;
            camera.save();
        }
    }

    /**
     * vertical: VRCamera draws a 9:16 picture, and its own window gets that shape too
     */
    static void applyFormat(CornerConfig cfg) {
        final CameraConfig camera = CameraConfig.current();
        final boolean vertical = cfg.format == CornerConfig.Format.VERTICAL;
        camera.outputWidth = vertical ? VERTICAL_WIDTH : 0;
        camera.outputHeight = vertical ? VERTICAL_HEIGHT : 0;
        camera.save();
        if (cfg.enabled && cfg.output == CornerConfig.Output.WINDOW) {
            try {
                OutputWindow.setSize(vertical ? VERTICAL_WIDTH : 960, vertical ? VERTICAL_HEIGHT : 540);
            } catch (RuntimeException | LinkageError e) {
                VRCameraCornerMod.LOGGER.warn("VRCameraCorner: could not resize the camera window", e);
            }
        }
    }

    private static Component cameraLabel(CornerConfig cfg) {
        final UUID selected = cfg.getSelectedCameraOwner();
        if (selected == null) {
            return Component.translatable("vrcamera_corner.config.camera")
                    .append(": ")
                    .append(Component.translatable("vrcamera_corner.config.camera.own"));
        }
        final String name = ru.vrcameracorner.network.CameraTracker.getInstance()
                .getAvailableCameras().getOrDefault(selected, "?");
        return Component.translatable("vrcamera_corner.config.camera").append(": " + name);
    }

    private static Component outputLabel(CornerConfig.Output output) {
        return Component.translatable("vrcamera_corner.config.output")
                .append(": ")
                .append(Component.translatable("vrcamera_corner.output." + output.name().toLowerCase(Locale.ROOT)));
    }

    private static Component formatLabel(CornerConfig.Format format) {
        return Component.translatable("vrcamera_corner.config.format")
                .append(": ")
                .append(Component.translatable("vrcamera_corner.format." + format.name().toLowerCase(Locale.ROOT)));
    }

    private static Component sizeLabel(float size) {
        return Component.translatableWithFallback("vrcamera_corner.config.size", "Size")
                .append(": " + Math.round(size * 100) + "%");
    }

    private static Component enableLabel(boolean on) {
        return Component.translatable(on ? "vrcamera_corner.config.enabled"
                                        : "vrcamera_corner.config.disabled");
    }

    private static MutableComponent positionLabel(CornerConfig cfg) {
        final String key = cfg.isFree() ? "vrcamera_corner.position.free"
                : "vrcamera_corner.position." + cfg.position.name().toLowerCase(Locale.ROOT);
        return Component.translatable("vrcamera_corner.config.position")
                .append(": ")
                .append(Component.translatable(key));
    }

    private static CornerConfig.Position nextPosition(CornerConfig.Position p) {
        return switch (p) {
            case TOP_LEFT     -> CornerConfig.Position.TOP_RIGHT;
            case TOP_RIGHT    -> CornerConfig.Position.BOTTOM_RIGHT;
            case BOTTOM_RIGHT -> CornerConfig.Position.BOTTOM_LEFT;
            case BOTTOM_LEFT  -> CornerConfig.Position.TOP_LEFT;
        };
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
