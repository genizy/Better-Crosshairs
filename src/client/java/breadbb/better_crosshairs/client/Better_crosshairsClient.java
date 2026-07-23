package breadbb.better_crosshairs.client;

import breadbb.better_crosshairs.client.config.CrosshairConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

public class Better_crosshairsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CrosshairConfig.get();
    }

    public static boolean betterCrosshairs$canAttack() {
        CrosshairConfig cfg = CrosshairConfig.get();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return false;
        }
        switch (cfg.attackIdentifier) {
            case ATTACK_COOLDOWN_READY -> {
                return !(player.getAttackStrengthScale(0.0F) < 1.0F);
            }
            case IN_RANGE -> {
                return mc.crosshairPickEntity instanceof LivingEntity living && living.isAlive();
            }
            case BOTH -> {
                return !(player.getAttackStrengthScale(0.0F) < 1.0F) && mc.crosshairPickEntity instanceof LivingEntity living && living.isAlive();
            }
            default -> {
                return !(player.getAttackStrengthScale(0.0F) < 1.0F) && mc.crosshairPickEntity instanceof LivingEntity living && living.isAlive();
            }
        }
    }
}
