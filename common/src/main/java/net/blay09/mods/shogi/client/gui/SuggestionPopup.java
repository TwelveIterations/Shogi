package net.blay09.mods.shogi.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;

import java.util.List;

public class SuggestionPopup {
    private static final int DEFAULT_MAX_VISIBLE_SUGGESTIONS = 8;
    private static final int HORIZONTAL_PADDING = 12;
    private static final int TEXT_LEFT_PADDING = 6;
    private static final int TEXT_TOP_PADDING = 2;
    private static final int SCREEN_MARGIN = 4;
    private static final int BACKGROUND_COLOR = 0xE0101010;
    private static final int BORDER_COLOR = 0xFF606060;
    private static final int SELECTED_COLOR = 0xFF2E5C9A;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private final int rowHeight;
    private final int maxVisibleSuggestions;
    private List<String> suggestions = List.of();
    private int selectedIndex;
    private int firstVisibleIndex;
    private int x;
    private int y;
    private int width;

    public SuggestionPopup(int rowHeight) {
        this(rowHeight, DEFAULT_MAX_VISIBLE_SUGGESTIONS);
    }

    public SuggestionPopup(int rowHeight, int maxVisibleSuggestions) {
        this.rowHeight = rowHeight;
        this.maxVisibleSuggestions = maxVisibleSuggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = List.copyOf(suggestions);
        if (this.suggestions.isEmpty()) {
            hide();
            return;
        }

        this.selectedIndex = Math.min(this.selectedIndex, this.suggestions.size() - 1);
        this.firstVisibleIndex = Math.min(this.firstVisibleIndex, this.selectedIndex);
        ensureSelectedVisible();
    }

    public void hide() {
        this.suggestions = List.of();
        this.selectedIndex = 0;
        this.firstVisibleIndex = 0;
    }

    public boolean isVisible() {
        return !suggestions.isEmpty();
    }

    public String selectedValue() {
        return suggestions.get(selectedIndex);
    }

    public KeyResult keyPressed(KeyEvent event) {
        if (!isVisible()) {
            return KeyResult.NONE;
        }

        return switch (event.key()) {
            case InputConstants.KEY_TAB, InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> KeyResult.ACCEPT;
            case InputConstants.KEY_DOWN -> {
                selectedIndex = (selectedIndex + 1) % suggestions.size();
                ensureSelectedVisible();
                yield KeyResult.HANDLED;
            }
            case InputConstants.KEY_UP -> {
                selectedIndex = (selectedIndex + suggestions.size() - 1) % suggestions.size();
                ensureSelectedVisible();
                yield KeyResult.HANDLED;
            }
            case InputConstants.KEY_ESCAPE -> {
                hide();
                yield KeyResult.HANDLED;
            }
            default -> KeyResult.NONE;
        };
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (!isVisible()) {
            return false;
        }

        final int visibleSuggestions = visibleSuggestions();
        if (mouseX < x || mouseX >= x + width || mouseY < y + 1) {
            return false;
        }

        final int visibleIndex = (int) ((mouseY - y - 1) / rowHeight);
        if (visibleIndex < 0 || visibleIndex >= visibleSuggestions) {
            return false;
        }

        selectedIndex = firstVisibleIndex + visibleIndex;
        return true;
    }

    public void extractRenderState(Font font, GuiGraphicsExtractor graphics, int anchorX, int anchorY, int lineHeight, boolean textShadow) {
        if (!isVisible()) {
            return;
        }

        width = suggestions.stream()
                .mapToInt(font::width)
                .max()
                .orElse(0) + HORIZONTAL_PADDING;
        final int visibleSuggestions = visibleSuggestions();
        final int height = visibleSuggestions * rowHeight + 2;
        x = Math.min(anchorX, graphics.guiWidth() - width - SCREEN_MARGIN);
        y = anchorY;
        if (y + height > graphics.guiHeight()) {
            y = anchorY - lineHeight - height - 2;
        }
        x = Math.max(SCREEN_MARGIN, x);
        y = Math.max(SCREEN_MARGIN, y);

        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
        graphics.outline(x, y, width, height, BORDER_COLOR);
        for (int i = 0; i < visibleSuggestions; i++) {
            final int suggestionIndex = firstVisibleIndex + i;
            final int rowY = y + 1 + i * rowHeight;
            if (suggestionIndex == selectedIndex) {
                graphics.fill(x + 1, rowY, x + width - 1, rowY + rowHeight, SELECTED_COLOR);
            }
            graphics.text(font, suggestions.get(suggestionIndex), x + TEXT_LEFT_PADDING, rowY + TEXT_TOP_PADDING, TEXT_COLOR, textShadow);
        }
    }

    private int visibleSuggestions() {
        return Math.min(maxVisibleSuggestions, suggestions.size() - firstVisibleIndex);
    }

    private void ensureSelectedVisible() {
        if (selectedIndex < firstVisibleIndex) {
            firstVisibleIndex = selectedIndex;
        } else if (selectedIndex >= firstVisibleIndex + maxVisibleSuggestions) {
            firstVisibleIndex = selectedIndex - maxVisibleSuggestions + 1;
        }
    }

    public enum KeyResult {
        NONE,
        HANDLED,
        ACCEPT
    }
}
