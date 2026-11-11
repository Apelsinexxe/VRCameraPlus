package ru.vrcameracorner;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VRCameraCornerMod implements ClientModInitializer {
    public static final String MOD_ID = "vrcamera_corner";
    public static final Logger LOGGER = LoggerFactory.getLogger("VRCameraCorner");

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("VRCameraCorner initializing");
        CornerConfig.load();
        ru.vrcameracorner.network.CameraTracker.getInstance().register();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                CornerCommand.register(dispatcher));

        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vrcamera_corner.config",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (configKey.consumeClick()) {
                mc.gui.setScreen(new CornerConfigScreen(mc.gui.screen()));
            }
        });

        // settings button in the bottom-left corner of the pause menu
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (screen instanceof PauseScreen) {
                Screens.getWidgets(screen).add(Button.builder(
                        Component.translatable("vrcamera_corner.config.title"),
                        btn -> client.gui.setScreen(new CornerConfigScreen(screen)))
                        .bounds(5, h - 25, 150, 20).build());
            }
        });

        LOGGER.info("VRCameraCorner initialized");
    }

    public static void openConfigScreen() {
        final Minecraft mc = Minecraft.getInstance();
        mc.gui.setScreen(new CornerConfigScreen(mc.gui.screen()));
    }
}
