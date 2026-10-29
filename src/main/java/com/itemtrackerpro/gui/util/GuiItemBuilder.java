package com.itemtrackerpro.gui.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class GuiItemBuilder {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final ItemStack itemStack;
    private final ItemMeta meta;

    public GuiItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.meta = itemStack.getItemMeta();
    }

    public GuiItemBuilder(ItemStack base) {
        this.itemStack = base.clone();
        this.meta = itemStack.getItemMeta();
    }

    public static GuiItemBuilder of(Material material) {
        return new GuiItemBuilder(material);
    }

    public static GuiItemBuilder of(ItemStack base) {
        return new GuiItemBuilder(base);
    }

    public GuiItemBuilder name(String legacyText) {
        if (meta != null) {
            meta.displayName(plain(legacyText));
        }
        return this;
    }

    public GuiItemBuilder lore(String... lines) {
        if (meta != null) {
            List<Component> lore = new ArrayList<>();
            for (String line : lines) {
                lore.add(plain(line));
            }
            meta.lore(lore);
        }
        return this;
    }

    public GuiItemBuilder loreList(List<String> lines) {
        return lore(lines.toArray(new String[0]));
    }

    public GuiItemBuilder amount(int amount) {
        itemStack.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    public GuiItemBuilder flags(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    public GuiItemBuilder skullOwner(UUID uuid) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        }
        return this;
    }

    public ItemStack build() {
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private Component plain(String legacyText) {
        return LEGACY.deserialize(legacyText).decoration(TextDecoration.ITALIC, false);
    }
}