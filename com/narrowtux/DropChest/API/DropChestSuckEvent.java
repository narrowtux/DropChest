package com.narrowtux.DropChest.API;

import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import com.narrowtux.DropChest.DropChestItem;

public class DropChestSuckEvent extends Event implements Cancellable{
	private static final long serialVersionUID = -4108296697557863586L;
	private DropChestItem chest;
	private Item item;
	private boolean cancel;

	public DropChestSuckEvent(DropChestItem chest, Item item) {
		super("DropChestSuckEvent");
		cancel = false;
		this.chest = chest;
		this.item = item;
	}
	
	/**
	 * @return the chest
	 */
	public DropChestItem getChest() {
		return chest;
	}

	/**
	 * @return the item
	 */
	public Item getItem() {
		return item;
	}


	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean arg0) {
		cancel = arg0;
	}

}
