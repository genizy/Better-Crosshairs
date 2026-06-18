package breadbb.better_crosshairs.client.mixins;

import breadbb.better_crosshairs.client.config.CrosshairConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class GuiMixin {

    private static final int[][] BETTER_CROSSHAIRS$OUTLINE_OFFSETS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    @WrapOperation(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    ordinal = 0
            )
    )
    private void betterCrosshairs$drawCrosshair(GuiGraphicsExtractor extractor, RenderPipeline pipeline, Identifier sprite,
                                                int x, int y, int width, int height, Operation<Void> original) {
        CrosshairConfig cfg = CrosshairConfig.get();
        if (!cfg.enabled) {
            original.call(extractor, pipeline, sprite, x, y, width, height);
            return;
        }

        Identifier id = cfg.spriteId(betterCrosshairs$canAttack());
        int size = cfg.size;
        int drawX = (extractor.guiWidth() - size) / 2 + cfg.offsetX;
        int drawY = (extractor.guiHeight() - size) / 2 + cfg.offsetY;

        int opacity = (cfg.shieldFade && betterCrosshairs$targetBlocking()) ? cfg.shieldFadeOpacity : cfg.opacity;

        if (cfg.outline) {
            int thickness = Math.max(1, Math.round(size / 15.0F));
            int outlineColor = cfg.outlineArgb(opacity);
            for (int[] d : BETTER_CROSSHAIRS$OUTLINE_OFFSETS) {
                extractor.blitSprite(RenderPipelines.GUI_TEXTURED, id,
                        drawX + d[0] * thickness, drawY + d[1] * thickness, size, size, outlineColor);
            }
        }

        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, id, drawX, drawY, size, size, cfg.argb(opacity));
    }

    @WrapOperation(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    ordinal = 1
            )
    )
    private void betterCrosshairs$drawIndicatorFull(GuiGraphicsExtractor extractor, RenderPipeline pipeline, Identifier sprite,
                                                    int x, int y, int width, int height, Operation<Void> original) {
        CrosshairConfig cfg = CrosshairConfig.get();
        if (!cfg.indicatorEnabled) {
            return;
        }
        float scale = cfg.indicatorScaleFactor();
        int w = Math.max(1, Math.round(width * scale));
        int h = Math.max(1, Math.round(height * scale));
        int dx = x + (width - w) / 2 + cfg.indicatorOffsetX;
        int dy = y + (height - h) / 2 + cfg.indicatorOffsetY;

        if (cfg.indicatorTinted()) {
            extractor.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, dx, dy, w, h, cfg.indicatorArgb());
        } else {
            original.call(extractor, pipeline, sprite, dx, dy, w, h);
        }
    }

    @WrapOperation(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
                    ordinal = 2
            )
    )
    private void betterCrosshairs$drawIndicatorBackground(GuiGraphicsExtractor extractor, RenderPipeline pipeline, Identifier sprite,
                                                          int x, int y, int width, int height, Operation<Void> original) {
        CrosshairConfig cfg = CrosshairConfig.get();
        if (!cfg.indicatorEnabled) {
            return;
        }
        float scale = cfg.indicatorScaleFactor();
        int w = Math.max(1, Math.round(width * scale));
        int h = Math.max(1, Math.round(height * scale));
        int dx = x + (width - w) / 2 + cfg.indicatorOffsetX;
        int dy = y + (height - h) / 2 + cfg.indicatorOffsetY;
        original.call(extractor, pipeline, sprite, dx, dy, w, h);
    }

    @WrapOperation(
            method = "extractCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V"
            )
    )
    private void betterCrosshairs$drawIndicatorProgress(GuiGraphicsExtractor extractor, RenderPipeline pipeline, Identifier sprite,
                                                        int texW, int texH, int u, int v, int x, int y, int blitW, int blitH,
                                                        Operation<Void> original) {
        CrosshairConfig cfg = CrosshairConfig.get();
        if (!cfg.indicatorEnabled) {
            return;
        }
        float scale = cfg.indicatorScaleFactor();
        int newTexW = Math.max(1, Math.round(texW * scale));
        int newTexH = Math.max(1, Math.round(texH * scale));
        int newBlitW = Math.max(0, Math.round(blitW * scale));
        int newBlitH = Math.max(1, Math.round(blitH * scale));
        int dx = x + (texW - newTexW) / 2 + cfg.indicatorOffsetX;
        int dy = y + (texH - newTexH) / 2 + cfg.indicatorOffsetY;
        original.call(extractor, pipeline, sprite, newTexW, newTexH, u, v, dx, dy, newBlitW, newBlitH);
    }

    private static boolean betterCrosshairs$canAttack() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return false;
        }
        if (player.getAttackStrengthScale(0.0F) < 1.0F) {
            return false;
        }
        return mc.crosshairPickEntity instanceof LivingEntity living && living.isAlive();
    }

    private static boolean betterCrosshairs$targetBlocking() {
        return Minecraft.getInstance().crosshairPickEntity instanceof LivingEntity living && living.isBlocking();
    }
}
