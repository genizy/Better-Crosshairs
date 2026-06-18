package breadbb.better_crosshairs.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class CrosshairConfigScreen extends Screen {

    private static final int PANEL_WIDTH = 260;
    private static final int GAP = 6;
    private static final int LABEL_H = 9;
    private static final int CTRL_H = 16;
    private static final int ROW_STRIDE = 26;
    private static final int BUTTON_H = 18;
    private static final int LABELED_ROWS = 9;

    private final Screen parent;
    private final CrosshairConfig cfg;

    public CrosshairConfigScreen(Screen parent) {
        super(Component.literal("Better Crosshairs"));
        this.parent = parent;
        this.cfg = CrosshairConfig.get();
    }

    @Override
    protected void init() {
        int left = this.width / 2 - PANEL_WIDTH / 2;
        int half = (PANEL_WIDTH - GAP) / 2;
        int rightCol = left + PANEL_WIDTH - half;
        int contentHeight = LABELED_ROWS * ROW_STRIDE + BUTTON_H;
        int y = Math.max(4, this.height / 2 - contentHeight / 2);

        labeledToggle(left, y, half, "Crosshair", "Toggle for the custom crosshair. When off, the vanilla crosshair is used.", () -> cfg.enabled, () -> cfg.enabled = !cfg.enabled);
        labeledToggle(rightCol, y, half, "Outline", "Draws a outline around the crosshair.", () -> cfg.outline, () -> cfg.outline = !cfg.outline);
        y += ROW_STRIDE;

        labeledSlider(left, y, half, "Size", "Crosshair size in pixels (vanilla is 15).", "px", 1, 64, cfg.size, v -> cfg.size = v);
        labeledSlider(rightCol, y, half, "Opacity", "Crosshair transparency.", "%", 0, 100, cfg.opacity, v -> cfg.opacity = v);
        y += ROW_STRIDE;

        labeledSlider(left, y, half, "Offset X", "X offset from the screen center, in pixels.", "px", -50, 50, clamp(cfg.offsetX, -50, 50), v -> cfg.offsetX = v);
        labeledSlider(rightCol, y, half, "Offset Y", "Y offset from the screen center, in pixels.", "px", -50, 50, clamp(cfg.offsetY, -50, 50), v -> cfg.offsetY = v);
        y += ROW_STRIDE;

        labeledText(left, y, half, "Color", "Color of the crosshair in hex format, e.g. #FF0000.", "#RRGGBB", cfg.color, 9, s -> cfg.color = s);
        labeledText(rightCol, y, half, "Outline color", "Color of the outline in hex format, e.g. #000000.", "#RRGGBB", cfg.outlineColor, 9, s -> cfg.outlineColor = s);
        y += ROW_STRIDE;

        labeledText(left, y, half, "Normal sprite", "Sprite for the normal crosshair, e.g. minecraft:hud/crosshair.", "namespace:path", cfg.normalSprite, 256, s -> cfg.normalSprite = s);
        labeledText(rightCol, y, half, "Attack sprite", "Sprite for the crosshair when its good to hit the target.", "namespace:path", cfg.attackSprite, 256, s -> cfg.attackSprite = s);
        y += ROW_STRIDE;

        labeledToggle(left, y, half, "Shield fade", "Fade of crosshair when aiming at someone who is blocking with a shield.", () -> cfg.shieldFade, () -> cfg.shieldFade = !cfg.shieldFade);
        labeledSlider(rightCol, y, half, "Shield opacity", "Crosshair opacity while the target is blocking.", "%", 0, 100, cfg.shieldFadeOpacity, v -> cfg.shieldFadeOpacity = v);
        y += ROW_STRIDE;

        labeledToggle(left, y, half, "Attack indicator", "Show the attack cooldown sword and bar below the crosshair.", () -> cfg.indicatorEnabled, () -> cfg.indicatorEnabled = !cfg.indicatorEnabled);
        labeledText(rightCol, y, half, "Indicator color", "Color of the attack sword in hex format.", "#RRGGBB", cfg.indicatorColor, 9, s -> cfg.indicatorColor = s);
        y += ROW_STRIDE;

        labeledSlider(left, y, half, "Indicator scale", "Attack indicator size, as a percentage of vanilla.", "%", 10, 400, cfg.indicatorScale, v -> cfg.indicatorScale = v);
        labeledSlider(rightCol, y, half, "Indicator opacity", "Attack sword transparency.", "%", 0, 100, cfg.indicatorOpacity, v -> cfg.indicatorOpacity = v);
        y += ROW_STRIDE;

        labeledSlider(left, y, half, "Indicator offset X", "Sword indicator X offset from the screen center, in pixels.", "px", -50, 50, clamp(cfg.indicatorOffsetX, -50, 50), v -> cfg.indicatorOffsetX = v);
        labeledSlider(rightCol, y, half, "Indicator offset Y", "Sword indicator Y offset from the screen center, in pixels.", "px", -50, 50, clamp(cfg.indicatorOffsetY, -50, 50), v -> cfg.indicatorOffsetY = v);
        y += ROW_STRIDE;

        addRenderableWidget(Button.builder(Component.literal("Reset to defaults"), b -> {
            cfg.resetToDefaults();
            rebuildWidgets();
        }).bounds(left, y, half, BUTTON_H).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
                .bounds(rightCol, y, half, BUTTON_H).build());
    }

    private void addLabel(int x, int y, int w, String name) {
        addRenderableWidget(new StringWidget(x, y, w, LABEL_H, Component.literal(name), this.font));
    }

    private void labeledToggle(int x, int y, int w, String name, String tip, BooleanSupplier state, Runnable onToggle) {
        addLabel(x, y, w, name);
        Button button = Button.builder(onOff(state.getAsBoolean()), b -> {
            onToggle.run();
            b.setMessage(onOff(state.getAsBoolean()));
        }).bounds(x, y + LABEL_H, w, CTRL_H).tooltip(Tooltip.create(Component.literal(tip))).build();
        addRenderableWidget(button);
    }

    private void labeledSlider(int x, int y, int w, String name, String tip, String unit, int min, int max, int value, IntConsumer setter) {
        addLabel(x, y, w, name);
        IntSlider slider = new IntSlider(x, y + LABEL_H, w, unit, min, max, value, setter);
        slider.setTooltip(Tooltip.create(Component.literal(tip)));
        addRenderableWidget(slider);
    }

    private void labeledText(int x, int y, int w, String name, String tip, String hint, String value, int maxLength, Consumer<String> setter) {
        addLabel(x, y, w, name);
        EditBox box = new EditBox(this.font, x, y + LABEL_H, w, CTRL_H, Component.literal(name));
        box.setMaxLength(maxLength);
        box.setHint(Component.literal(hint));
        box.setValue(value == null ? "" : value);
        box.setResponder(setter);
        box.setTooltip(Tooltip.create(Component.literal(tip)));
        addRenderableWidget(box);
    }

    private static Component onOff(boolean on) {
        return Component.literal(on ? "On" : "Off");
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.clamp(v, lo, hi);
    }

    @Override
    public void onClose() {
        cfg.clamp();
        cfg.save();
        Minecraft.getInstance().setScreen(parent);
    }

    private static final class IntSlider extends AbstractSliderButton {
        private final int min;
        private final int max;
        private final String unit;
        private final IntConsumer setter;

        IntSlider(int x, int y, int width, String unit, int min, int max, int initial, IntConsumer setter) {
            super(x, y, width, CTRL_H, Component.literal(""), (double) (initial - min) / (double) (max - min));
            this.min = min;
            this.max = max;
            this.unit = unit;
            this.setter = setter;
            updateMessage();
        }

        private int intValue() {
            return (int) Math.round(min + this.value * (max - min));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(intValue() + unit));
        }

        @Override
        protected void applyValue() {
            setter.accept(intValue());
        }
    }
}
