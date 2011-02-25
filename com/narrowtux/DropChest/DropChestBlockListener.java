package com.narrowtux.DropChest;

import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.List;
import org.bukkit.block.ContainerBlock;
/**
 * DropChest block listener
 * @author narrowtux
 */
public class DropChestBlockListener extends BlockListener {
	private final DropChest plugin;
	private List<DropChestItem> chests;
	public DropChestBlockListener(final DropChest instance) {
		plugin = instance;
	}
	@Override
	public void onBlockInteract(BlockInteractEvent event)
	{
		Player player = null;
		if(event.getEntity().getClass().getName().contains("Player")){
			player = (Player)event.getEntity();
		} else {
			return;
		}
		DropChestPlayer dplayer = DropChestPlayer.getPlayerByName(player.getName());
		if(dplayer!=null&&!dplayer.getChestRequestType().equals(ChestRequestType.NONE)){
			Block b = event.getBlock();
			if(DropChestItem.acceptsBlockType(b.getType())){
				ContainerBlock chest = (ContainerBlock)b.getState();
				int radius = dplayer.getRequestedRadius();
				if(radius < 2)
					radius = 2;
				chests = plugin.getChests();
				boolean chestexists = false;
				int chestid = 0;
				DropChestItem chestdci = null;
				int i = 0;
				for(DropChestItem dcic : chests){
					Block block = b;
					if(plugin.locationsEqual(dcic.getBlock().getLocation(), block.getLocation())){
						chestexists = true;
						chestdci = dcic;
						chestid = i;
						break;
					}
					i++;
				}
				if(!chestexists&&dplayer.getChestRequestType().equals(ChestRequestType.CREATE)){
					DropChestItem dci = new DropChestItem(chest, radius, b, plugin);
					chests.add(dci);
					if(player!=null)
					{
						player.sendMessage("Activated DropChest on this Chest");
					}
				}
				if(dplayer.getChestRequestType().equals(ChestRequestType.WHICH)){
					if(chestdci!=null){
						dplayer.getPlayer().sendMessage("ID: "+String.valueOf(chestid+1)+" Radius: "+String.valueOf(chestdci.getRadius()));
						String acceptstring = "";
						for(FilterType type:FilterType.values()){
							acceptstring+=type.toString()+": ";
							for(Material m:chestdci.getFilter(type)){
								acceptstring+=m.toString() + ", ";
							}
						}
						dplayer.getPlayer().sendMessage(acceptstring);

					} else {
						dplayer.getPlayer().sendMessage("This is not a DropChest!");
					}
				}
				dplayer.setChestRequestType(ChestRequestType.NONE);
				plugin.save();
				event.setCancelled(true);
				System.out.println("DropChest activated on "+String.valueOf(chests.size())+" Chests");
			}
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event){
		Block block = event.getBlock();
		if(DropChestItem.acceptsBlockType(block.getType())){
			for(DropChestItem dci : plugin.getChests())
			{
				if(plugin.locationsEqual(dci.getBlock().getLocation(),block.getLocation())&&!plugin.hasPermission(event.getPlayer(), "dropchest.destroy")){
					event.setCancelled(true);
					event.getPlayer().sendMessage(ChatColor.RED.toString()+"You need to remove this DropChest before breaking it");
				}
			}
		}
	}

	@Override
	public void onBlockDamage(BlockDamageEvent event)
	{
		Player player = event.getPlayer();
		DropChestPlayer dplayer = DropChestPlayer.getPlayerByName(player.getName());
		Block b = event.getBlock();
		if(DropChestItem.acceptsBlockType(b.getType())){
			int radius = plugin.getRequestedRadius();
			if(radius < 2)
				radius = 2;
			chests = plugin.getChests();
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
			if(chestexists&&event.getDamageLevel().getLevel()==0&&plugin.hasPermission(player, "dropchest.filter.set")&&dplayer.isEditingFilter()){
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

	@Override
	public void onBlockRedstoneChange(BlockRedstoneEvent event){
		if(event.getNewCurrent()>0){
			DropChestItem chestdci = plugin.getChestByBlock(event.getBlock());
			if(chestdci!=null){
				chestdci.dropAll();
			}
		}
	}
}
