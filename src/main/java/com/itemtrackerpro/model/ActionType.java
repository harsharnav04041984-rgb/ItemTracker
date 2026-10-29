package com.itemtrackerpro.model;

import com.itemtrackerpro.config.ConfigManager;

public enum ActionType {
    BLOCK_BREAK("block-break"),
    BLOCK_PLACE("block-place"),
    CRAFT("crafting"),
    SMELT("smelting"),
    PICKUP("item-pickup"),
    DROP("item-drop"),
    PLAYER_TRANSFER("player-transfer"),
    INVENTORY_CHANGE("inventory-change"),
    CONTAINER_DEPOSIT("container-deposit"),
    CONTAINER_WITHDRAW("container-withdraw"),
    CONTAINER_TRANSFER("container-transfer"),
    TRADE("trade"),
    FISHING("fishing");

    private final String configKey;

    ActionType(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    public boolean isEnabled(ConfigManager configManager) {
        return configManager.isTrackingEnabled(configKey);
    }
}