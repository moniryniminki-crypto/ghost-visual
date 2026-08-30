package com.ghostvisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public class GhostVisualClient implements ClientModInitializer {
    public static ConfigManager CONFIG;

    @Override
    public void onInitializeClient() {
        CONFIG = ConfigManager.load();
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onHudRender(MatrixStack matrices, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        TextRenderer tr = client.textRenderer;
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        double time = (client.world != null) ? client.world.getTime() + tickDelta : client.player.age + tickDelta;
        float pulse = (float)(1.0 + Math.sin(time / 8.0) * 0.12);

        int x = width/2 - 96;
        int y = height - 84;

        // Background panel with soft blur look
        int panelW = 196;
        int panelH = 52;
        DrawableHelper.fill(matrices, x - 10, y - 10, x + panelW + 10, y + panelH + 10, 0x66001122);

        // Title
        tr.drawWithShadow(matrices, "Ghost Visual", x, y - 2, 0xFFD9F1FF);

        // Pulse HP bar (demo)
        int baseWidth = 160;
        int pulseWidth = Math.max(6, (int)(baseWidth * pulse * (0.6 + 0.4 * CONFIG.hudScale)));
        int barX = x + 6;
        int barY = y + 16;
        DrawableHelper.fill(matrices, barX, barY, barX + baseWidth, barY + 12, 0x22000000);
        int pulseColor = 0xFF4FD1FF;
        DrawableHelper.fill(matrices, barX, barY, barX + pulseWidth, barY + 12, pulseColor);

        // Small labels
        tr.draw(matrices, "HP", barX, barY - 10, 0xBFE6F8FF);
        String cfgText = String.format("Opacity: %.2f  Scale: %.2f", CONFIG.hudOpacity, CONFIG.hudScale);
        tr.draw(matrices, cfgText, barX + baseWidth - tr.getWidth(cfgText), barY - 10, 0x99E6F8FF);

        // Demo quickbar icons
        int slotStartX = x + (panelW/2) - (CONFIG.quickbarSlots*18);
        for (int i = 0; i < CONFIG.quickbarSlots; i++) {
            int sx = slotStartX + i * 36;
            int sy = y + 30;
            int col = 0x22000000;
            DrawableHelper.fill(matrices, sx, sy, sx + 32, sy + 32, col);
            tr.drawWithShadow(matrices, Integer.toString(i+1), sx + 12, sy + 8, 0xFFDEE9FF);
        }
    }
}
