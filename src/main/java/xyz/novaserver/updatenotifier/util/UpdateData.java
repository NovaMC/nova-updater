package xyz.novaserver.updatenotifier.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionComparisonOperator;
import net.minecraft.client.MinecraftClient;
import xyz.novaserver.updatenotifier.client.UpdateNotifier;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class UpdateData {

    private SemanticVersion currentVersion;
    private SemanticVersion latestVersion;
    private String packEdition;

    private String installerApiUrl;
    private String modpackBaseUrl;

    private String downloadUrl;

    public void init() {
        // Read and parse version data from meta.json in game directory
        CompletableFuture.supplyAsync(() -> {
            try {
                return JsonUtils.readJsonFromFile(FabricLoader.getInstance().getGameDir().resolve("meta.json"));
            } catch (IOException e) {
                UpdateNotifier.logger.error("Failed to get current version because of an I/O error", e);
            }
            return null;
        }).thenAcceptAsync(localJson -> {
            if (localJson != null) {
                this.packEdition = localJson.get("edition").getAsString();
                this.installerApiUrl = localJson.get("installer-api-url").getAsString();
                this.modpackBaseUrl = localJson.get("modpack-base-url").getAsString();

                String version = localJson.get("version").getAsString();
                try {
                    this.currentVersion = SemanticVersion.parse(version);
                } catch (VersionParsingException e) {
                    UpdateNotifier.logger.error("Failed to parse current version data", e);
                }
            }

            getModpackMeta();
            getInstaller();

        }, MinecraftClient.getInstance());
    }

    public boolean isUpdateAvailable() {
        if (currentVersion == null || latestVersion == null) {
            return false;
        }
        return VersionComparisonOperator.LESS.test(currentVersion, latestVersion);
    }

    public String getPackEdition() {
        return packEdition;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    private void getModpackMeta() {
        // Read and parse version data from remote json
        CompletableFuture.supplyAsync(() -> {
            try {
                return JsonUtils.readJsonFromUrl(modpackBaseUrl + packEdition + "/meta.json");
            } catch (IOException e) {
                UpdateNotifier.logger.error("Failed to read json because of an I/O error", e);
            }
            return null;
        }).thenAcceptAsync(metaJson -> {
            if (metaJson != null) {
                String version = metaJson.get("version").getAsString();
                try {
                    this.latestVersion = SemanticVersion.parse(version);
                } catch (VersionParsingException e) {
                    UpdateNotifier.logger.error("Failed to read json because of an I/O error", e);
                }
            }
        }, MinecraftClient.getInstance());
    }

    private void getInstaller() {
        // Get download link to latest installer jar
        CompletableFuture.supplyAsync(() -> {
            try {
                return JsonUtils.readJsonFromUrl(installerApiUrl);
            } catch (IOException e) {
                UpdateNotifier.logger.error("Failed to read json because of an I/O error", e);
            }
            return null;
        }).thenAcceptAsync(apiJson -> {
            if (apiJson != null) {
                apiJson.getAsJsonArray("assets").forEach(asset -> {
                    if (asset.getAsJsonObject().get("name").getAsString().endsWith(".jar")) {
                        this.downloadUrl = asset.getAsJsonObject().get("browser_download_url").getAsString();
                    }
                });
            }
        }, MinecraftClient.getInstance());
    }

    public static class Downloader implements Callable<Boolean> {
        private final String url;
        private final File file;

        public Downloader(String url, File file) {
            this.url = url;
            this.file = file;
        }

        @Override
        public Boolean call() throws Exception {
            URL url = new URL(this.url);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            long filesize = connection.getContentLengthLong();
            if (filesize == -1) {
                throw new Exception("Content length must not be -1 (unknown)!");
            }
            long totalDataRead = 0;
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream())) {
                FileOutputStream fos = new FileOutputStream(file);
                try (BufferedOutputStream out = new BufferedOutputStream(fos, 1024)) {
                    byte[] data = new byte[1024];
                    int i;
                    while ((i = in.read(data, 0, 1024)) >= 0) {
                        totalDataRead = totalDataRead + i;
                        out.write(data, 0, i);
                    }
                }
            }
            return true;
        }
    }
}
