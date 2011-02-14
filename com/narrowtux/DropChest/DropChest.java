package com.narrowtux.DropChest;

import java.io.File;
import org.bukkit.ChatColor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.narrowtux.DropChest.EntityWatcher;

import java.util.ArrayList;
import java.util.Timer;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ContainerBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.io.*;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.bukkit.plugin.Plugin;

/**
 * DropChest for Bukkit
 *
 * @author narrowtux
 */
public class DropChest extends JavaPlugin {
	private List<DropChestItem> chests = new ArrayList<DropChestItem>();
	@SuppressWarnings("unused")
	private final DropChestPlayerListener playerListener = new DropChestPlayerListener(this);
	private final DropChestBlockListener blockListener = new DropChestBlockListener(this);
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	private EntityWatcher entityWatcher;
	private Timer entityTimer = new Timer();
	private boolean requestChest;
	private boolean whichChest;
	private int requestedRadius;
	private Player requestPlayer;
	public PermissionHandler Permissions = null;

	public DropChest(PluginLoader pluginLoader, Server instance,
			PluginDescriptionFile desc, File folder, File plugin,
			ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		// TODO: Place any custom initialisation code here
		// NOTE: Event registration should be done in onEnable not here as all events are unregistered when a plugin is disabled
		requestedRadius = 2;
	}


	public void onEnable() {
		setupPermissions();

		entityWatcher = new EntityWatcher(this);
		entityTimer.scheduleAtFixedRate(entityWatcher, 100, 1000);
		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Monitor, this);
		// EXAMPLE: Custom code, here we just output some info so we can check all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );

