package xyz.novaserver.updatenotifier.util;

import java.io.File;
import java.nio.file.Path;

public class Storage {
    public static String UPDATER_FILENAME = "Nova-Installer.jar";

    public static Path getStorageDirectory() {
        return getAppDataDirectory().resolve(getStorageDirectoryName());
    }

    public static Path getAppDataDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            return new File(System.getenv("APPDATA")).toPath();
        else if (os.contains("mac"))
            return new File(System.getProperty("user.home") + "/Library/Application Support").toPath();
        else if (os.contains("nux"))
            return new File(System.getProperty("user.home")).toPath();
        else
            return new File(System.getProperty("user.dir")).toPath();
    }

    public static String getStorageDirectoryName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac"))
            return "nova-installer";
        else
            return ".nova-installer";
    }
}
