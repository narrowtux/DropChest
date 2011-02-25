package com.narrowtux.DropChest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.server.EntityMinecart;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ContainerBlock;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMinecart;
import org.bukkit.craftbukkit.entity.CraftStorageMinecart;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DropChestVehicleListener extends VehicleListener {
	private DropChest plugin;

	private int timescalled = 0;
	public DropChestVehicleListener(DropChest plugin) {
		this.plugin = plugin;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onVehicleMove(VehicleMoveEvent event)
	{
		if(event.getVehicle() instanceof Minecart)
		{
			if(event.getVehicle() instanceof StorageMinecart){
				plugin.log.log(Level.FINEST, "They fixed minecarts!");
			}
			EntityMinecart minecart = (EntityMinecart)((CraftMinecart)event.getVehicle()).getHandle();
			/*
			 * EntityMinecart.d means the type of the minecart
			 * d == 0 means a normal minecart
			 * d == 1 means a storage minecart
			 * d == 2 means a powered minecart
			 */
			if(minecart.d == 1){
				CraftStorageMinecart storage = new CraftStorageMinecart((CraftServer)plugin.getServer(), minecart);
				Inventory inv = storage.getInventory();
				for(DropChestItem dci : plugin.getChests()){

					if(minecartNearBlock(dci.getBlock(), storage))
					{
						dci.minecartAction(storage);
					}
				}
			}

		}
	}

	@Override
	public
	void onVehicleEnter(VehicleEnterEvent event){
		event.setCancelled(true);
		if(event.getEntered() instanceof Player){
			Player p = (Player)event.getEntered();
			Location loc = event.getVehicle().getLocation();
			p.sendMessage(ChatColor.GREEN.toString()+"Pos: "+loc.getBlockX()+"x "+loc.getBlockY()+"y "+loc.getBlockZ()+"z");
		}
	}

	public boolean minecartNearBlock(Block b, Minecart v)
	{
		if(v.getLocation().getBlockY()!=b.getY()){
			return false;
		}
		List<Block> facings = new ArrayList<Block>();
		facings.add(b.getFace(BlockFace.NORTH));
		facings.add(b.getFace(BlockFace.EAST));
		facings.add(b.getFace(BlockFace.SOUTH));
		facings.add(b.getFace(BlockFace.WEST));
		Location loc = v.getLocation();
		int x = loc.getBlockX();
		int z = loc.getBlockZ();

		for(Block face:facings)
		{
			if(x==face.getX()&&z==face.getZ()){
				return true;
			}
		}

		return false;
	}

}
