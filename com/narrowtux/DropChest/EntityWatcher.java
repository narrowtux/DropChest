package com.narrowtux.DropChest;

import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.block.Block;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.narrowtux.DropChest.DropChest;

import org.bukkit.entity.Item;

public class EntityWatcher implements Runnable {
	private DropChest plugin;
	public EntityWatcher(DropChest plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		try{
			for(World w : plugin.getServer().getWorlds()){
				for(Entity e : w.getEntities())
				{
					if(e instanceof Item)
					{
						Item item = (Item)e;
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
							Vector distance = dci.getBlock().getLocation().toVector().add(new Vector(0.5,0,0.5)).subtract(item.getLocation().toVector());
							if(distance.lengthSquared() < 1.0*dci.getRadius()*dci.getRadius() + 1)
							{
								ItemStack stack = item.getItemStack();
								System.out.println(stack);
								HashMap<Integer, ItemStack> ret = dci.addItem(stack,FilterType.SUCK);
								boolean allin = false;
								if(ret.size()==0){
									item.remove();
									allin = true;
								}
								else {
									for(ItemStack s : ret.values()){
										stack.setAmount(s.getAmount());
									}
									item.setItemStack(stack);
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
