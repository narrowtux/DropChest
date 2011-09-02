package com.narrowtux.DropChest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class DropChestVehicleListener extends VehicleListener {
	private DropChest plugin;

	public DropChestVehicleListener(DropChest plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onVehicleMove(VehicleMoveEvent event)
	{
		if(event.getVehicle() instanceof StorageMinecart){
			StorageMinecart storage = (StorageMinecart)event.getVehicle();
			for(DropChestItem dci : plugin.getChests()){
				if(minecartNearBlock(dci.getBlock(), storage))
				{
					dci.minecartAction(storage);
				}
			}
		}
	}
	
	public boolean minecartNearBlock(Block b, Minecart v)
	{
		if(Math.abs(v.getLocation().getBlockY()-b.getY())>1)
		{
			return false;
		}
		
		List<Block> facings = new ArrayList<Block>();
		facings.add(b.getRelative(BlockFace.NORTH));
		facings.add(b.getRelative(BlockFace.EAST));
		facings.add(b.getRelative(BlockFace.SOUTH));
		facings.add(b.getRelative(BlockFace.WEST));
		facings.add(b.getRelative(BlockFace.UP));
		facings.add(b.getRelative(BlockFace.DOWN));
		Location loc = v.getLocation();
		int x = loc.getBlockX();
		int z = loc.getBlockZ();
		int y = loc.getBlockY();

		for(Block face:facings)
		{
			if(x==face.getX()&&z==face.getZ()&&y==face.getY()){
				return true;
			}
		}

		return false;
	}
}
