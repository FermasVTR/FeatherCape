package ru.feathercape.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CapeManager {
    private static final Map<String, Animation> CAPES = new ConcurrentHashMap<>();

    public static void put(String player, byte[] gif) {
        try {
            CAPES.put(player, new Animation(gif));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void remove(String player) {
        CAPES.remove(player);
    }

    public static Animation get(String player) {
        return CAPES.get(player);
    }

    public static void tick() {
        CAPES.values().forEach(Animation::tick);
    }

    public static final class Animation {
        private final List<NativeImage> frames = new ArrayList<>();
        private final List<Integer> delays = new ArrayList<>();
        private int index;
        private long next;
        private Identifier textureId;

        Animation(byte[] gif) throws Exception {
            try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(gif))) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
                if (!readers.hasNext()) throw new IllegalArgumentException("Not a GIF");
                ImageReader r = readers.next();
                r.setInput(in, false, false);
                int count = r.getNumImages(true);
                if (count > 60) count = 60;
                for (int i = 0; i < count; i++) {
                    BufferedImage b = r.read(i);
                    if (b.getWidth() != 64 || b.getHeight() != 32)
                        throw new IllegalArgumentException("Cape GIF must be exactly 64x32");
                    NativeImage n = new NativeImage(NativeImage.Format.RGBA, 64, 32, false);
                    for (int y = 0; y < 32; y++) for (int x = 0; x < 64; x++) {
                        int argb = b.getRGB(x, y);
                        n.setColorArgb(x, y, argb);
                    }
                    frames.add(n);
                    int delay = 10;
                    try {
                        Object meta = r.getImageMetadata(i);
                        // Java's GIF metadata API varies; 10 ticks is the safe fallback.
                    } catch (Exception ignored) {}
                    delays.add(delay);
                }
                r.dispose();
            }
            if (frames.isEmpty()) throw new IllegalArgumentException("GIF has no frames");
            upload();
        }

        void tick() {
            if (MinecraftClient.getInstance().getTickDelta() < -1) return;
            long now = System.currentTimeMillis();
            if (now < next) return;
            index = (index + 1) % frames.size();
            upload();
            next = now + delays.get(index) * 50L;
        }

        private void upload() {
            MinecraftClient mc = MinecraftClient.getInstance();
            NativeImage copy = copy(frames.get(index));
            NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "feathercape", copy);
            textureId = mc.getTextureManager().registerDynamicTexture("feathercape", tex);
        }

        public Identifier texture() { return textureId; }

        private static NativeImage copy(NativeImage src) {
            NativeImage n = new NativeImage(NativeImage.Format.RGBA, src.getWidth(), src.getHeight(), false);
            for (int y=0; y<src.getHeight(); y++)
                for (int x=0; x<src.getWidth(); x++)
                    n.setColorArgb(x, y, src.getColorArgb(x, y));
            return n;
        }
    }

    private CapeManager() {}
}
