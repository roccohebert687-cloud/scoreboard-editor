package com.scoreboardeditor.input;

import com.scoreboardeditor.gui.ScoreboardEditorScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Registers and handles the configurable keybind that opens the
 * {@link ScoreboardEditorScreen}.
 *
 * <p>Default key: {@code `} (grave / backtick).
 * The keybind is re-bindable from Minecraft's Controls screen.
 *
 * <p>The key is intentionally suppressed while any text-input screen is
 * open (chat, sign editor, book editor, anvil, command block, etc.) by
 * relying on the standard Fabric keybinding system which already gates
 * {@link KeyBinding#wasPressed()} behind the game being in-focus and no
 * screen being open – except for screens we explicitly open ourselves.
 */
public class KeybindHandler {

    private static final String CATEGORY = "key.categories.scoreboard-editor";

    private KeyBinding openEditorKey;

    /**
     * Registers the keybind with Fabric's {@link KeyBindingHelper} and
     * subscribes to the client tick event to detect presses.
     */
    public void register() {
        openEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.scoreboard-editor.open_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,   // ` key
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    // -----------------------------------------------------------------------
    // Tick handler
    // -----------------------------------------------------------------------

    private void onClientTick(MinecraftClient client) {
        // wasPressed() already returns false when:
        //  - no Minecraft window focus
        //  - a Screen is currently open (e.g. chat, book, sign)
        // So we do not need additional "is text-input screen" checks;
        // the keybind system handles that for us.
        while (openEditorKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new ScoreboardEditorScreen());
            }
        }
    }

    public KeyBinding getOpenEditorKey() {
        return openEditorKey;
    }
}
