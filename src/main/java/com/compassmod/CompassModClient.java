package com.compassmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class CompassModClient implements ClientModInitializer {

    public static KeyMapping toggleCompassKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category compassCategory = KeyMapping.Category.register(
            ResourceLocation.fromNamespaceAndPath("compassmod", "keys")
        );

        toggleCompassKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.compassmod.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            compassCategory
        ));

        HudRenderCallback.EVENT.register((drawContext, deltaTracker) ->
            CompassHud.render(drawContext, deltaTracker.getGameTimeDeltaPartialTick(true))
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            long handle = GLFW.glfwGetCurrentContext();

            boolean ctrlHeld = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                            || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
            boolean altHeld  = GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS
                            || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;

            while (toggleCompassKey.consumeClick()) {
                if (ctrlHeld && altHeld) {
                    CompassHud.cycleMode();
                    client.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                            "§6[Compass] §fMode: §e" + CompassHud.getModeName()
                        ), true
                    );
                }
            }
        });

        CompassCommand.register();
        CompassMod.LOGGER.info("CompassMod client initialized!");
    }
}
