package breadbb.better_crosshairs.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CrosshairConfig {

    public static final String DEFAULT_NORMAL_SPRITE = "minecraft:hud/crosshair";
    public static final String DEFAULT_ATTACK_SPRITE = "better_crosshairs:hud/crosshairactive";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("better_crosshairs.json");

    private static CrosshairConfig instance;

    public boolean enabled = true;
    public int offsetX = 0;
    public int offsetY = 0;
    public int size = 15;
    public int opacity = 100;
    public String color = "#FFFFFF";
    public boolean outline = false;
    public String outlineColor = "#000000";
    public String normalSprite = DEFAULT_NORMAL_SPRITE;
    public String attackSprite = DEFAULT_ATTACK_SPRITE;

    public boolean shieldFade = true;
    public int shieldFadeOpacity = 25;

    public boolean indicatorEnabled = true;
    public int indicatorOffsetX = 0;
    public int indicatorOffsetY = 0;
    public int indicatorScale = 100;
    public int indicatorOpacity = 100;
    public String indicatorColor = "#FFFFFF";

    public static CrosshairConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static CrosshairConfig load() {
        try {
            if (Files.exists(PATH)) {
                String json = Files.readString(PATH, StandardCharsets.UTF_8);
                CrosshairConfig loaded = GSON.fromJson(json, CrosshairConfig.class);
                if (loaded != null) {
                    loaded.clamp();
                    instance = loaded;
                    return loaded;
                }
            }
        } catch (Exception _) {
        }
        CrosshairConfig fresh = new CrosshairConfig();
        instance = fresh;
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException _) {
        }
    }

    public void clamp() {
        size = Math.clamp(size, 1, 128);
        opacity = Math.clamp(opacity, 0, 100);
        offsetX = Math.clamp(offsetX, -512, 512);
        offsetY = Math.clamp(offsetY, -512, 512);
        if (isBlank(normalSprite)) normalSprite = DEFAULT_NORMAL_SPRITE;
        if (isBlank(attackSprite)) attackSprite = DEFAULT_ATTACK_SPRITE;
        if (isBlank(color)) color = "#FFFFFF";
        if (isBlank(outlineColor)) outlineColor = "#000000";

        shieldFadeOpacity = Math.clamp(shieldFadeOpacity, 0, 100);

        indicatorScale = Math.clamp(indicatorScale, 10, 400);
        indicatorOpacity = Math.clamp(indicatorOpacity, 0, 100);
        indicatorOffsetX = Math.clamp(indicatorOffsetX, -512, 512);
        indicatorOffsetY = Math.clamp(indicatorOffsetY, -512, 512);
        if (isBlank(indicatorColor)) indicatorColor = "#FFFFFF";
    }

    public void resetToDefaults() {
        CrosshairConfig d = new CrosshairConfig();
        enabled = d.enabled;
        offsetX = d.offsetX;
        offsetY = d.offsetY;
        size = d.size;
        opacity = d.opacity;
        color = d.color;
        outline = d.outline;
        outlineColor = d.outlineColor;
        normalSprite = d.normalSprite;
        attackSprite = d.attackSprite;
        shieldFade = d.shieldFade;
        shieldFadeOpacity = d.shieldFadeOpacity;
        indicatorEnabled = d.indicatorEnabled;
        indicatorOffsetX = d.indicatorOffsetX;
        indicatorOffsetY = d.indicatorOffsetY;
        indicatorScale = d.indicatorScale;
        indicatorOpacity = d.indicatorOpacity;
        indicatorColor = d.indicatorColor;
    }

    public Identifier spriteId(boolean canAttack) {
        Identifier id = Identifier.tryParse(canAttack ? attackSprite : normalSprite);
        if (id == null) {
            id = Identifier.tryParse(canAttack ? DEFAULT_ATTACK_SPRITE : DEFAULT_NORMAL_SPRITE);
        }
        return id;
    }

    public int argb() {
        return argb(opacity);
    }

    public int argb(int opacityPercent) {
        return (alpha(opacityPercent) << 24) | parseRgb(color, 0xFFFFFF);
    }

    public int outlineArgb() {
        return outlineArgb(opacity);
    }

    public int outlineArgb(int opacityPercent) {
        return (alpha(opacityPercent) << 24) | parseRgb(outlineColor, 0x000000);
    }

    private static int alpha(int opacityPercent) {
        return Math.clamp(Math.round(opacityPercent * 2.55F), 0, 255);
    }

    public float indicatorScaleFactor() {
        return indicatorScale / 100.0F;
    }

    public int indicatorArgb() {
        return (indicatorAlpha() << 24) | parseRgb(indicatorColor, 0xFFFFFF);
    }

    public boolean indicatorTinted() {
        return parseRgb(indicatorColor, 0xFFFFFF) != 0xFFFFFF || indicatorOpacity < 100;
    }

    private int indicatorAlpha() {
        return Math.clamp(Math.round(indicatorOpacity * 2.55F), 0, 255);
    }

    private static int parseRgb(String s, int fallback) {
        if (s == null) return fallback;
        s = s.trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        } else if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        }
        try {
            return (int) (Long.parseLong(s, 16) & 0xFFFFFFL) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
