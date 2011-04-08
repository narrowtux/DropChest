package com.narrowtux.DropChest;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * Handle events for all Player related events
 * @author narrowtux
 */
public class DropChestPlayerListener extends PlayerListener {
	private final DropChest plugin;

	public DropChestPlayerListener(DropChest instance) {
		plugin = instance;

	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction()==Action.RIGHT_CLICK_BLOCK){
			DropChestPlayer dplayer = DropChestPlayer.getPlayerByName(event.getPlayer().getName());
			if(dplayer!=null&&!dplayer.getChestRequestType().equals(ChestRequestType.NONE)){
				Block b = event.getClickedBlock();
				if(DropChestItem.acceptsBlockType(b.getType())){
					List<DropChestItem> chests = plugin.getChests();
					DropChestItem chestdci = plugin.getChestByBlock(b);
					
					switch(dplayer.getChestRequestType()){
					case CREATE:
						if(chestdci==null){
							ContainerBlock chest = (ContainerBlock)b.getState();
							int radius = dplayer.getRequestedRadius();
							if(radius < 2)
								radius = 2;
							
							DropChestItem dci = new DropChestItem(chest, radius, b, plugin);
							
							chests.add(dci);
							
							if(event.getPlayer()!=null)
							{
								event.getPlayer().sendMessage("Created DropChest. ID: #"+dci.getId());
							}
							plugin.save();
						} else {
							dplayer.getPlayer().sendMessage(ChatColor.RED+"This DropChest already exists. ID: #"+chestdci.getId());
						}
						break;
					case WHICH:
						if(chestdci!=null){
							dplayer.getPlayer().sendMessage(chestdci.getId()+": "+chestdci.listString());
						} else {
							dplayer.getPlayer().sendMessage("This is not a DropChest!");
						}
						break;
					}
					dplayer.setChestRequestType(ChestRequestType.NONE);
					event.setCancelled(true);
				}
			}
		} else if(event.getAction()==Action.LEFT_CLICK_BLOCK){
			Player player = event.getPlayer();
			DropChestPlayer dplayer = DropChestPlayer.getPlayerByName(player.getName());
			Block b = event.getClickedBlock();
			if(DropChestItem.acceptsBlockType(b.getType())){
				int radius = plugin.getRequestedRadius();
				if(radius < 2)
					radius = 2;
				List<DropChestItem> chests = plugin.getChests();
				boolean chestexists = false;
				DropChestItem chestdci = null;
				int i = 0;
				for(DropChestItem dcic : chests){
					Block block = b;
					if(plugin.locationsEqual(dcic.getBlock().getLocation(), block.getLocation())){
						chestexists = true;
						chestdci = dcic;
						break;
					}
					i++;
				}
				if(chestexists&&plugin.hasPermission(player, "dropchest.filter.set")&&dplayer.isEditingFilter()){
					Material m = player.getItemInHand().getType();
					boolean found = false;
					if(m.getId()==0&&plugin.hasPermission(player, "dropchest.filter.reset")){
						chestdci.getFilter(dplayer.getEditingFilterType()).clear();
						player.sendMessage(ChatColor.GREEN.toString()+"All items will be accepted.");
					} else{
						for (Material ma : chestdci.getFilter(dplayer.getEditingFilterType())){
							if(m.getId()==ma.getId()){
								chestdci.getFilter(dplayer.getEditingFilterType()).remove(ma);
								found = true;
								if(chestdci.getFilter(dplayer.getEditingFilterType()).size()==0){
									player.sendMessage(ChatColor.GREEN.toString()+"All items will be accepted.");
								} else {
									player.sendMessage(ChatColor.RED.toString()+ma.toString()+" won't be accepted.");
								}
								break;
							}
						}
						if(!found)
						{
							chestdci.getFilter(dplayer.getEditingFilterType()).add(m);
							player.sendMessage(ChatColor.GREEN.toString()+m.toString()+" will be accepted.");
						}
					}
					plugin.save();
				}
			}
		}
	}
}

