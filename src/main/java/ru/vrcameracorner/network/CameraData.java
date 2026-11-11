package ru.vrcameracorner.network;

/**
 * Position and orientation of another player's camera, received from the server.
 */
public final class CameraData {
    public final String ownerName;
    public final double x, y, z;
    public final float qx, qy, qz, qw;
    public final long timestamp;

    public CameraData(String ownerName, double x, double y, double z, float qx, float qy, float qz, float qw) {
        this.ownerName = ownerName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.qx = qx;
        this.qy = qy;
        this.qz = qz;
        this.qw = qw;
        this.timestamp = System.currentTimeMillis();
    }

    public float[] toYawPitch() {
        // quaternion to Minecraft yaw/pitch (degrees)
        final double sinYaw = 2.0 * (qw * qy + qz * qx);
        final double cosYaw = 1.0 - 2.0 * (qx * qx + qy * qy);
        final float yaw = (float) Math.toDegrees(Math.atan2(sinYaw, cosYaw));

        final double sinPitch = 2.0 * (qw * qx - qy * qz);
        final float pitch = (float) Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, sinPitch))));

        return new float[]{yaw, pitch};
    }
}
