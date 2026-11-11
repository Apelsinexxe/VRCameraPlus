package ru.vrcameracorner;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import ru.deelter.vrcamera.client.config.CameraConfig;
import ru.deelter.vrcamera.client.config.ScreenOutput;

import java.util.Locale;

public final class CornerCommand {
    private static final int DONE = 1;

    private CornerCommand() {
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommands.literal("vrcamcorner")
                .executes(context -> {
                    final CornerConfig config = CornerConfig.get();
                    context.getSource().sendFeedback(Component.translatable(
                            config.enabled ? "vrcamera_corner.command.status.on" : "vrcamera_corner.command.status.off",
                            Component.translatable(config.isFree() ? "vrcamera_corner.position.free"
                                    : "vrcamera_corner.position." + config.position.name().toLowerCase(Locale.ROOT)),
                            String.format(Locale.ROOT, "%.2f", config.size), config.margin));
                    return DONE;
                });

        root.then(ClientCommands.literal("on").executes(context -> {
            CornerConfig.get().enabled = true;
            CornerConfig.save();
            final CameraConfig camera = CameraConfig.current();
            if (camera.screenOutput != ScreenOutput.WINDOW) {
                camera.screenOutput = ScreenOutput.WINDOW;
                camera.save();
            }
            context.getSource().sendFeedback(Component.translatable("vrcamera_corner.command.enabled"));
            return DONE;
        }));

        root.then(ClientCommands.literal("off").executes(context -> {
            CornerConfig.get().enabled = false;
            CornerConfig.save();
            context.getSource().sendFeedback(Component.translatable("vrcamera_corner.command.disabled"));
            return DONE;
        }));

        root.then(ClientCommands.literal("config").executes(context -> {
            final net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            mc.execute(() -> mc.gui.setScreen(new CornerConfigScreen(null)));
            return DONE;
        }));

        final LiteralArgumentBuilder<FabricClientCommandSource> position = ClientCommands.literal("position");
        for (final CornerConfig.Position value : CornerConfig.Position.values()) {
            final String name = value.name().toLowerCase(Locale.ROOT);
            position.then(ClientCommands.literal(name).executes(context -> {
                CornerConfig.get().position = value;
                CornerConfig.get().unfree();
                CornerConfig.save();
                context.getSource().sendFeedback(Component.translatable("vrcamera_corner.command.position",
                        Component.translatable("vrcamera_corner.position." + name)));
                return DONE;
            }));
        }
        root.then(position);

        root.then(ClientCommands.literal("size")
                .then(ClientCommands.argument("value", FloatArgumentType.floatArg(0.1f, 0.6f)).executes(context -> {
                    final float value = FloatArgumentType.getFloat(context, "value");
                    CornerConfig.get().size = value;
                    CornerConfig.save();
                    context.getSource().sendFeedback(Component.translatable("vrcamera_corner.command.size",
                            String.format(Locale.ROOT, "%.2f", value)));
                    return DONE;
                })));

        dispatcher.register(root);
    }
}