		// Load our stuff
		load();
	}


	public List<DropChestItem> getChests() {
		return chests;
	}

	public void setChests(List<DropChestItem> chests) {
		this.chests = chests;
		save();

	}

	public void setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");


		if(this.Permissions == null) {
			if(test != null) {
				this.Permissions = ((Permissions)test).getHandler();
			} else {
				System.out.println("Warning! Permissions is not enabled! All Operations are allowed!");
			}
		}
	}

	private void load(){
		File file = new File("plugins/DropChest.txt");
		if(file.exists()){
			FileInputStream input;
			try {
				input = new FileInputStream("plugins/DropChest.txt");
				InputStreamReader ir = new InputStreamReader(input);
				BufferedReader r = new BufferedReader(ir);
				String locline = "testseereasd";
				while(true){
					locline = r.readLine();
					if(locline == null)
						break;
					String locSplit[] = locline.split(",");
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
						Block b = getWorldWithId(worldid).getBlockAt((int)(double)x,(int)(double)y,(int)(double)z);
						if(b.getTypeId() == Material.CHEST.getId()){
							Chest chest = (Chest)(ContainerBlock)b.getState();
							DropChestItem dci = new DropChestItem(chest, radius, this);
							if(locSplit.length>=6){
								List<Material> filter = dci.getFilter();
								for(int i = 5;i<locSplit.length;i++){
									filter.add(Material.getMaterial((int)Integer.valueOf(locSplit[i])));
								}
							}
							chests.add(dci);
						} else {
							System.out.println("Could not find a chest."+locline);
						}
					} else {
						System.out.println("Could not read a line."+locline);
					}
				}
				input.close();
				save();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("Could not find chestfile");
		}
	}


	public void save(){
		File file = new File("plugins/DropChest.txt");
		if(!file.exists()){
			System.out.println("no file. Trying to create it.");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream output = new FileOutputStream("plugins/DropChest.txt");
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(output));
			for(DropChestItem dci : chests)
			{
				Block block = dci.getChest().getBlock();
				Location loc = block.getLocation();
				String line = String.valueOf(loc.getX()) + "," + String.valueOf(loc.getY()) + "," + String.valueOf(loc.getZ()) + "," + String.valueOf(dci.getRadius()) + "," + String.valueOf(loc.getWorld().getId());
				//Filter saving
				for(Material m:dci.getFilter())
				{
					line+=","+String.valueOf(m.getId());
				}
				line+="\n";
				w.write(line);
			}
			w.flush();
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public boolean isDebugging(final Player player) {
		if (debugees.containsKey(player)) {
			return debugees.get(player);
		} else {
			return false;
		}
	}

	public void setDebugging(final Player player, final boolean value) {
		debugees.put(player, value);
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		entityTimer.cancel();

	}

	public Boolean isNear(Location loc1, Location loc2, double maxDistance){
		if(loc1.getWorld().getId()!=loc2.getWorld().getId()){
			return false;
		}
		double x1 = loc1.getX(), x2 = loc2.getX(), y1 = loc1.getY(), y2 = loc2.getY(), z1 = loc1.getZ(), z2 = loc2.getZ();
		double dx = x1-x2, dy = y1-y2, dz = z1-z2;
		double maxDistance2 = maxDistance*maxDistance;
		double distance2 = dx*dx+dy*dy+dz*dz;
		return distance2 <= maxDistance2;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[])
	{
		Player player = null;
		if(sender.getClass().getName().contains("Player")){
			player = (Player)sender;
		}
		if(cmd.getName().equalsIgnoreCase("dropchest")||cmd.getName().equalsIgnoreCase("dc"))
		{
			boolean syntaxerror=false;
			if(!hasPermission(player, "dropchest")){
				player.sendMessage("You may not use DropChest. Please ask your Operator to enable this awesome plugin for you.");
				return false;
			}
			if(args.length >= 1&&args.length <= 3){
				if(args[0].equalsIgnoreCase("add")){
					if(!hasPermission(player, "dropchest.create")){
						player.sendMessage("You may not create DropChests.");
						return false;
					}
					requestChest = true;
					whichChest = false;
					if(sender.getClass().getName().contains("Player"))
						requestPlayer = (Player)sender;
					else
						requestPlayer = null;
					if(args.length==2&&hasPermission(player, "dropchest.radius.set")){
						requestedRadius = (int)Integer.valueOf(args[1]);
						if(requestedRadius>getMaximumRadius(player)&&!hasPermission(player, "dropchest.radius.setBig")){
							requestedRadius = getMaximumRadius(player);
						}
					}
					sender.sendMessage(ChatColor.GREEN.toString()+"Now rightclick on the Chest that you want to add");
				} else if(args[0].equalsIgnoreCase("remove")){
					if(!hasPermission(player, "dropchest.remove")){
						player.sendMessage("You may not remove DropChests.");
						return false;
					}
					if(args.length==2){
						int chestid = Integer.valueOf(args[1]);
						chestid--;
						if(chestid>=0&&chestid<chests.size()){
							chests.remove(chestid);
							setChests(chests);
							sender.sendMessage(ChatColor.RED.toString()+"Removed Chest.");
						} else {
							syntaxerror = true;
						}
					} else {
						syntaxerror = true;
					}
				} else if(args[0].equalsIgnoreCase("list")){
					if(!hasPermission(player, "dropchest.list")){
						player.sendMessage("You may not list DropChests.");
						return false;
					}
					int i = 1;
					sender.sendMessage(ChatColor.BLUE.toString()+"List of DropChests");
					sender.sendMessage(ChatColor.BLUE.toString()+"------------------");
					sender.sendMessage(ChatColor.GREEN.toString()+String.valueOf(chests.size())+" DropChests total");
					for(DropChestItem dci : chests){
						Chest chest = dci.getChest();
						Inventory inv = chest.getInventory();
						int fullstacks = 0;
						int stacks = inv.getSize();
						for(int j = 0; j<stacks; j++){
							ItemStack stack = inv.getItem(j);
							if(stack.getAmount()!=0){
								fullstacks++;
							}
						}
						sender.sendMessage("DropChest #"+String.valueOf(i) + ": " + String.valueOf(fullstacks) + "/" + String.valueOf(stacks)+ " stacks used. Radius: "+String.valueOf(dci.getRadius()));
						i++;
					}
				} else if(args[0].equalsIgnoreCase("tp")){
					if(!hasPermission(player, "dropchest.teleport")){
						player.sendMessage("You may not teleport to DropChests.");
						return false;
					}
					int i = Integer.valueOf(args[1]);
					if(i-1<chests.size()&&i>0){
						Chest chest = chests.get(i-1).getChest();
						if(player!=null){
							player.teleportTo(new Location(getServer().getWorlds().get(0),chest.getX(), chest.getY(), chest.getZ()));
						}
					} else {
						syntaxerror = true;
					}
				} else if(args[0].equalsIgnoreCase("setradius")){
					if(!hasPermission(player, "dropchest.radius.set")){
						player.sendMessage("You may not set the radius of a DropChest.");
						return false;
					}
					if(args.length==3){
						int chestid = Integer.valueOf(args[1]);
						int radius = Integer.valueOf(args[2]);
						chestid--;
						if(chestid>=0&&chestid<chests.size() && radius>=1){
							DropChestItem dci = chests.get(chestid);
							boolean force=true;
							if(!hasPermission(player, "dropchest.radius.setBig")){
								force =  false;
							}
							if(radius>getMaximumRadius(player)&&!force){
								radius = getMaximumRadius(player);
							}
							dci.setRadius(radius);
							sender.sendMessage("Radius of Chest #"+String.valueOf(chestid+1)+" set to "+String.valueOf(dci.getRadius()));
							setChests(chests);
						} else {
							syntaxerror = true;
						}
					} else {
						syntaxerror = true;
					}
				} else if(args[0].equalsIgnoreCase("which")){
					if(!hasPermission(player, "dropchest.which")){
						player.sendMessage("You may not ask if this is a DropChest.");
						return false;
					}
					if(player != null){
						requestChest = true;
						whichChest = true;
						requestPlayer = player;
						sender.sendMessage(ChatColor.GREEN.toString()+"Now rightclick on a chest to get its id");
					}

				}else if(args[0].equalsIgnoreCase("resetfilter")){
					if(!hasPermission(player, "dropchest.filter.reset"))
					{
						player.sendMessage("You may not reset the filter of a DropChest!");
						return false;
					} 
					if(args.length==2){
						int chestid = Integer.valueOf(args[1]);
						chestid--;
						if(chestid>=0&&chestid<chests.size()){
							chests.get(chestid).getFilter().clear();
							save();
							sender.sendMessage(ChatColor.GREEN.toString()+"Reset filter.");
						} else {
							syntaxerror = true;
						}
					} else {
						syntaxerror = true;
					}
				}else {
					syntaxerror = true;
				}
			} else {
				syntaxerror = true;
			}
			if(syntaxerror){
				if(onPermissionSend(sender, "dropchest", "DropChest Usage:")){
					onPermissionSend(sender, "dropchest.create", " /dropchest add [radius]: Add a chest to the list, default radius is 2");
					onPermissionSend(sender, "dropchest.remove", " /dropchest remove chestid : Remove a chest from the list");
					onPermissionSend(sender, "dropchest.list", " /dropchest list : Lists all DropChests");
					onPermissionSend(sender, "dropchest.radius.set", " /dropchest setradius chestid radius : Sets the suck-radius of the chest");
					onPermissionSend(sender, "dropchest.which", " /dropchest which : Check if a Chest is a DropChest and which id it has");
					onPermissionSend(sender, "dropchest.teleport", " /dropchest tp chestid : Teleports you to DropChest with ID chestid");
					int max = getMaximumRadius(player);
					String maxs = String.valueOf(max);
					if(hasPermission(player, "dropchest.radius.setBig")||max==65536){
						maxs = "unlimited";
					}
					sender.sendMessage("Your maximum radius is "+maxs);
				}
			}
		}
		return false;
	}

	public boolean isRequestingWhichChest() {
		return whichChest;
	}

	public void resetRequestChest(){
		requestChest = false;
	}

	public boolean isRequestingChest() {
		return requestChest;
	}

	public Player getRequestPlayer() {
		return requestPlayer;
	}

	public int getRequestedRadius() {
		return requestedRadius;
	}

	public int getMaximumRadius(Player player) {
		if(player == null){
			return 65536;
		} else {
			int max = Permissions.getPermissionInteger(player.getName(), "dropchestmaxradius");
			if(max==-1){
				max = 20;
			}
			return max;
		}
	}
	
	public boolean hasPermission(Player player, String node){
		if(Permissions==null)
		{
			return true;
		}
		if(player==null)
		{
			return true;
		} else {
			if(Permissions.has(player, "*")){
				return true;
			} else {
				return Permissions.has(player, node);
			}
		}
	}
	
	public boolean onPermissionSend(CommandSender sender, String node, String message){
		Player player = null;
		if(sender.getClass().getName().contains("Player")){
			player = (Player)sender;
		}
		if(hasPermission(player, node)){
			sender.sendMessage(message);
			return true;
		} else {
			return false;
		}
	}
	
	public World getWorldWithId(long worldid){
		for (World w : getServer().getWorlds()){
			if(w.getId()==worldid){
				return w;
			}
		}
		return null;
	}
	
	public Location locationOf(Chest chest){
		Location ret = new Location(chest.getWorld(), chest.getX(), chest.getY(), chest.getZ());
		return ret;
	}
	
	public boolean locationsEqual(Location loc1, Location loc2){
		return loc1.getWorld().getId()==loc2.getWorld().getId()&&loc1.getX()==loc2.getX()&&loc1.getY()==loc2.getY()&&loc1.getZ()==loc2.getZ();
	}
}

