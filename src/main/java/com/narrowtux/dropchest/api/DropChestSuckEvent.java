/*
 * Copyright (C) 2011 Moritz Schmale <narrow.m@gmail.com>
 *
 * DropChest is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl.html>.
 */

package com.narrowtux.dropchest.api;

import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import com.narrowtux.dropchest.DropChestItem;

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
