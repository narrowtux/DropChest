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

package com.narrowtux.dropchest;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class DropChestBlockListener extends BlockListener {
	private final DropChest plugin;
	public DropChestBlockListener(final DropChest instance) {
		plugin = instance;
	}
	@Override
	public void onBlockBreak(BlockBreakEvent event){
		Block block = event.getBlock();
		if(DropChestItem.acceptsBlockType(block.getType())){
			DropChestItem dci = plugin.getChestByBlock(block);
			if(dci!=null)
			{
				if(dci.getOwner().equals(event.getPlayer().getName())){
					plugin.removeChest(dci);
					event.getPlayer().sendMessage(ChatColor.GREEN+"Removed Dropchest.");
				} else {
					event.getPlayer().sendMessage("That's not your chest.");
				}
			}
		}
	}

	@Override
	public void onBlockRedstoneChange(BlockRedstoneEvent event){
		if(!plugin.config.isDropItemsOnRedstone()){
			return;
		}
		if(event.getNewCurrent()==0){
			return;
		}
		Block block = event.getBlock();
		BlockFace faces[] = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST};
		for(BlockFace face:faces){
			Block chest = block.getRelative(face);
			if(DropChestItem.acceptsBlockType(chest.getType())){
				DropChestItem dci = plugin.getChestByBlock(chest);
				if(dci!=null){
					dci.dropAll();
					return;
				}
			}
		}
		Block chest = null;
		chest = block.getRelative(BlockFace.UP).getRelative(BlockFace.UP);
		DropChestItem dci = plugin.getChestByBlock(chest);
		if(dci!=null){
			dci.dropAll();
			return;
		}
	}
}
