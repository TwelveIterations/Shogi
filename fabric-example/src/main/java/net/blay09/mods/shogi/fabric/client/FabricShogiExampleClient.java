package net.blay09.mods.shogi.fabric.client;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;

public class FabricShogiExampleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.register(ClientCommands.literal("shogi_rule_edit_box")
                .executes(_ -> {
                    Minecraft.getInstance().schedule(() -> Minecraft.getInstance().gui.setScreen(new RuleEditBoxSampleScreen()));
                    return Command.SINGLE_SUCCESS;
                })));
    }
}
