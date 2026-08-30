package com.ghostvisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class GhostVisualClient implements ClientModInitializer {
    public static ConfigManager.Config CONFIG;
    public static KeyBinding TOGGLE_KEY;

    @Override
    public void onInitializeClient() {
        CONFIG = ConfigManager.load();

        // Register keybinding: Right Shift
        TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ghostvisual.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.category.ghostvisual"
        ));

        // Listen for toggle key
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_KEY.wasPressed()) {
                if (client.player == null) continue;
                // Toggle screen
                if (client.currentScreen == null) {
                    client.setScreen(new GhostVisualScreen());
                } else {
                    client.setScreen(null);
                }
            }
        });

        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onHudRender(net.minecraft.client.util.math.MatrixStack matrices, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        TextRenderer tr = client.textRenderer;
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        double time = (client.world != null) ? client.world.getTime() + tickDelta : client.player.age + tickDelta;
        float pulse = (float)(1.0 + Math.sin(time / 8.0) * 0.12);

        // Position based on config (normalized)
        int x = (int)(width * CONFIG.hudX) - 128;
        int y = (int)(height * CONFIG.hudY) - 28;

        // Background panel
        int panelW = 256;
        int panelH = 64;
        int bg = (int)(CONFIG.hudOpacity * 255) << 24 | 0x001122;
        DrawableHelper.fill(matrices, x - 12, y - 12, x + panelW + 12, y + panelH + 12, bg);

        // Title
        tr.drawWithShadow(matrices, Text.translatable("ghostvisual.title").getString(), x, y - 8, 0xFFD9F1FF);

        // Pulse HP bar
        int baseWidth = 180;
        int pulseWidth = Math.max(6, (int)(baseWidth * pulse * (0.6 + 0.4 * CONFIG.hudScale)));
        int barX = x + 8;
        int barY = y + 18;
        DrawableHelper.fill(matrices, barX, barY, barX + baseWidth, barY + 12, 0x22000000);
        DrawableHelper.fill(matrices, barX, barY, barX + pulseWidth, barY + 12, 0xFF4FD1FF);

        // Labels
        tr.draw(matrices, Text.translatable("ghostvisual.hud.hp").getString(), barX, barY - 10, 0xBFE6F8FF);
        String cfgText = String.format("%.0f%%  Масштаб: %.2f", CONFIG.hudOpacity * 100.0, CONFIG.hudScale);
        tr.draw(matrices, cfgText, barX + baseWidth - tr.getWidth(cfgText), barY - 10, 0x99E6F8FF);

        // Quickbar demo (clickable visual slots)
        int slotStartX = x + (panelW/2) - (CONFIG.quickbarSlots * 20);
        for (int i = 0; i < CONFIG.quickbarSlots; i++) {
            int sx = slotStartX + i * 40;
            int sy = y + 34;
            int col = 0x22000000;
            DrawableHelper.fill(matrices, sx, sy, sx + 36, sy + 36, col);
            tr.drawWithShadow(matrices, Integer.toString(i+1), sx + 14, sy + 10, 0xFFDEE9FF);
        }
    }

    public static void toggleMenu() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen == null) client.setScreen(new GhostVisualScreen());
        else client.setScreen(null);
    }
}
