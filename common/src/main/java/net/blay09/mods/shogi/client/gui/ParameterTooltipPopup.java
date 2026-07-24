package net.blay09.mods.shogi.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class ParameterTooltipPopup {
    private static final int HORIZONTAL_PADDING = 12;
    private static final int TEXT_LEFT_PADDING = 6;
    private static final int TEXT_TOP_PADDING = 3;
    private static final int SCREEN_MARGIN = 4;
    private static final int BACKGROUND_COLOR = 0xE0101010;
    private static final int BORDER_COLOR = 0xFF606060;
    private static final int TEXT_COLOR = 0xFFE0E0E0;

    private Component text = Component.empty();

    public void setParameters(List<String> parameters, int parameterIndex) {
        final MutableComponent component = Component.empty();
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                component.append(", ");
            }

            final Component parameter = Component.literal(parameters.get(i));
            component.append(i == parameterIndex ? parameter.copy().withStyle(ChatFormatting.BOLD) : parameter);
        }
        this.text = component;
    }

    public void hide() {
        this.text = Component.empty();
    }

    public boolean isVisible() {
        return !this.text.getString().isEmpty();
    }

    public void extractRenderState(Font font, GuiGraphicsExtractor graphics, int anchorX, int anchorY, int lineHeight, boolean textShadow) {
        if (!isVisible()) {
            return;
        }

        final int width = font.width(this.text) + HORIZONTAL_PADDING;
        final int height = lineHeight + 6;
        int x = Math.min(anchorX, graphics.guiWidth() - width - SCREEN_MARGIN);
        int y = anchorY;
        if (y + height > graphics.guiHeight()) {
            y = anchorY - lineHeight - height - 2;
        }
        x = Math.max(SCREEN_MARGIN, x);
        y = Math.max(SCREEN_MARGIN, y);

        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
        graphics.outline(x, y, width, height, BORDER_COLOR);
        graphics.text(font, this.text, x + TEXT_LEFT_PADDING, y + TEXT_TOP_PADDING, TEXT_COLOR);
    }
}
