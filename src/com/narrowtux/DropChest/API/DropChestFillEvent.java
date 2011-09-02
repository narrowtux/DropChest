package com.narrowtux.DropChest.API;

import org.bukkit.event.Event;

import com.narrowtux.DropChest.DropChestItem;

public class DropChestFillEvent extends Event {
	private DropChestItem chest;
	public DropChestFillEvent(DropChestItem chest) {
		super("DropChestFillEvent");
		this.chest = chest;
	}
	
	/**
	 * @return the chest
	 */
	public DropChestItem getChest() {
		return chest;
	}

	public double getNewFillRate(){
		return chest.getPercentFull();
	}
}
