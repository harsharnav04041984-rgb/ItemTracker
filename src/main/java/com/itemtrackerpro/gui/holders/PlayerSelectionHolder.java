package com.itemtrackerpro.gui.holders;

import com.itemtrackerpro.database.PlayerSummary;
import com.itemtrackerpro.gui.GuiConstants;
import com.itemtrackerpro.gui.GuiManager;
import com.itemtrackerpro.gui.ItpGuiHolder;
import com.itemtrackerpro.gui.util.GuiItemBuilder;
import com.itemtrackerpro.util.TimestampUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Generic disambiguation screen used when a text search matches multiple players. */
public class PlayerSelectionHolder implements ItpGuiHolder {

    private static final int SLOT_BACK = 22;

    private final Inventory inventory;
    private final List<PlayerSummary> matches;
    private final Consumer<Player> onBack;
    private final BiConsumer<Player, PlayerSummary> onSelect;

    public PlayerSelectionHolder(GuiManager guiManager, List<PlayerSummary> matches,
                                  Consumer<Player> onBack, BiConsumer<Player, PlayerSummary> onSelect) {
        this.matches = matches;
        this.onBack = onBack;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(this, GuiConstants.SIZE_3_ROWS,
                Component.text(GuiConstants.TITLE_PREFIX + "Select Player"));
        build();
    }

    private void build() {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, GuiItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build());
        }

        for (int i = 0; i < Math.min(matches.size(), 18); i++) {
            PlayerSummary s = matches.get(i);
            inventory.setItem(i, GuiItemBuilder.of(Material.PLAYER_HEAD)
                    .name("&a&l" + s.name())
                    .lore("&7UUID: &f" + s.uuid(),
                            "&7Recorded events: &f" + s.eventCount(),
                            "&7Last seen: &f" + TimestampUtil.format(s.lastSeen()),
                            "",
                            "&eClick to view")
                    .build());
        }

        inventory.setItem(SLOT_BACK, GuiItemBuilder.of(Material.ARROW).name("&e&lBack").build());
    }

    @Override
    public void handleClick(Player player, int slot, ClickType clickType) {
        if (slot == SLOT_BACK) {
            onBack.accept(player);
            return;
        }
        if (slot < matches.size()) {
            onSelect.accept(player, matches.get(slot));
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}