package ru.vrcameracorner;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import ru.vrcameracorner.network.CameraTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Which camera the corner shows: the own one or another player's (view only).
 */
public class CameraSelectionScreen extends Screen {
    private final Screen parent;

    public CameraSelectionScreen(Screen parent) {
        super(Component.translatable("vrcamera_corner.config.camera.select"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        final int cx = this.width / 2;
        final int bw = 200;
        int y = 30;

        this.addRenderableWidget(Button.builder(
                Component.translatable("vrcamera_corner.config.camera.own"),
                btn -> choose(null)).bounds(cx - bw / 2, y, bw, 20).build());
        y += 24;

        final List<Map.Entry<UUID, String>> sorted =
                new ArrayList<>(CameraTracker.getInstance().getAvailableCameras().entrySet());
        sorted.sort(Map.Entry.comparingByValue());
        for (final Map.Entry<UUID, String> entry : sorted) {
            if (y > this.height - 50) {
                break;
            }
            final UUID uuid = entry.getKey();
            this.addRenderableWidget(Button.builder(Component.literal(entry.getValue()),
                    btn -> choose(uuid)).bounds(cx - bw / 2, y, bw, 20).build());
            y += 24;
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK,
                btn -> this.minecraft.gui.setScreen(this.parent))
                .bounds(cx - bw / 2, this.height - 28, bw, 20).build());
    }

    private void choose(UUID owner) {
        CornerConfig.get().setSelectedCameraOwner(owner);
        CornerConfig.save();
        this.minecraft.gui.setScreen(new CornerConfigScreen(this.parent instanceof CornerConfigScreen c ? c.parent() : this.parent));
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
