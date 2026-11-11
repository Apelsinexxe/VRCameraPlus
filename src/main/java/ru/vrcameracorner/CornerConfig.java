package ru.vrcameracorner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class CornerConfig {
    private static final Path CONFIG_PATH = Path.of("config/vrcamera_corner.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static CornerConfig instance = new CornerConfig();

    public enum Position {
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    public enum Output {
        CORNER, WINDOW
    }

    /**
     * NORMAL is the shape of the game window, VERTICAL is 9:16 like TikTok
     */
    public enum Format {
        NORMAL, VERTICAL
    }

    public boolean enabled = false;
    public Output output = Output.CORNER;
    public Format format = Format.NORMAL;
    public Position position = Position.TOP_LEFT;
    /**
     * where the corner was dragged to, 0..1 of the free room across and down; below 0 means it sits in position
     */
    public float freeX = -1.0f;
    public float freeY = -1.0f;
    public float size = 0.25f;
    public int margin = 10;

    @SerializedName("selectedCamera")
    private String selectedCameraString;
    private transient UUID selectedCameraOwner;

    /**
     * @return if the picture goes into the corner of the game window, not into VRCamera's own window
     */
    public boolean cornerShown() {
        return enabled && output != Output.WINDOW;
    }

    public boolean isFree() {
        return freeX >= 0.0f && freeY >= 0.0f;
    }

    public void unfree() {
        freeX = -1.0f;
        freeY = -1.0f;
    }

    public UUID getSelectedCameraOwner() {
        if (selectedCameraOwner == null && selectedCameraString != null) {
            try {
                selectedCameraOwner = UUID.fromString(selectedCameraString);
            } catch (IllegalArgumentException e) {
                selectedCameraString = null;
            }
        }
        return selectedCameraOwner;
    }

    public void setSelectedCameraOwner(UUID uuid) {
        this.selectedCameraOwner = uuid;
        this.selectedCameraString = uuid == null ? null : uuid.toString();
    }

    public static CornerConfig get() {
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                final CornerConfig read = GSON.fromJson(Files.readString(CONFIG_PATH), CornerConfig.class);
                instance = read != null ? read : new CornerConfig();
                if (instance.position == null) {
                    instance.position = Position.TOP_LEFT;
                }
                if (instance.output == null) {
                    instance.output = Output.CORNER;
                }
                if (instance.format == null) {
                    instance.format = Format.NORMAL;
                }
            } else {
                instance = new CornerConfig();
                save();
            }
        } catch (IOException | RuntimeException e) {
            VRCameraCornerMod.LOGGER.error("Failed to load config", e);
            instance = new CornerConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(instance));
        } catch (IOException e) {
            VRCameraCornerMod.LOGGER.error("Failed to save config", e);
        }
    }
}
