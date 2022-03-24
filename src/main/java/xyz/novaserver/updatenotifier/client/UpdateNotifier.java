package xyz.novaserver.updatenotifier.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.novaserver.updatenotifier.util.UpdateData;

@Environment(EnvType.CLIENT)
public class UpdateNotifier implements ClientModInitializer {
    public static final Logger logger = LogManager.getLogger("UpdateNotifier");

    public static boolean UPDATE_SCREEN_SHOWN = false;
    public static UpdateData UPDATE_DATA = new UpdateData();

    @Override
    public void onInitializeClient() {
        UPDATE_DATA.init();
    }
}
