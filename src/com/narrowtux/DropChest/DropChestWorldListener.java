package com.narrowtux.DropChest;

import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;

public class DropChestWorldListener extends WorldListener {
	public static DropChest plugin;
	@Override
	public void onChunkUnload(ChunkUnloadEvent event){
		for(DropChestItem item:plugin.getChests()){
			if(item.getBlock().getChunk().equals(event.getChunk())){
				event.setCancelled(true);
				return;
			}
		}
	}
}
