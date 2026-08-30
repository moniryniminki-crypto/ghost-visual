package com.ghostvisual;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class GhostVisualScreen extends Screen {
    private final int width = 600;
    private final int height = 360;

    private List<Category> categories = new ArrayList<>();
    private int selectedIndex = 1; // default Visual

    protected GhostVisualScreen() {
        super(Text.literal("Ghost Visual"));
        initCategories();
    }

    private void initCategories() {
        categories.clear();
        Category combat = new Category("ghostvisual.menu.combat", new String[][]{
                {"ghostvisual.option.quickbar", "Описание: Быстрые слоты для предметов и умений"}
        });
        Category visual = new Category("ghostvisual.menu.visual", new String[][]{
                {"ghostvisual.option.chams", "ghostvisual.option.chams.desc"},
                {"ghostvisual.option.tracers", "ghostvisual.option.tracers.desc"},
                {"ghostvisual.option.pulseParticles", "ghostvisual.option.pulseParticles.desc"}
        });
        Category menu = new Category("ghostvisual.menu.menu", new String[][]{
                {"ghostvisual.option.hudOpacity", "ghostvisual.option.hudOpacity.desc"},
                {"ghostvisual.option.hudScale", "ghostvisual.option.hudScale.desc"}
        });
        categories.add(combat);
        categories.add(visual);
        categories.add(menu);
    }

    @Override
    protected void init() {
        super.init();
        // build buttons for category list
        int left = (this.width - width) / 2 + 16;
        int top = (this.height - height) / 2 + 16;

        // category buttons
        for (int i = 0; i < categories.size(); i++) {
            final int idx = i;
            this.addDrawableChild(new ButtonWidget(left, top + i * 26, 120, 20, categories.get(i).getTitle(), (btn) -> {
                selectedIndex = idx;
            }));
        }

        // option toggles on the right side will be created in render -- dynamic
        // Add close button
        this.addDrawableChild(new ButtonWidget((this.width - width) / 2 + width - 100, (this.height - height) / 2 + height - 40, 80, 20, Text.literal("Закрыть"), (btn) -> {
            this.client.setScreen(null);
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // draw background dim
        this.renderBackground(matrices);

        int cx = (this.width - width) / 2;
        int cy = (this.height - height) / 2;

        // panel background
        fill(matrices, cx, cy, cx + width, cy + height, 0xCC001122);

        // draw category list
        int left = cx + 12;
        int top = cy + 12;
        for (int i = 0; i < categories.size(); i++) {
            int y = top + i * 26;
            drawCenteredText(matrices, this.textRenderer, categories.get(i).getTitle(), left + 60, y + 6, selectedIndex == i ? 0xFFD9F1FF : 0xAAB0C8D8);
        }

        // draw selected options on the right
        int rightX = cx + 150;
        int optY = top;
        Category sel = categories.get(selectedIndex);
        for (int i = 0; i < sel.options.length; i++) {
            String key = sel.options[i][0];
            String descKey = sel.options[i][1];
            String label = Text.translatable(key).getString();
            String desc = descKey.startsWith("ghostvisual") ? Text.translatable(descKey).getString() : descKey;

            // simple toggle button
            int btnX = rightX + 12;
            int btnY = optY + i * 36;
            // determine current value from ConfigManager (simple mapping)
            boolean value = getOptionValue(key);
            String btnText = value ? "Вкл" : "Выкл";
            this.textRenderer.drawWithShadow(matrices, label, btnX, btnY, 0xFFFFFF);
            this.textRenderer.draw(matrices, desc, btnX, btnY + 10, 0x99E6F8FF);

            // add a button if not already present (avoid duplicate creation)
            // For simplicity we won't add persistent widget objects here; clicks are handled in mouseClicked
            this.textRenderer.drawWithShadow(matrices, btnText, btnX + 300, btnY, value ? 0x8BFF8B : 0xFF8B8B);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private boolean getOptionValue(String key) {
        switch (key) {
            case "ghostvisual.option.chams":
                return ConfigManager.CONFIG.chamsEnabled;
            case "ghostvisual.option.tracers":
                return ConfigManager.CONFIG.tracersEnabled;
            case "ghostvisual.option.pulseParticles":
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int cx = (this.width - width) / 2;
        int cy = (this.height - height) / 2;
        int rightX = cx + 150;
        int top = cy + 12;
        Category sel = categories.get(selectedIndex);
        for (int i = 0; i < sel.options.length; i++) {
            int btnX = rightX + 12;
            int btnY = top + i * 36;
            int bx1 = btnX + 300;
            int by1 = btnY;
            int bx2 = bx1 + 40;
            int by2 = by1 + 12;
            if (mouseX >= bx1 && mouseX <= bx2 && mouseY >= by1 && mouseY <= by2) {
                // toggle option
                toggleOption(sel.options[i][0]);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void toggleOption(String key) {
        switch (key) {
            case "ghostvisual.option.chams":
                ConfigManager.CONFIG.chamsEnabled = !ConfigManager.CONFIG.chamsEnabled;
                ConfigManager.save();
                break;
            case "ghostvisual.option.tracers":
                ConfigManager.CONFIG.tracersEnabled = !ConfigManager.CONFIG.tracersEnabled;
                ConfigManager.save();
                break;
            default:
                break;
        }
    }

    private static class Category {
        String titleKey;
        String[][] options;
        Category(String titleKey, String[][] options) { this.titleKey = titleKey; this.options = options; }
        public String getTitle() { return Text.translatable(titleKey).getString(); }
    }
}
