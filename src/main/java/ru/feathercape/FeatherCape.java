package ru.feathercape;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.level.ServerPlayer;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class FeatherCape implements ModInitializer {
    public static final String MOD_ID = "feathercape";
    public static final int MAX_GIF_BYTES = 512 * 1024;
    public static net.minecraft.util.Identifier id(String path) { return net.minecraft.util.Identifier.of(MOD_ID, path); }

    public record CapePayload(String player, byte[] gif) implements net.minecraft.network.packet.CustomPayload {
        public static final net.minecraft.network.packet.CustomPayload.Id<CapePayload> ID =
            new net.minecraft.network.packet.CustomPayload.Id<>(id("cape"));
        public static final net.minecraft.network.codec.PacketCodec<net.minecraft.network.PacketByteBuf, CapePayload> CODEC =
            net.minecraft.network.codec.PacketCodec.of(
                (v,b) -> { b.writeString(v.player,64); b.writeByteArray(v.gif); },
                b -> new CapePayload(b.readString(64), b.readByteArray(MAX_GIF_BYTES)));
        public net.minecraft.network.packet.CustomPayload.Id<? extends net.minecraft.network.packet.CustomPayload> getId(){ return ID; }
    }
    public record ClearCapePayload(String player) implements net.minecraft.network.packet.CustomPayload {
        public static final net.minecraft.network.packet.CustomPayload.Id<ClearCapePayload> ID =
            new net.minecraft.network.packet.CustomPayload.Id<>(id("clear"));
        public static final net.minecraft.network.codec.PacketCodec<net.minecraft.network.PacketByteBuf, ClearCapePayload> CODEC =
            net.minecraft.network.codec.PacketCodec.of(
                (v,b)->b.writeString(v.player,64), b->new ClearCapePayload(b.readString(64)));
        public net.minecraft.network.packet.CustomPayload.Id<? extends net.minecraft.network.packet.CustomPayload> getId(){ return ID; }
    }

    @Override public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(CapePayload.ID, CapePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CapePayload.ID, CapePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClearCapePayload.ID, ClearCapePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClearCapePayload.ID, ClearCapePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CapePayload.ID, (payload, ctx) -> {
            if (payload.gif().length > MAX_GIF_BYTES) return;
            ServerPlayer sender = ctx.player();
            if (!sender.getGameProfile().name().equals(payload.player())) return;
            try {
                Path dir = sender.serverLevel().getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("feathercape");
                Files.createDirectories(dir);
                Files.write(dir.resolve(sender.getGameProfile().id()+".gif"), payload.gif(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) { e.printStackTrace(); }
            for (ServerPlayer p : sender.serverLevel().players()) {
                if (ServerPlayNetworking.canSend(p, CapePayload.ID))
                    ServerPlayNetworking.send(p, payload);
            }
        });
    }
}
