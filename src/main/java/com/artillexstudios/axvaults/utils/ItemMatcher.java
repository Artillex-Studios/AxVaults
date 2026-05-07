package com.artillexstudios.axvaults.utils;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
        if (nbtTags()) matches++;
        return matches >= needMatch;
    }

    public boolean material() {
        Object val = map.getOrDefault("material", map.get("type"));
        if (val == null) return false;
        needMatch++;
        var material = DataComponents.material();
        if (material == null) return false;
        return SimpleRegex.matches((String) val, wrapped.get(material).toString());
    }

    public boolean name() {
        Object val = map.get("name");
        if (val == null) return false;
        needMatch++;
        Component customName = wrapped.get(DataComponents.customName());
        if (customName == null) return false;
        String plain = plainSerializer.serialize(customName);
        return SimpleRegex.matches((String) val, plain);
    }

    public boolean customModelData() {
        Object val = map.get("custom-model-data");
        if (!(val instanceof Integer num)) return false;
        needMatch++;
        var cmd = wrapped.get(DataComponents.customModelData());
        if (cmd == null || cmd.floats().isEmpty()) return false;
        return num == cmd.floats().getFirst().intValue();
    }

    public boolean nbtTags() {
        Object val = map.get("nbt-tags");
        if (!(val instanceof List<?> confRawTags) || confRawTags.isEmpty()) return false;
        needMatch++;

        // Get NBTs of the item getting moved
        CompoundTag itemMeta = wrapped.get(DataComponents.customData());
        if (itemMeta == null) return false;
        Set<String> itemTags = itemMeta.getAllKeys();

        for (Object confRawTag : confRawTags) {
            if (!(confRawTag instanceof String confTag) || confTag.isBlank()) continue;

            for (String itemTag : itemTags) {
                if (SimpleRegex.matches(confTag, itemTag)) return true;
            }
        }
        return false;
    }
}
