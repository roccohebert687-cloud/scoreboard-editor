package com.scoreboardeditor.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scoreboardeditor.ScoreboardEditorMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and saves the global ModConfig to disk as JSON.
 */
public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "scoreboard-editor.json";

    private final Path configPath;
    private ModConfig config;

    public ConfigManager() {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        this.config = new ModConfig();
    }

    public void load() {
        if (!Files.exists(configPath)) {
            config = new ModConfig();
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
            if (loaded != null) {
                config = loaded;
            }
        } catch (IOException e) {
            ScoreboardEditorMod.LOGGER.error("Failed to load config: {}", e.getMessage());
            config = new ModConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            ScoreboardEditorMod.LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    public ModConfig getConfig() {
        return config;
    }

    public void setConfig(ModConfig config) {
        this.config = config;
    }
}
