package net.blay09.mods.shogi.fabric.client;

import net.blay09.mods.shogi.client.gui.RuleEditBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RuleEditBoxSampleScreen extends Screen {
    private static final Component TITLE = Component.literal("RuleEditBox Sample");
    private static final String SAMPLE_RULE = """
            any(
              has_item("minecraft:diamond"),
              is_near("minecraft:beacon", 8)
            )
            """;

    private RuleEditBox ruleEditBox;

    public RuleEditBoxSampleScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        final int margin = 24;
        final int titleBottom = 30;
        final int buttonHeight = 20;
        final int buttonTop = this.height - margin - buttonHeight;
        final int editBoxWidth = Math.max(120, this.width - margin * 2);
        final int editBoxHeight = Math.max(60, buttonTop - titleBottom - 12);

        this.ruleEditBox = RuleEditBox.builder()
                .setX(margin)
                .setY(titleBottom)
                .setPlaceholder(Component.literal("Enter a Shogi rule expression"))
                .build(this.font, editBoxWidth, editBoxHeight, TITLE);
        this.ruleEditBox.setCharacterLimit(4096);
        this.ruleEditBox.setValue(SAMPLE_RULE);
        this.addRenderableWidget(this.ruleEditBox);
        this.setInitialFocus(this.ruleEditBox);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(this.width / 2 - 50, buttonTop, 100, buttonHeight)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, TITLE, this.width / 2, 12, 0xFFFFFFFF);
    }
}
