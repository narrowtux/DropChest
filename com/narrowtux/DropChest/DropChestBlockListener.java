package com.narrowtux.DropChest;

import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockInteractEvent;
import java.util.List;

import org.bukkit.block.Chest;
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
		}
		if(plugin.isRequestingChest()&&player==plugin.getRequestPlayer()){
			Block b = event.getBlock();
			if(b.getTypeId() == Material.CHEST.getId()){
				Chest chest = (Chest)(ContainerBlock)b.getState();
				int radius = plugin.getRequestedRadius();
				if(radius < 2)
					radius = 2;
				chests = plugin.getChests();
				boolean chestexists = false;
				int chestid = 0;
				DropChestItem chestdci = null;
				int i = 0;
				for(DropChestItem dcic : chests){
					Block block = b;
					if(plugin.locationsEqual(plugin.locationOf(dcic.getChest()), block.getLocation())){
						chestexists = true;
						chestdci = dcic;
						chestid = i;
						break;
					}
					i++;
				}
				if(!chestexists&&!plugin.isRequestingWhichChest()){
					DropChestItem dci = new DropChestItem(chest, radius, plugin);
					chests.add(dci);
					if(player!=null)
					{
						player.sendMessage("Activated DropChest on this Chest");
					}
				}
				if(plugin.isRequestingWhichChest()){
					if(chestdci!=null){
						plugin.getRequestPlayer().sendMessage("ID: "+String.valueOf(chestid+1)+" Radius: "+String.valueOf(chestdci.getRadius()));
					} else {
						plugin.getRequestPlayer().sendMessage("This is not a DropChest!");
					}
					plugin.resetRequestChest();
				}

				plugin.resetRequestChest();
				plugin.setChests(chests);
				event.setCancelled(true);
				System.out.println("DropChest activated on "+String.valueOf(chests.size())+" Chests");
			}
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event){
		Block block = event.getBlock();
		if(block.getTypeId() == Material.CHEST.getId()){
			for(DropChestItem dci : plugin.getChests())
			{
				if(plugin.locationsEqual(plugin.locationOf(dci.getChest()),block.getLocation())){
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
		Block b = event.getBlock();
		if(b.getTypeId() == Material.CHEST.getId()){
			Chest chest = (Chest)(ContainerBlock)b.getState();
			int radius = plugin.getRequestedRadius();
			if(radius < 2)
				radius = 2;
			chests = plugin.getChests();
			boolean chestexists = false;
			int chestid = 0;
			DropChestItem chestdci = null;
			int i = 0;
			for(DropChestItem dcic : chests){
				Block block = b;
				if(plugin.locationsEqual(plugin.locationOf(dcic.getChest()), block.getLocation())){
					chestexists = true;
					chestdci = dcic;
					chestid = i;
					break;
				}
				i++;
			}
			if(chestexists&&event.getDamageLevel().getLevel()==0){
				player.sendMessage("You touched me with type "+player.getItemInHand().getType().toString());
			}
		}
	}
}
