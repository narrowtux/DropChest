package com.narrowtux.DropChest;

import org.bukkit.event.player.PlayerListener;

/**
 * Handle events for all Player related events
 * @author narrowtux
 */
public class DropChestPlayerListener extends PlayerListener {
    @SuppressWarnings("unused")
	private final DropChest plugin;

    public DropChestPlayerListener(DropChest instance) {
        plugin = instance;
        
    }
}

