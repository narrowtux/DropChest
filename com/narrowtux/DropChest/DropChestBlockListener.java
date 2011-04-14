package com.narrowtux.DropChest;

import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
/**
 * DropChest block listener
 * @author narrowtux
 */
public class DropChestBlockListener extends BlockListener {
	private final DropChest plugin;
	public DropChestBlockListener(final DropChest instance) {
		plugin = instance;
	}
	@Override
	public void onBlockBreak(BlockBreakEvent event){
		Block block = event.getBlock();
		if(DropChestItem.acceptsBlockType(block.getType())){
			for(DropChestItem dci : plugin.getChests())
			{
				if(plugin.locationsEqual(dci.getBlock().getLocation(),block.getLocation())){
					if(dci.getOwner().equals(event.getPlayer().getName())){
						plugin.getChests().remove(dci);
						event.getPlayer().sendMessage(ChatColor.GREEN+"Removed Dropchest.");
					} else {
						event.getPlayer().sendMessage("That's not your chest.");
					}
					break;
				}
			}
		}
	}
}
