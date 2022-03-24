package xyz.novaserver.updatenotifier.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class JsonUtils {
    public static JsonObject readJsonFromUrl(String url) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8));
        return JsonParser.parseReader(bufferedReader).getAsJsonObject();
    }

    public static JsonObject readJsonFromFile(Path path) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(path.getFileSystem().provider().newInputStream(path), StandardCharsets.UTF_8));
        return JsonParser.parseReader(bufferedReader).getAsJsonObject();
    }
}

