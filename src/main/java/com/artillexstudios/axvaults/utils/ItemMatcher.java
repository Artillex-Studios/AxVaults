package com.artillexstudios.axvaults.utils;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.components.DataComponents;
import com.artillexstudios.axapi.items.components.data.CustomModelData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;

import java.util.Map;

public class ItemMatcher {
    private final WrappedItemStack wrapped;
    private final Map<String, Object> map;
    private final PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();
    private int needMatch = 0;

    public ItemMatcher(WrappedItemStack wrapped, Map<String, Object> map) {
        this.wrapped = wrapped;
        this.map = map;
    }

    public boolean isMatching() {
        int matches = 0;
        if (material()) matches++;
        if (name()) matches++;
        if (customModelData()) matches++;
        return matches >= needMatch;
    }

    public boolean material() {
        Object val = map.getOrDefault("material", map.get("type"));
        if (val == null) return false;
        needMatch++;
        Material material = wrapped.get(DataComponents.MATERIAL);
        if (material == null) return false;
        return SimpleRegex.matches((String) val, material.toString());
    }

    public boolean name() {
        Object val = map.get("name");
        if (val == null) return false;
        needMatch++;
        Component customName = wrapped.get(DataComponents.CUSTOM_NAME);
        if (customName == null) return false;
        String plain = plainSerializer.serialize(customName);
        return SimpleRegex.matches((String) val, plain);
    }

    public boolean customModelData() {
        Object val = map.get("custom-model-data");
        if (!(val instanceof Integer num)) return false;
        needMatch++;
        CustomModelData cmd = wrapped.get(DataComponents.CUSTOM_MODEL_DATA);
        if (cmd == null || cmd.floats().isEmpty()) return false;
        return num == cmd.floats().getFirst().intValue();
    }
}
