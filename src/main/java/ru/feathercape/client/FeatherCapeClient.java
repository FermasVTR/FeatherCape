package ru.feathercape.client;

import ru.feathercape.FeatherCape;
import ru.feathercape.FeatherCape.CapePayload;
import ru.feathercape.FeatherCape.ClearCapePayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.nio.file.Files;

public final class FeatherCapeClient implements ClientModInitializer {
    public static final KeyBinding OPEN = KeyBindingHelper.registerKeyBinding(
        new KeyBinding("key.feathercape.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.feathercape")
    );

    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playC2S().register(CapePayload.ID, CapePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClearCapePayload.ID, ClearCapePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CapePayload.ID, CapePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClearCapePayload.ID, ClearCapePayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(CapePayload.ID, (payload, ctx) ->
            CapeManager.put(payload.player(), payload.gif())
        );
        ClientPlayNetworking.registerGlobalReceiver(ClearCapePayload.ID, (payload, ctx) ->
            CapeManager.remove(payload.player())
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN.wasPressed()) {
                openFile(client);
            }
            CapeManager.tick();
        });
    }

    private static void openFile(MinecraftClient client) {
        Thread.startVirtualThread(() -> {
            FileDialog dialog = new FileDialog((Frame) null, "Choose GIF cape", FileDialog.LOAD);
            dialog.setFilenameFilter((d, name) -> name.toLowerCase().endsWith(".gif"));
            dialog.setVisible(true);
            if (dialog.getFile() == null) return;
            try {
                File file = new File(dialog.getDirectory(), dialog.getFile());
                byte[] data = Files.readAllBytes(file.toPath());
                if (data.length > FeatherCape.MAX_GIF_BYTES) {
                    client.execute(() -> client.player.sendMessage(net.minecraft.text.Text.literal(
                        "GIF too large: max 512 KB"), false));
                    return;
                }
                if (!file.getName().toLowerCase().endsWith(".gif")) return;
                client.execute(() -> {
                    if (client.player != null && ClientPlayNetworking.canSend(CapePayload.ID)) {
                        ClientPlayNetworking.send(new CapePayload(client.player.getGameProfile().name(), data));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
