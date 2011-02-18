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

import com.sun.tools.example.debug.gui.Environment;

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
	private static int currentId = 1;
	private int id;
	public DropChestItem(ContainerBlock containerBlock, int radius, Block block, DropChest plugin)
	{
		this.containerBlock = containerBlock;
		this.radius = radius;
		warnedNearlyFull = false;
		warnedFull = false;
		this.plugin = plugin;
		this.block = block;
		id = currentId++;
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
		} else if(fileVersion.equals("0.1")||fileVersion.equals("0.2")||fileVersion.equals("0.3")||fileVersion.equals("0.4")){
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
				if(data.length>=6){
					Double x,y,z;
					long worldid;
					World world;
					x = Double.valueOf(data[0]);
					y = Double.valueOf(data[1]);
					z = Double.valueOf(data[2]);
					radius = Integer.valueOf(data[3]);
					setMinecartAction(DropChestMinecartAction.valueOf(data[5]));
					org.bukkit.World.Environment env = org.bukkit.World.Environment.NORMAL;
					if(fileVersion.equals("0.3")){
						env = org.bukkit.World.Environment.valueOf(data[6]);
					}
					if(fileVersion.equalsIgnoreCase("0.1")){
						worldid = Long.valueOf(data[4]);
						world = plugin.getWorldWithId(worldid);
					} else {
						world = plugin.getServer().getWorld(data[4]);
						if(world==null)
						{
							world = plugin.getServer().createWorld(data[4], env);
						}
					}
					if(fileVersion.equals("0.4")){
						id = Integer.valueOf(data[7]);
						currentId = Math.max(currentId, id+1);
					} else {
						id = currentId++;
					}
					if(world!=null){
						Block b = world.getBlockAt((int)(double)x,(int)(double)y,(int)(double)z);
						if(acceptsBlockType(b.getType())){
							this.containerBlock = (ContainerBlock)b.getState();
							this.block = b;
							if(this.containerBlock==null){
								loadedProperly = false;
								System.out.println("Chest is null...");
							}
						} else {
							System.out.println("Block is not accepted!");
							loadedProperly = false;
						}
					} else {
						System.out.println("World not found!");
						loadedProperly = false;
					}
				} else {
					System.out.println("Number of columns not accepted!");
					loadedProperly = false;
					return;
				}
			} else {
				System.out.println("Number of columns not accepted!");
				loadedProperly=false;
			}
		} else {
			System.out.println("File has invalid version: "+fileVersion);
			loadedProperly=false;
		}
	}

	public String save()
	{
		// VERSION!!!! 0.3
		String line = "";
		Location loc = block.getLocation();
		line = String.valueOf(loc.getX()) + "," + String.valueOf(loc.getY()) + "," + String.valueOf(loc.getZ()) + "," + String.valueOf(getRadius()) + "," + String.valueOf(loc.getWorld().getName());
		line += "," + String.valueOf(minecartAction);
		line += "," + String.valueOf(loc.getWorld().getEnvironment());
		line += "," + String.valueOf(id);
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

	public int getId() {
		return id;
	}
	
	public String listString(){
		String ret = "";
		ret+="#"+String.valueOf(id);
		double p = getPercentFull();
		ret+=" | "+(int)(p*100)+"%";
		if(getFilter().size()>0)
		{
			ret+=" | has "+String.valueOf(getFilter().size())+" filtered items";
		} else {
			ret+=" | no filters";
		}
		if(minecartAction.equals(DropChestMinecartAction.IGNORE)){
			ret+=" | ignore";
		} else if(minecartAction.equals(DropChestMinecartAction.PUSH_TO_MINECART)){
			ret+=" | push";
		} else {
			ret+=" | pull";
		}
		return ret;
	}
}
