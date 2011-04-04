package com.narrowtux.DropChest;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.block.Block;

import java.util.List;

import org.bukkit.craftbukkit.entity.CraftItem;

import net.minecraft.server.EntityItem;

import org.bukkit.inventory.ItemStack;

import com.narrowtux.DropChest.DropChest;


public class EntityWatcher implements Runnable {
	private DropChest plugin;
	private List<DropChestItem> chestsToBeRemoved;
	public EntityWatcher(DropChest plugin) {
		this.plugin = plugin;
		chestsToBeRemoved = new ArrayList<DropChestItem>();
	}

	@Override
	public void run() {
		try{
			for(World w : plugin.getServer().getWorlds()){
				for(Entity e : w.getEntities())
				{
					if(e.getClass().getName().contains("CraftItem"))
					{
						CraftItem item = (CraftItem)e;
						EntityItem eitem = (EntityItem)item.getHandle();
						int type = eitem.a.id;
						int count = eitem.a.count;
						short damage = (short)eitem.a.damage;
						ItemStack stack = new ItemStack(type, count, damage);
						for(int i = 0; i<plugin.getChests().size();i++){
							DropChestItem dci = plugin.getChests().get(i);
							Location loc = dci.getLocation();
							World world = loc.getWorld();
							Block block = loc.getBlock();
							int x, z;
							x = loc.getBlockX()/16;
							z = loc.getBlockZ()/16;
							if(!world.isChunkLoaded(x, z)){
								//Try to load the chunk.
								world.loadChunk(x, z);
								continue;
							}
							if(!DropChestItem.acceptsBlockType(block.getType())){
								//Try to load the chunk.
								world.loadChunk(x, z);
								continue;
							}
							if(plugin.isNear(dci.getBlock().getLocation(), e.getLocation(), dci.getRadius()))
							{
								HashMap<Integer, ItemStack> ret = dci.addItem(stack,FilterType.SUCK);
								boolean allin = false;
								if(ret.size()==0){
									item.remove();
									allin = true;
								}
								else {
									for(ItemStack s : ret.values()){
										eitem.a.count = s.getAmount();
									}
								}
								if(dci.isFull())
									dci.warnFull();
								else if(dci.getPercentFull()>=0.8)
									dci.warnNearlyFull();
								if(allin){
									break;
								}
								continue;
							}
						}
					}
				}
			}
		} catch(Exception e){
			System.out.println("Warning! An error occured!");
			e.printStackTrace();
		}
	}
}
