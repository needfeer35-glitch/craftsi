package net.craftsi.mod;

import net.craftsi.mod.gui.CraftsiScreen;
import net.craftsi.mod.logic.CraftExecutor;
import net.craftsi.mod.node.NodeGraph;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CraftsiMod implements ClientModInitializer {

    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.craftsi.open",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.craftsi"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new CraftsiScreen());
                }
            }
            if (client.player != null) {
                CraftExecutor.tick(NodeGraph.getInstance(), client.player);
            }
        });
    }
}