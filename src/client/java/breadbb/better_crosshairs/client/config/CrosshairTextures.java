package breadbb.better_crosshairs.client.config;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CrosshairTextures {

    public enum Type { ATLAS, TEXTURE }

    public static final class Resolved {
        public final Type crosshairtype;
        public final Identifier id;
        public final int width;
        public final int height;

        Resolved(Type crosshairtype, Identifier id, int width, int height) {
            this.crosshairtype = crosshairtype;
            this.id = id;
            this.width = width;
            this.height = height;
        }
    }

    private static final class Entry {
        volatile Identifier id;
        volatile int width;
        volatile int height;
        volatile boolean failed;
    }

    private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "BetterCrosshairs-IO");
        t.setDaemon(true);
        return t;
    });

    private CrosshairTextures() {
    }

    public static Resolved resolve(String source, Identifier fallback) {
        String s = source == null ? "" : source.trim();
        if (isUrl(s) || isFile(s)) {
            Entry entry = CACHE.computeIfAbsent(s, CrosshairTextures::beginLoad);
            if (entry.id != null && !entry.failed) {
                return new Resolved(Type.TEXTURE, entry.id, entry.width, entry.height);
            }
            return new Resolved(Type.ATLAS, fallback, 0, 0);
        }
        Identifier id = Identifier.tryParse(s);
        return new Resolved(Type.ATLAS, id != null ? id : fallback, 0, 0);
    }

    private static boolean isUrl(String s) {
        String l = s.toLowerCase(Locale.ROOT);
        return l.startsWith("http://") || l.startsWith("https://") || l.startsWith("file://");
    }

    private static boolean isFile(String s) {
        String l = s.toLowerCase(Locale.ROOT);
        if (l.startsWith("file:")) return true;
        if (l.endsWith(".png") || l.endsWith(".jpg") || l.endsWith(".jpeg")) return true;
        return s.length() >= 3 && Character.isLetter(s.charAt(0)) && s.charAt(1) == ':'
                && (s.charAt(2) == '\\' || s.charAt(2) == '/');
    }

    private static Entry beginLoad(String source) {
        Entry entry = new Entry();
        IO.submit(() -> {
            try {
                byte[] bytes = readBytes(source);
                NativeImage image = NativeImage.read(new ByteArrayInputStream(bytes));
                int w = image.getWidth();
                int h = image.getHeight();
                Identifier id = Identifier.fromNamespaceAndPath("better_crosshairs", "dynamic/" + key(source));
                Minecraft.getInstance().execute(() -> {
                    try {
                        DynamicTexture texture = new DynamicTexture(() -> "better_crosshairs/" + source, image);
                        Minecraft.getInstance().getTextureManager().register(id, texture);
                        entry.width = w;
                        entry.height = h;
                        entry.id = id;
                    } catch (Throwable t) {
                        image.close();
                        entry.failed = true;
                    }
                });
            } catch (Throwable t) {
                entry.failed = true;
            }
        });
        return entry;
    }

    private static byte[] readBytes(String source) throws IOException {
        if (isUrl(source)) {
            URLConnection connection = URI.create(source).toURL().openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setRequestProperty("User-Agent", "BetterCrosshairs");
            try (InputStream in = connection.getInputStream()) {
                return in.readAllBytes();
            }
        }
        return Files.readAllBytes(filePath(source));
    }

    private static Path filePath(String source) {
        if (source.toLowerCase(Locale.ROOT).startsWith("file:")) {
            return Path.of(URI.create(source));
        }
        Path p = Path.of(source);
        return p.isAbsolute() ? p : FabricLoader.getInstance().getConfigDir().resolve(source);
    }

    private static String key(String source) {
        String lower = source.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lower.length() && sb.length() < 32; i++) {
            char c = lower.charAt(i);
            sb.append((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') ? c : '_');
        }
        return sb + "_" + Integer.toHexString(source.hashCode());
    }
}
