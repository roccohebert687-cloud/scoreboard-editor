package com.scoreboardeditor;

import com.scoreboardeditor.config.ConfigManager;
import com.scoreboardeditor.hud.CustomScoreboardRenderer;
import com.scoreboardeditor.input.KeybindHandler;
import com.scoreboardeditor.profile.ProfileManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class ScoreboardEditorMod implements ClientModInitializer {

    public static final String MOD_ID = "scoreboard-editor";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ScoreboardEditorMod instance;

    private ConfigManager configManager;
    private ProfileManager profileManager;
    private KeybindHandler keybindHandler;
    private CustomScoreboardRenderer hudRenderer;

    @Override
    public void onInitializeClient() {
        instance = this;

        LOGGER.info("Initializing Scoreboard Editor...");

        configManager = new ConfigManager();
        configManager.load();

        profileManager = new ProfileManager(configManager);
        profileManager.load();

        keybindHandler = new KeybindHandler();
        keybindHandler.register();

        hudRenderer = new CustomScoreboardRenderer(profileManager);
        hudRenderer.register();

        LOGGER.info("Scoreboard Editor initialized successfully.");
    }

    public static ScoreboardEditorMod getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public CustomScoreboardRenderer getHudRenderer() {
        return hudRenderer;
    }
}
