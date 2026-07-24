package net.blay09.mods.shogi.client.gui;

import java.util.List;

public record TextSuggestions(int start, int end, List<String> values) {
    public TextSuggestions {
        values = List.copyOf(values);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }
}
