package com.narrowtux.DropChest;

import java.util.HashMap;

import net.minecraft.server.EntityMinecart;

import org.bukkit.block.ContainerBlock;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMinecart;
import org.bukkit.craftbukkit.entity.CraftStorageMinecart;
import org.bukkit.entity.Minecart;
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
		timescalled++;
		if(timescalled%100==0){
			//System.out.println(String.valueOf(timescalled));
		}
		 if(event.getVehicle() instanceof Minecart)
			{
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
						if(plugin.isNear(dci.getBlock().getLocation(), storage.getLocation(), 2))
						{
							ContainerBlock chest = dci.getChest();
							Inventory chinv = chest.getInventory();
							Inventory miinv = storage.getInventory();
							if(dci.getMinecartAction()==DropChestMinecartAction.PUSH_TO_MINECART){
								for(int i = 0; i<chinv.getSize();i++){
									ItemStack items = chinv.getItem(i);
									if(items.getAmount()!=0){
										HashMap<Integer,ItemStack> hash = miinv.addItem(items);
										if(hash.size()!=0){
											ItemStack ret=hash.get(0);
											items.setAmount(items.getAmount()-ret.getAmount());
										}
										chinv.remove(items);
									}
								}
								dci.triggerRedstone();
							} else if(dci.getMinecartAction() == DropChestMinecartAction.PULL_FROM_MINECART){
								for(int i = 0; i<miinv.getSize();i++){
									ItemStack items = miinv.getItem(i);
									if(items.getAmount()!=0){
										HashMap<Integer,ItemStack> hash = dci.addItem(items);
										if(hash.size()!=0){
											ItemStack ret=hash.get(0);
											items.setAmount(items.getAmount()-ret.getAmount());
										}
										miinv.remove(items);
									}
								}
								dci.triggerRedstone();
							}
						}
					}
				}

			}
	}

}
