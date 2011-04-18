package com.narrowtux.DropChest;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.ChatColor;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRedstoneEvent;
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
	
	@Override
	public void onBlockRedstoneChange(BlockRedstoneEvent event){
		if(event.getNewCurrent()==0){
			return;
		}
		Block block = event.getBlock();
		BlockFace faces[] = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST};
		for(BlockFace face:faces){
			Block chest = block.getFace(face);
			if(DropChestItem.acceptsBlockType(chest.getType())){
				DropChestItem dci = plugin.getChestByBlock(chest);
				if(dci!=null){
					dci.dropAll();
					return;
				}
			}
		}
		Block chest = null;
		chest = block.getFace(BlockFace.UP).getFace(BlockFace.UP);
		DropChestItem dci = plugin.getChestByBlock(chest);
		if(dci!=null){
			dci.dropAll();
			return;
		}
	}
}
