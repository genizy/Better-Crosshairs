package breadbb.better_crosshairs.client;

import breadbb.better_crosshairs.client.config.CrosshairConfig;
import net.fabricmc.api.ClientModInitializer;

public class Better_crosshairsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CrosshairConfig.get();
    }
}
