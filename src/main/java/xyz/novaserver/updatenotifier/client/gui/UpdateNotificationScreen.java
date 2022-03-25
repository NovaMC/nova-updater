package xyz.novaserver.updatenotifier.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import xyz.novaserver.updatenotifier.client.UpdateNotifier;
import xyz.novaserver.updatenotifier.util.Storage;
import xyz.novaserver.updatenotifier.util.UpdateData;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Environment(EnvType.CLIENT)
public class UpdateNotificationScreen extends Screen {
    private final Screen parent;

    public UpdateNotificationScreen(Screen parent) {
        super(new TranslatableText("title.updatenotifier.update"));
        this.parent = parent;
    }

    protected static int row(int index) {
        return 40 + index * 13;
    }

    protected void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 120, this.height / 6 + 128, 100, 20, new TranslatableText("option.updatenotifier.later").formatted(Formatting.WHITE), (button) -> {
            UpdateNotifier.UPDATE_SCREEN_SHOWN = true;
            this.client.setScreen(this.parent);
        }));

        this.addDrawableChild(new ButtonWidget(this.width / 2 + 20, this.height / 6 + 128, 100, 20, new TranslatableText("option.updatenotifier.yes").formatted(Formatting.AQUA), (button) -> {
            File file = Storage.getStorageDirectory().resolve(Storage.UPDATER_FILENAME).toFile();
            UpdateData.Downloader download = new UpdateData.Downloader(UpdateNotifier.UPDATE_DATA.getDownloadUrl(), file);

            if (UpdateNotifier.UPDATE_DATA.getDownloadUrl() != null) {
                ExecutorService service = Executors.newFixedThreadPool(2);
                boolean completed = false;
                try {
                    completed = service.submit(download).get();
                } catch (Exception e) {
                    UpdateNotifier.logger.error("Encountered an error while downloading the installer:", e);
                }
                if (completed) {
                    try {
                        String cmd = "java -jar \"" + file.getAbsolutePath() + "\" --install " + UpdateNotifier.UPDATE_DATA.getPackEdition() + " --directory \"" + FabricLoader.getInstance().getGameDir().toString() + "\"";
                        UpdateNotifier.logger.info("Command used to run installer:\n" + cmd);
                        Runtime.getRuntime().exec(cmd);
                    } catch (IOException e) {
                        UpdateNotifier.logger.error("An I/O error occurred while attempting to start the installer:", e);
                    }
                }
            }
            this.client.scheduleStop();
        }));
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, new TranslatableText("description.updatenotifier.updateAvailable"), this.width / 2, row(1), 5636095);
        for (int i = 1; i <= 2; i++) {
            drawCenteredText(matrices, this.textRenderer, new TranslatableText("description.updatenotifier.update" + i), this.width / 2, row(i + 2), 16777215);
        }
        drawCenteredText(matrices, this.textRenderer, new TranslatableText("description.updatenotifier.updateDownload"), this.width / 2, row(6), 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
