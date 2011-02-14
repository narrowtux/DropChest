package com.narrowtux.DropChest;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DropChestItem {
	private Chest chest;
	private int radius;
	private boolean warnedNearlyFull;
	private boolean warnedFull;
	private DropChest plugin;
	private List<Material> filter = new ArrayList<Material>();
	
	public DropChestItem(Chest chest, int radius, DropChest plugin)
	{
		this.chest = chest;
		this.radius = radius;
		warnedNearlyFull = false;
		warnedFull = false;
		this.plugin = plugin;
	}
	
	public Chest getChest() {
		return chest;
	}
	
	public void setChest(Chest chest) {
		this.chest = chest;
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
		Inventory inv = chest.getInventory();
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
}
