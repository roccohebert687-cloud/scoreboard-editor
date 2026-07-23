package com.scoreboardeditor.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.scoreboardeditor.ScoreboardEditorMod;
import com.scoreboardeditor.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the collection of {@link Profile} objects.
 * Profiles are persisted to {@code config/scoreboard-editor-profiles.json}.
 */
public class ProfileManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PROFILES_FILE = "scoreboard-editor-profiles.json";

    private final Path profilesPath;
    private final ConfigManager configManager;
    private List<Profile> profiles = new ArrayList<>();

    public ProfileManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.profilesPath = FabricLoader.getInstance().getConfigDir().resolve(PROFILES_FILE);
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------

    public void load() {
        if (!Files.exists(profilesPath)) {
            profiles = createDefaults();
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(profilesPath, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<Profile>>() {}.getType();
            List<Profile> loaded = GSON.fromJson(reader, listType);
            if (loaded != null && !loaded.isEmpty()) {
                profiles = loaded;
            } else {
                profiles = createDefaults();
            }
        } catch (IOException e) {
            ScoreboardEditorMod.LOGGER.error("Failed to load profiles: {}", e.getMessage());
            profiles = createDefaults();
        }
    }

    public void save() {
        try {
            Files.createDirectories(profilesPath.getParent());
            try (Writer writer = Files.newBufferedWriter(profilesPath, StandardCharsets.UTF_8)) {
                GSON.toJson(profiles, writer);
            }
        } catch (IOException e) {
            ScoreboardEditorMod.LOGGER.error("Failed to save profiles: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Profile management
    // ------------------------------------------------------------------

    public List<Profile> getProfiles() {
        return profiles;
    }

    public Profile getActiveProfile() {
        String activeName = configManager.getConfig().activeProfile;
        for (Profile p : profiles) {
            if (p.name.equals(activeName)) return p;
        }
        // Fallback: return first profile
        return profiles.isEmpty() ? createFallback() : profiles.get(0);
    }

    public void setActiveProfile(String name) {
        configManager.getConfig().activeProfile = name;
        configManager.save();
    }

    public Profile getProfile(String name) {
        for (Profile p : profiles) {
            if (p.name.equals(name)) return p;
        }
        return null;
    }

    public Profile createProfile(String name) {
        Profile existing = getProfile(name);
        if (existing != null) return existing;
        Profile p = new Profile(name);
        profiles.add(p);
        save();
        return p;
    }

    public boolean deleteProfile(String name) {
        boolean removed = profiles.removeIf(p -> p.name.equals(name));
        if (removed) {
            // Switch active if deleted
            if (configManager.getConfig().activeProfile.equals(name)) {
                configManager.getConfig().activeProfile =
                        profiles.isEmpty() ? "Default" : profiles.get(0).name;
                configManager.save();
            }
            save();
        }
        return removed;
    }

    public boolean renameProfile(String oldName, String newName) {
        if (newName == null || newName.isBlank()) return false;
        if (getProfile(newName) != null) return false;
        Profile p = getProfile(oldName);
        if (p == null) return false;
        p.name = newName;
        if (configManager.getConfig().activeProfile.equals(oldName)) {
            configManager.getConfig().activeProfile = newName;
            configManager.save();
        }
        save();
        return true;
    }

    // ------------------------------------------------------------------
    // Defaults
    // ------------------------------------------------------------------

    private List<Profile> createDefaults() {
        List<Profile> list = new ArrayList<>();
        list.add(Profile.createDefault());
        list.add(Profile.createDonutSmpPreset());
        list.add(Profile.createHypixelPreset());
        return list;
    }

    private Profile createFallback() {
        Profile p = Profile.createDefault();
        profiles.add(p);
        save();
        return p;
    }
}
