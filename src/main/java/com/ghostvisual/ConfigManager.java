package com.ghostvisual;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    public static Config CONFIG = new Config();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("ghost-visual.json");
    }

    public static Config load() {
        Path p = configPath();
        try {
            if (Files.exists(p)) {
                String txt = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                CONFIG = GSON.fromJson(txt, Config.class);
            } else {
                CONFIG = new Config();
                save();
            }
        } catch (IOException e) {
            e.printStackTrace();
            CONFIG = new Config();
        }
        return CONFIG;
    }

    public static void save() {
        try {
            Path p = configPath();
            Files.createDirectories(p.getParent());
            String txt = GSON.toJson(CONFIG);
            Files.write(p, txt.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Config {
        public boolean chamsEnabled = true;
        public boolean chamsThroughWalls = false;
        public String chamsColor = "#4FD1FF";

        public boolean tracersEnabled = true;
        public String tracerColor = "#8B5CF6";

        public double hudOpacity = 0.92;
        public double hudScale = 1.0;
        public int quickbarSlots = 4;

        public double hudX = 0.5; // normalized (0..1)
        public double hudY = 0.85;

        // other options can be added here
    }
}
