package kaiakk.sphericality.client;

import com.mojang.blaze3d.platform.InputConstants;
import kaiakk.sphericality.network.GravityChangePayload;
import kaiakk.sphericality.network.GravityVariables;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.lwjgl.glfw.GLFW;

public class SphericalityClient implements ClientModInitializer {

    public static KeyMapping increaseGravityKey;
    public static KeyMapping decreaseGravityKey;
    public static KeyMapping resetGravityKey;
    public static KeyMapping quickConfigKey;

    private static int resetCooldownTicks = 0;
    public static boolean serverHasMod = true;
    private static boolean connectedToServer = false;
    private static int handshakeTimeoutTicks = 0;
    private static boolean probeSent = false;

    @Override
    public void onInitializeClient() {
        GravityVariables.load();

        increaseGravityKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.gravitymachine.increase",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UP,
                "category.gravitymachine"
        ));

        decreaseGravityKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.gravitymachine.decrease",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_DOWN,
                "category.gravitymachine"
        ));

        resetGravityKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.gravitymachine.reset",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.gravitymachine"
        ));

        quickConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.gravitymachine.config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "category.gravitymachine"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!connectedToServer && client.player != null) {
                connectedToServer = true;
                serverHasMod = false;
                handshakeTimeoutTicks = 100;
            }

            if (connectedToServer && client.player == null) {
                connectedToServer = false;
                serverHasMod = true;
                handshakeTimeoutTicks = 0;
            }

            if (handshakeTimeoutTicks > 0) handshakeTimeoutTicks--;

                if (!serverHasMod && connectedToServer && !probeSent) {
                    try {
                        ClientPlayNetworking.send(new GravityChangePayload.ClientProbePayload());
                        probeSent = true;
                    } catch (Exception ignored) {}
                }

            if (client.player == null) return;

            var gravityAttr = client.player.getAttribute(Attributes.GRAVITY);
            if (gravityAttr == null) return;

            double current = gravityAttr.getBaseValue();

            if (resetCooldownTicks > 0) resetCooldownTicks--;

            while (increaseGravityKey.consumeClick()) {
                if (!serverHasMod) {
                    client.player.displayClientMessage(Component.literal("Server does not run the Gravity Machine mod — features disabled."), true);
                    continue;
                }
                double target = Mth.clamp(current + GravityVariables.STEP, GravityVariables.MIN_GRAVITY, GravityVariables.MAX_GRAVITY);
                double delta = target - current;
                if (Math.abs(delta) > 1.0E-6) {
                    ClientPlayNetworking.send(new GravityChangePayload(delta));
                    if (GravityVariables.PLAY_SOUNDS && target != GravityVariables.MAX_GRAVITY) {
                        client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
                    }
                    current = target;
                }
            }

            while (decreaseGravityKey.consumeClick()) {
                if (!serverHasMod) {
                    client.player.displayClientMessage(Component.literal("Server does not run the Gravity Machine mod — features disabled."), true);
                    continue;
                }
                double target = Mth.clamp(current - GravityVariables.STEP, GravityVariables.MIN_GRAVITY, GravityVariables.MAX_GRAVITY);
                double delta = target - current;
                if (Math.abs(delta) > 1.0E-6) {
                    ClientPlayNetworking.send(new GravityChangePayload(delta));
                    if (GravityVariables.PLAY_SOUNDS && target != GravityVariables.MIN_GRAVITY) {
                        client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 0.8f);
                    }
                    current = target;
                }
            }

            while (resetGravityKey.consumeClick()) {
                if (!serverHasMod) {
                    client.player.displayClientMessage(Component.literal("Server does not run the Gravity Machine mod — features disabled."), true);
                    continue;
                }
                if (resetCooldownTicks > 0) {
                    client.player.displayClientMessage(
                            Component.literal("Your gravity reset is currently on cooldown!"), true
                    );
                    if (GravityVariables.PLAY_SOUNDS) client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 0.2f);
                } else {
                    double target = Mth.clamp(GravityVariables.VANILLA_GRAVITY, GravityVariables.MIN_GRAVITY, GravityVariables.MAX_GRAVITY);
                    double delta = target - current;
                    if (Math.abs(delta) > 1.0E-6) {
                        ClientPlayNetworking.send(new GravityChangePayload(delta));
                        resetCooldownTicks = GravityVariables.RESET_COOLDOWN;
                        if (GravityVariables.PLAY_SOUNDS) client.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.4f, 2.0f);
                        current = target;
                    }
                }
            }

            while (quickConfigKey.consumeClick()) {
                client.setScreen(kaiakk.sphericality.client.ModMenuIntegration.createConfigScreen(client.screen));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(GravityChangePayload.ServerHandshakePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                serverHasMod = true;
                probeSent = false;
            });
        });
        
        ClientPlayNetworking.registerGlobalReceiver(GravityChangePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (context.player() == null) return;
                var attr = context.player().getAttribute(Attributes.GRAVITY);
                if (attr != null) {
                    attr.setBaseValue(payload.change());
                }
            });
        });

        HudElementRegistry.addLast(
                ResourceLocation.fromNamespaceAndPath("sphericality", "gravity_display"),
                (guiGraphics, tickCounter) -> {
                    Minecraft client = Minecraft.getInstance();
                        if (client.player == null || client.options.hideGui) return;
                        if (connectedToServer && !serverHasMod) return;

                    var gravityAttr = client.player.getAttribute(Attributes.GRAVITY);
                    if (gravityAttr == null) return;

                        double currentGravity = Mth.clamp(
                            gravityAttr.getBaseValue(),
                            GravityVariables.MIN_GRAVITY,
                            GravityVariables.MAX_GRAVITY
                        );

                        double deltaFromVanilla = currentGravity - GravityVariables.VANILLA_GRAVITY;

                        if (GravityVariables.DISPLAY_POSITION != GravityVariables.GravityDisplayPosition.DISABLED) {
                        String currentStr = String.format("Gravity: %.3f", currentGravity);
                        String deltaStr = String.format(
                            "Shift: %s%.3f",
                            deltaFromVanilla >= 0 ? "+" : "",
                            deltaFromVanilla
                        );

                        int screenW = client.getWindow().getGuiScaledWidth();
                        int screenH = client.getWindow().getGuiScaledHeight();
                        int curW = client.font.width(currentStr);
                        int deltaW = client.font.width(deltaStr);

                        int x1 = 10;
                        int y1 = 10;
                        int x2 = 10;
                        int y2 = 20;

                        switch (GravityVariables.DISPLAY_POSITION) {
                            case TOP_LEFT -> { x1 = 10; y1 = 10; x2 = 10; y2 = 20; }
                            case TOP_RIGHT -> { x1 = screenW - curW - 10; x2 = screenW - deltaW - 10; y1 = 10; y2 = 20; }
                            case BOTTOM_RIGHT -> { x1 = screenW - curW - 10; x2 = screenW - deltaW - 10; y1 = screenH - 30; y2 = screenH - 20; }
                            case BOTTOM_LEFT -> { x1 = 10; x2 = 10; y1 = screenH - 30; y2 = screenH - 20; }
                            default -> { x1 = 10; y1 = 10; x2 = 10; y2 = 20; }
                        }

                        guiGraphics.drawString(client.font, currentStr, x1, y1, 0xFFFFFFFF, true);

                        int shiftColor = Math.abs(deltaFromVanilla) > 0.001
                            ? 0xFFFFFF00
                            : 0xFFFFFFFF;

                        guiGraphics.drawString(client.font, deltaStr, x2, y2, shiftColor, true);
                        }
                }
        );
    }
}
