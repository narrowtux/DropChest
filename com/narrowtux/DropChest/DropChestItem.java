package com.narrowtux.DropChest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.ContainerBlock;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class DropChestItem {
	private ContainerBlock containerBlock;
	private Block block;

	private int radius;
	private boolean warnedNearlyFull;
	private boolean warnedFull;
	private DropChest plugin;
	private List<Material> filter = new ArrayList<Material>();
	private DropChestMinecartAction minecartAction = DropChestMinecartAction.IGNORE;
	private boolean loadedProperly = true;

	public DropChestItem(ContainerBlock containerBlock, int radius, Block block, DropChest plugin)
	{
		this.containerBlock = containerBlock;
		this.radius = radius;
		warnedNearlyFull = false;
		warnedFull = false;
		this.plugin = plugin;
		this.block = block;
	}

	public DropChestItem(String loadString, String fileVersion, DropChest plugin)
	{
		this.plugin = plugin;
		warnedNearlyFull = false;
		warnedFull = false;
		load(loadString, fileVersion);
	}

	public ContainerBlock getChest() {
		return containerBlock;
	}

	public void setChest(Chest chest) {
		this.containerBlock = chest;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		if(radius<2)
			radius = 2;
		this.radius = radius;
	}

	public double getPercentFull(){
		Inventory inv = containerBlock.getInventory();
		int fullstacks = 0;
		int stacks = inv.getSize();
		for(int j = 0; j<stacks; j++){
			ItemStack istack = inv.getItem(j);
			if(istack.getAmount()!=0){
				fullstacks++;
			}
		}
		double percent = (double)fullstacks/(double)stacks;
		if(percent<0.8)
			warnedNearlyFull = false;
		return percent;
	}

	public boolean isFull(){
		boolean full = getPercentFull() == 1.0;
		if(!full)
			warnedFull = false;
		return full;
	}

	public void warnNearlyFull(){
		if(!warnedNearlyFull){
			plugin.getServer().broadcastMessage("Warning! Chest is nearly full.");
			warnedNearlyFull = true;
		}
	}

	public void warnFull(){
		if(!warnedFull)
		{
			plugin.getServer().broadcastMessage("Warning! Chest is full.");
			warnedFull = true;
		}
	}

	public List<Material> getFilter(){
		return filter;
	}

	public HashMap<Integer, ItemStack> addItem(ItemStack item)
	{
		if(filter.size()==0)
			return containerBlock.getInventory().addItem(item);
		else
		{
			for(Material m : filter)
			{
				if(m.getId()==item.getTypeId()){
					return containerBlock.getInventory().addItem(item);
				}
			}
			HashMap<Integer, ItemStack> ret = new HashMap<Integer, ItemStack>();
			ret.put(0, item);
			return ret;
		}
	}

	/**
	 * @param minecartAction the minecartAction to set
	 */
	public void setMinecartAction(DropChestMinecartAction minecartAction) {
		this.minecartAction = minecartAction;
	}

	/**
	 * @return the minecartAction
	 */
	public DropChestMinecartAction getMinecartAction() {
		return minecartAction;
	}

	public void setRedstone(boolean value){
		Block below = block.getFace(BlockFace.DOWN);
		if(below.getTypeId() == Material.LEVER.getId()){
			byte data = below.getData();
			if(value){
				data=0x8|0x5;
			} else {
				data=0x5;
			}
			below.setData(data);
		}
	}
	
	public void triggerRedstone(){
		setRedstone(true);
		Timer timer = new Timer();
		timer.schedule(new RedstoneTrigger(this), 1000);
	}

	public void dropAll(){
		World world = block.getWorld();
		Location loc = block.getLocation();
		for(int i = 0; i<containerBlock.getInventory().getSize();i++){
			ItemStack item = containerBlock.getInventory().getItem(i);
			if(item.getAmount()!=0){
				world.dropItem(loc, item);
				containerBlock.getInventory().remove(item);
			}
		}
		System.out.println("Dropped all items");
	}

	private void load(String loadString, String fileVersion)
	{
		if(fileVersion.equalsIgnoreCase("0.0")){
			String locSplit[] = loadString.split(",");
			if(locSplit.length>=3){
				Double x = Double.valueOf(locSplit[0]);
				Double y = Double.valueOf(locSplit[1]);
				Double z = Double.valueOf(locSplit[2]);
	
				int radius = 2;
				long worldid = 0;
				if(locSplit.length>=4){
					radius = (int)Integer.valueOf(locSplit[3]);
					if(locSplit.length>=5){
						worldid = (long)Long.valueOf(locSplit[4]);
					}
				}
				World wo = plugin.getWorldWithId(worldid);
				if(wo!=null)
				{
					Block b = wo.getBlockAt((int)(double)x,(int)(double)y,(int)(double)z);
					if(b.getTypeId() == Material.CHEST.getId()){
						ContainerBlock chest = (ContainerBlock)b.getState();
						this.containerBlock = chest;
						this.radius = radius;
						this.block = b;
						if(locSplit.length>=6){
							List<Material> filter = getFilter();
							for(int i = 5;i<locSplit.length;i++){
								filter.add(Material.getMaterial((int)Integer.valueOf(locSplit[i])));
							}
						}
					} else {
						loadedProperly = false;
					}
				} else {
					loadedProperly = false;
				}
	
			} else {
				loadedProperly = false;
			}
		} else if(fileVersion.equalsIgnoreCase("0.1")){
			String splt[] = loadString.split(";");
			if(splt.length>=1){
				String data[] = splt[0].split(",");
				String filters[];
				if(splt.length==2){
					filters = splt[1].split(",");
					for(int i = 0; i<filters.length; i++)
					{
						filter.add(Material.valueOf(filters[i]));
					}
				}
				if(data.length==6){
					Double x,y,z;
					long worldid;
					World world;
					x = Double.valueOf(data[0]);
					y = Double.valueOf(data[1]);
					z = Double.valueOf(data[2]);
					radius = Integer.valueOf(data[3]);
					worldid = Long.valueOf(data[4]);
					minecartAction = DropChestMinecartAction.valueOf(data[5]);
					world = plugin.getWorldWithId(worldid);
					if(world!=null){
						Block b = world.getBlockAt((int)(double)x,(int)(double)y,(int)(double)z);
						if(b.getTypeId() == Material.CHEST.getId()||b.getTypeId()==Material.DISPENSER.getId()){
							this.containerBlock = (ContainerBlock)b.getState();
							this.block = b;
							if(this.containerBlock==null){
								loadedProperly = false;
								System.out.println("Chest is null...");
							}
						} else {
							loadedProperly = false;
						}
					} else {
						loadedProperly = false;
					}
				} else {
					loadedProperly = false;
					return;
				}
			} else {
				loadedProperly=false;
			}
		} else {
			System.out.println("Could not find version:"+fileVersion);
			loadedProperly=false;
		}
	}

	public String save()
	{
		// VERSION!!!! 0.1
		String line = "";
		Location loc = block.getLocation();
		line = String.valueOf(loc.getX()) + "," + String.valueOf(loc.getY()) + "," + String.valueOf(loc.getZ()) + "," + String.valueOf(getRadius()) + "," + String.valueOf(loc.getWorld().getId());
		line += "," + String.valueOf(minecartAction);
		line += ";";
		//Filter saving
		int i = 0;
		for(Material m:getFilter())
		{
			if(i>0){
				line+=",";
			}
			line+=m.name();
			i++;
		}
		line+="\n";
		return line;
	}

	public boolean isLoadedProperly() {
		return loadedProperly;
	}
	

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}
	
	static public boolean acceptsBlockType(Material m){
		return m.getId()==Material.CHEST.getId()
				||m.getId()==Material.DISPENSER.getId()
				||m.getId()==Material.FURNACE.getId()
				||m.getId()==Material.BURNING_FURNACE.getId();
	}
}
