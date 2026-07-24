package net.blay09.mods.shogi.client.gui;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.IMEPreeditOverlay;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.TextCursorUtils;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.PreeditEvent;
import net.blay09.mods.shogi.common.mixin.MultilineTextFieldStringViewAccessor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class RuleEditBox extends AbstractTextAreaWidget {
    private static final int LINE_HEIGHT = 9;
    private static final int CURSOR_HEIGHT = LINE_HEIGHT + 1;
    private static final int CURSOR_COLOR = 0xFFD0D0D0;
    private static final int PLACEHOLDER_TEXT_COLOR = ARGB.color(204, 0xFFE0E0E0);

    private final Font font;
    private final Component placeholder;
    private final MultilineTextField textField;
    private final boolean textShadow;
    private final int cursorColor;
    private @Nullable IMEPreeditOverlay preeditOverlay;
    private long focusedTime = Util.getMillis();

    private RuleEditBox(
            Font font,
            int x,
            int y,
            int width,
            int height,
            Component placeholder,
            Component narration,
            boolean textShadow,
            int cursorColor,
            boolean showBackground,
            boolean showDecorations) {
        super(x, y, width, height, narration, AbstractScrollArea.defaultSettings((int) (LINE_HEIGHT / 2.0)), showBackground, showDecorations);
        this.font = font;
        this.placeholder = placeholder;
        this.textShadow = textShadow;
        this.cursorColor = cursorColor;
        this.textField = new MultilineTextField(font, width - this.totalInnerPadding());
        this.textField.setCursorListener(this::scrollToCursor);
    }

    public void setCharacterLimit(int characterLimit) {
        this.textField.setCharacterLimit(characterLimit);
    }

    public void setLineLimit(int lineLimit) {
        this.textField.setLineLimit(lineLimit);
    }

    public void setValueListener(Consumer<String> valueListener) {
        this.textField.setValueListener(valueListener);
    }

    public void setValue(String value) {
        this.setValue(value, false);
    }

    public void setValue(String value, boolean allowOverflowLineLimit) {
        this.textField.setValue(value, allowOverflowLineLimit);
    }

    public String getValue() {
        return this.textField.value();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.editBox", this.getMessage(), this.getValue()));
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (doubleClick) {
            this.textField.selectWordAtCursor();
        } else {
            this.textField.setSelecting(event.hasShiftDown());
            this.seekCursorScreen(event.x(), event.y());
        }
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        this.textField.setSelecting(true);
        this.seekCursorScreen(event.x(), event.y());
        this.textField.setSelecting(event.hasShiftDown());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return this.textField.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.visible && this.isFocused() && event.isAllowedChatCharacter()) {
            this.textField.insertText(event.codepointAsString());
            return true;
        }
        return false;
    }

    @Override
    public boolean preeditUpdated(@Nullable PreeditEvent event) {
        this.preeditOverlay = event != null ? new IMEPreeditOverlay(event, this.font, CURSOR_HEIGHT) : null;
        return true;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        final String value = this.textField.value();
        if (value.isEmpty() && !this.isFocused()) {
            graphics.textWithWordWrap(this.font, this.placeholder, this.getInnerLeft(), this.getInnerTop(), this.width - this.totalInnerPadding(), PLACEHOLDER_TEXT_COLOR);
            return;
        }

        final var spans = ShogiExpressionHighlighter.highlight(value);
        final int cursor = this.textField.cursor();
        final boolean showCursor = this.isFocused() && TextCursorUtils.isCursorVisible(Util.getMillis() - this.focusedTime);
        final boolean needsValidCursorPos = this.preeditOverlay != null;
        final boolean insertCursor = cursor < value.length();
        int cursorX = this.getInnerLeft();
        int cursorY = this.getInnerTop();
        int drawTop = this.getInnerTop();
        boolean hasDrawnCursor = false;

        for (Object lineViewObject : this.textField.iterateLines()) {
            final MultilineTextFieldStringViewAccessor lineView = (MultilineTextFieldStringViewAccessor) lineViewObject;
            final boolean lineWithinVisibleBounds = this.withinContentAreaTopBottom(drawTop, drawTop + LINE_HEIGHT);
            if (lineWithinVisibleBounds) {
                drawHighlightedLine(graphics, value, spans, lineView.getBeginIndex(), lineView.getEndIndex(), this.getInnerLeft(), drawTop);
            }

            if (!hasDrawnCursor && (needsValidCursorPos || showCursor) && insertCursor && cursor >= lineView.getBeginIndex() && cursor <= lineView.getEndIndex()) {
                cursorX = this.getInnerLeft() + this.font.width(value.substring(lineView.getBeginIndex(), cursor));
                cursorY = drawTop;
                if (lineWithinVisibleBounds && showCursor) {
                    TextCursorUtils.extractInsertCursor(graphics, cursorX, cursorY, this.cursorColor, CURSOR_HEIGHT);
                }
                hasDrawnCursor = true;
            } else if ((needsValidCursorPos || showCursor) && !insertCursor && lineWithinVisibleBounds) {
                cursorX = this.getInnerLeft() + this.font.width(value.substring(lineView.getBeginIndex(), lineView.getEndIndex()));
                cursorY = drawTop;
            }

            drawTop += LINE_HEIGHT;
        }

        if (showCursor && !insertCursor && this.withinContentAreaTopBottom(cursorY, cursorY + LINE_HEIGHT)) {
            TextCursorUtils.extractAppendCursor(graphics, this.font, cursorX, cursorY, this.cursorColor, this.textShadow);
        }

        extractSelection(graphics, value);

        if (this.isHovered()) {
            graphics.requestCursor(CursorTypes.IBEAM);
        }

        if (this.preeditOverlay != null) {
            this.preeditOverlay.updateInputPosition(cursorX, cursorY);
            graphics.setPreeditOverlay(this.preeditOverlay);
        }
    }

    private void drawHighlightedLine(
            GuiGraphicsExtractor graphics,
            String value,
            List<ShogiExpressionHighlighter.Span> spans,
            int lineStart,
            int lineEnd,
            int x,
            int y) {
        int current = lineStart;
        for (ShogiExpressionHighlighter.Span span : spans) {
            if (span.end() <= lineStart) {
                continue;
            }
            if (span.start() >= lineEnd) {
                break;
            }

            if (current < span.start()) {
                x = drawTextSlice(graphics, value, current, Math.min(span.start(), lineEnd), x, y, ShogiExpressionHighlighter.DEFAULT_COLOR);
            }

            final int start = Math.max(span.start(), lineStart);
            final int end = Math.min(span.end(), lineEnd);
            if (start < end) {
                x = drawTextSlice(graphics, value, start, end, x, y, span.color());
            }
            current = Math.max(current, end);
        }

        if (current < lineEnd) {
            drawTextSlice(graphics, value, current, lineEnd, x, y, ShogiExpressionHighlighter.DEFAULT_COLOR);
        }
    }

    private int drawTextSlice(GuiGraphicsExtractor graphics, String value, int start, int end, int x, int y, int color) {
        final String text = value.substring(start, end);
        graphics.text(this.font, text, x, y, color, this.textShadow);
        return x + this.font.width(text);
    }

    private void extractSelection(GuiGraphicsExtractor graphics, String value) {
        if (!this.textField.hasSelection()) {
            return;
        }

        final MultilineTextFieldStringViewAccessor selection = (MultilineTextFieldStringViewAccessor) (Object) this.textField.getSelected();
        int drawTop = this.getInnerTop();
        for (Object lineViewObject : this.textField.iterateLines()) {
            final MultilineTextFieldStringViewAccessor lineView = (MultilineTextFieldStringViewAccessor) lineViewObject;
            if (selection.getBeginIndex() > lineView.getEndIndex()) {
                drawTop += LINE_HEIGHT;
                continue;
            }
            if (lineView.getBeginIndex() > selection.getEndIndex()) {
                break;
            }

            if (this.withinContentAreaTopBottom(drawTop, drawTop + LINE_HEIGHT)) {
                final int drawBegin = this.font.width(value.substring(lineView.getBeginIndex(), Math.max(selection.getBeginIndex(), lineView.getBeginIndex())));
                final int drawEnd;
                if (selection.getEndIndex() > lineView.getEndIndex()) {
                    drawEnd = this.width - this.innerPadding();
                } else {
                    drawEnd = this.font.width(value.substring(lineView.getBeginIndex(), selection.getEndIndex()));
                }

                graphics.textHighlight(this.getInnerLeft() + drawBegin, drawTop, this.getInnerLeft() + drawEnd, drawTop + LINE_HEIGHT, true);
            }

            drawTop += LINE_HEIGHT;
        }
    }

    @Override
    protected void extractDecorations(GuiGraphicsExtractor graphics) {
        super.extractDecorations(graphics);
        if (this.textField.hasCharacterLimit()) {
            final int characterLimit = this.textField.characterLimit();
            final Component countText = Component.translatable("gui.multiLineEditBox.character_limit", this.textField.value().length(), characterLimit);
            graphics.text(this.font, countText, this.getX() + this.width - this.font.width(countText), this.getY() + this.height + 4, -6250336);
        }
    }

    @Override
    public int getInnerHeight() {
        return LINE_HEIGHT * this.textField.getLineCount();
    }

    private void scrollToCursor() {
        double scrollAmount = this.scrollAmount();
        final var firstFullyVisibleLine = (MultilineTextFieldStringViewAccessor) (Object) this.textField.getLineView((int) (scrollAmount / LINE_HEIGHT));
        if (this.textField.cursor() <= firstFullyVisibleLine.getBeginIndex()) {
            scrollAmount = this.textField.getLineAtCursor() * LINE_HEIGHT;
        } else {
            final var lastFullyVisibleLine = (MultilineTextFieldStringViewAccessor) (Object) this.textField.getLineView((int) ((scrollAmount + this.height) / LINE_HEIGHT) - 1);
            if (this.textField.cursor() > lastFullyVisibleLine.getEndIndex()) {
                scrollAmount = this.textField.getLineAtCursor() * LINE_HEIGHT - this.height + LINE_HEIGHT + this.totalInnerPadding();
            }
        }

        this.setScrollAmount(scrollAmount);
    }

    private void seekCursorScreen(double x, double y) {
        final double mouseX = x - this.getX() - this.innerPadding();
        final double mouseY = y - this.getY() - this.innerPadding() + this.scrollAmount();
        this.textField.seekCursorToPoint(mouseX, mouseY);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) {
            this.focusedTime = Util.getMillis();
        }

        Minecraft.getInstance().onTextInputFocusChange(this, focused);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int x;
        private int y;
        private Component placeholder = CommonComponents.EMPTY;
        private boolean textShadow = true;
        private int cursorColor = CURSOR_COLOR;
        private boolean showBackground = true;
        private boolean showDecorations = true;

        public Builder setX(int x) {
            this.x = x;
            return this;
        }

        public Builder setY(int y) {
            this.y = y;
            return this;
        }

        public Builder setPlaceholder(Component placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Builder setTextShadow(boolean textShadow) {
            this.textShadow = textShadow;
            return this;
        }

        public Builder setCursorColor(int cursorColor) {
            this.cursorColor = cursorColor;
            return this;
        }

        public Builder setShowBackground(boolean showBackground) {
            this.showBackground = showBackground;
            return this;
        }

        public Builder setShowDecorations(boolean showDecorations) {
            this.showDecorations = showDecorations;
            return this;
        }

        public RuleEditBox build(Font font, int width, int height, Component narration) {
            return new RuleEditBox(
                    font,
                    this.x,
                    this.y,
                    width,
                    height,
                    this.placeholder,
                    narration,
                    this.textShadow,
                    this.cursorColor,
                    this.showBackground,
                    this.showDecorations);
        }
    }
}
