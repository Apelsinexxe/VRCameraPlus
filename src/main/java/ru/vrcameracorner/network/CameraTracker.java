package ru.vrcameracorner.network;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import ru.deelter.vrcamera.client.sync.RemoteCameras;
import ru.vrcameracorner.VRCameraCornerMod;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Other players' cameras, read from what VRCamera already heard from the server (RemoteCameras).
 * VRCamera owns the vrcamera:sync channel, a second receiver on it would break its sync, so nothing is listened to here.
 */
public final class CameraTracker {
    private static final CameraTracker INSTANCE = new CameraTracker();

    private Field camerasField;
    private Field nameField;
    private Field positionField;
    private Field rotationField;
    private boolean broken;

    private CameraTracker() {
    }

    public static CameraTracker getInstance() {
        return INSTANCE;
    }

    /**
     * only checks that VRCamera's classes are as expected
     */
    public void register() {
        if (init()) {
            VRCameraCornerMod.LOGGER.info("CameraTracker reads other cameras from VRCamera");
        }
    }

    private boolean init() {
        if (broken) {
            return false;
        }
        if (camerasField != null) {
            return true;
        }
        try {
            camerasField = RemoteCameras.class.getDeclaredField("cameras");
            camerasField.setAccessible(true);
            final Class<?> camera = Class.forName("ru.deelter.vrcamera.client.sync.RemoteCameras$Camera");
            nameField = camera.getDeclaredField("ownerName");
            positionField = camera.getDeclaredField("position");
            rotationField = camera.getDeclaredField("rotation");
            nameField.setAccessible(true);
            positionField.setAccessible(true);
            rotationField.setAccessible(true);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            broken = true;
            VRCameraCornerMod.LOGGER.error("VRCameraCorner: can't read other players' cameras from VRCamera", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, Object> raw() throws IllegalAccessException {
        return (Map<UUID, Object>) camerasField.get(RemoteCameras.INSTANCE);
    }

    public Map<UUID, String> getAvailableCameras() {
        final Map<UUID, String> result = new HashMap<>();
        if (!init()) {
            return result;
        }
        try {
            for (final Map.Entry<UUID, Object> e : raw().entrySet()) {
                final Object name = nameField.get(e.getValue());
                result.put(e.getKey(), name == null ? "?" : name.toString());
            }
        } catch (IllegalAccessException | RuntimeException ex) {
            VRCameraCornerMod.LOGGER.error("VRCameraCorner: reading cameras failed", ex);
        }
        return result;
    }

    /**
     * @return where that camera is now, null if it is gone or not placed yet
     */
    public CameraData getCamera(UUID owner) {
        if (owner == null || !init()) {
            return null;
        }
        try {
            final Object camera = raw().get(owner);
            if (camera == null) {
                return null;
            }
            final Vec3 pos = (Vec3) positionField.get(camera);
            final Quaternionf rot = (Quaternionf) rotationField.get(camera);
            if (pos == null || rot == null) {
                return null;
            }
            final Object name = nameField.get(camera);
            return new CameraData(name == null ? "?" : name.toString(), pos.x, pos.y, pos.z,
                    rot.x, rot.y, rot.z, rot.w);
        } catch (IllegalAccessException | RuntimeException ex) {
            VRCameraCornerMod.LOGGER.error("VRCameraCorner: reading a camera failed", ex);
            return null;
        }
    }
}
