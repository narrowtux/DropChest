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
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private int requestedRadius;
	private Player requestPlayer;
	public PermissionHandler Permissions = null;
	@SuppressWarnings("unused")
	private String version = "0.0";
	private DropChestVehicleListener vehicleListener = new DropChestVehicleListener(this);
	public Logger log;
	public DropChest() {
		// TODO: Place any custom initialisation code here
		// NOTE: Event registration should be done in onEnable not here as all events are unregistered when a plugin is disabled
		requestedRadius = 2;
		DropChestPlayer.plugin = this;
	}


	public void onEnable() {
		log = getServer().getLogger();
		setupPermissions();
		entityWatcher = new EntityWatcher(this);
		entityTimer.scheduleAtFixedRate(entityWatcher, 100, 1000);
		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.REDSTONE_CHANGE, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Priority.Normal, this);
		//pm.registerEvent(Event.Type.VEHICLE_ENTER, vehicleListener, Priority.Normal, this);
		// EXAMPLE: Custom code, here we just output some info so we can check all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		log.log( Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
		version = pdfFile.getVersion();
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
			try{
				this.Permissions = ((Permissions)test).getHandler();
			} catch(Exception e) {
				this.Permissions = null;
				log.log(Level.WARNING, "Permissions is not enabled! All Operations are allowed!");
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
				String locline;
				String version = "0.0";
				while(true){
					locline = r.readLine();
					if(locline == null)
						break;
					if(locline.startsWith("#"))
					{
						continue;
					}
					if(locline.contains("version")){
						version = locline.split(" ")[1];
					} else {
						DropChestItem item = new DropChestItem(locline, version, this);
						if(item.isLoadedProperly())
							chests.add(item);
						else
							log.log(Level.SEVERE, "Problem with line "+locline);
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
			log.log(Level.SEVERE, "no file. Trying to create it.");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream output = new FileOutputStream("plugins/DropChest.txt");
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(output));
			w.write("version 0.5\n");
			w.write("# LEGEND\n# x, y, z, radius, World-Name, minecartAction, chestid;Suck-Filter;Pull-Filter;Push-Filter\n");
			for(DropChestItem dci : chests)
			{
				String line = dci.save();
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
			if(args.length >= 1&&args.length <= 4){
				
				
				
				if(args[0].equalsIgnoreCase("add")){
					/*****************
					 *      ADD      *
					 *****************/
					if(!hasPermission(player, "dropchest.create")){
						player.sendMessage("You may not create DropChests.");
						return false;
					}
					DropChestPlayer player2 = DropChestPlayer.getPlayerByName(player.getName());
					player2.setChestRequestType(ChestRequestType.CREATE);
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
					player2.setRequestedRadius(requestedRadius);
					requestedRadius = 2;
					sender.sendMessage(ChatColor.GREEN.toString()+"Now rightclick on the Chest that you want to add");
				} else if(args[0].equalsIgnoreCase("remove")){
					/*****************
					 *     REMOVE    *
					 *****************/
					if(!hasPermission(player, "dropchest.remove")){
						player.sendMessage("You may not remove DropChests.");
						return false;
					}
					if(args.length==2){
						int chestid = Integer.valueOf(args[1]);
						DropChestItem dci = getChestById(chestid);
						if(dci!=null){
							chests.remove(dci);
							save();
							sender.sendMessage(ChatColor.RED.toString()+"Removed Chest.");
						} else {
							syntaxerror = true;
						}
					} else {
						syntaxerror = true;
					}
				} else if(args[0].equalsIgnoreCase("list")){
					/*****************
					 *      LIST     *
					 *****************/
					if(!hasPermission(player, "dropchest.list")){
						player.sendMessage("You may not list DropChests.");
						return false;
					}
					int i = 1;

					//Page limit is 6 items per page
					//calculation of needed pages
					int num = chests.size();
					int needed = (int) Math.ceil((double)num/6.0);
					int current = 1;
					if(args.length==2){
						current = Integer.valueOf(args[1]);
					}
					if(current>needed)
						current = 1;
					if(needed!=1)
						sender.sendMessage(ChatColor.BLUE.toString()+"Page "+String.valueOf(current)+" of "+ String.valueOf(needed));
					sender.sendMessage(ChatColor.BLUE.toString()+"ID | % full | filters | radius");
					sender.sendMessage(ChatColor.BLUE.toString()+"------");
					for(i = (current-1)*6;i<Math.min(current*6, chests.size()); i++){
						sender.sendMessage(chests.get(i).listString());
					}

				} else if(args[0].equalsIgnoreCase("tp")){
					/*****************
					 *   TELEPORT    *
					 *****************/
					if(!hasPermission(player, "dropchest.teleport")){
						player.sendMessage("You may not teleport to DropChests.");
						return false;
					}
					int i = Integer.valueOf(args[1]);
					DropChestItem dci = getChestById(i);
					if(dci!=null){
						if(player!=null){
							player.teleportTo(dci.getBlock().getLocation());
						}
					} else {
						sender.sendMessage(ChatColor.RED.toString()+"This chest does not exist.");
					}
				} else if(args[0].equalsIgnoreCase("setradius")){
					/*****************
					 *   SETRADIUS   *
					 *****************/
					if(!hasPermission(player, "dropchest.radius.set")){
						player.sendMessage("You may not set the radius of a DropChest.");
						return false;
					}
					if(args.length==3){
						int chestid = Integer.valueOf(args[1]);
						int radius = Integer.valueOf(args[2]);
						DropChestItem dci = getChestById(chestid);
						if(dci != null){
							boolean force=true;
							if(!hasPermission(player, "dropchest.radius.setBig")){
								force =  false;
							}
							if(radius>getMaximumRadius(player)&&!force){
								radius = getMaximumRadius(player);
							}
							dci.setRadius(radius);
							sender.sendMessage("Radius of Chest #"+String.valueOf(chestid)+" set to "+String.valueOf(dci.getRadius()));
							setChests(chests);
						} else {
							syntaxerror = true;
						}
					} else {
						syntaxerror = true;
					}
				} else if(args[0].equalsIgnoreCase("which")){
					/*****************
					 *     WHICH     *
					 *****************/
					if(!hasPermission(player, "dropchest.which")){
						player.sendMessage("You may not ask if this is a DropChest.");
						return false;
					}
					if(player != null){
						DropChestPlayer pl = DropChestPlayer.getPlayerByName(player.getName());
						pl.setChestRequestType(ChestRequestType.WHICH);
						sender.sendMessage(ChatColor.GREEN.toString()+"Now rightclick on a chest to get its id");
					}

				}else if(args[0].equalsIgnoreCase("filter")){
					/*****************
					 *     FILTER    *
					 *****************/
					if(!hasPermission(player, "dropchest.filter"))
					{
						player.sendMessage("You may not use DropChest filters!");
						return false;
					} 
					//dropchest filter {suck|push|pull|finish} [{chestid} {itemid|itemtype|clear}]
					DropChestPlayer dplayer = null;
					if(player!=null){
						dplayer = DropChestPlayer.getPlayerByName(player.getName());
					}
					if(args.length>=2){
						String typestring = args[1];
						FilterType type = null;
						try{
							type = FilterType.valueOf(typestring.toUpperCase());
						} catch(java.lang.IllegalArgumentException e){
							type = null;
						}
						if(type!=null){
							if(args.length==2&&dplayer!=null){
								dplayer.setEditingFilter(true);
								dplayer.setEditingFilterType(type);
								dplayer.getPlayer().sendMessage(ChatColor.GREEN.toString()+"You're now entering interactive mode for filtering "+type.toString().toLowerCase()+"ed items");
							} else if(dplayer==null&&args.length==2) {
								sender.sendMessage("You can't use interactive mode from a console!");
							} else if(args.length==4) {
								String cheststring = args[2];
								String itemstring = args[3];
								Integer chestid = null;
								try{
									chestid = Integer.valueOf(cheststring);
								} catch (java.lang.NumberFormatException e){
									chestid = null;
								}
								if(chestid!=null){
									DropChestItem chest = getChestById(chestid);
									Material item = null;
									if(itemstring.equalsIgnoreCase("clear")){
										chest.getFilter(type).clear();
										sender.sendMessage(ChatColor.GREEN.toString()+"Filter cleared.");
									} else {
										try{
											item = Material.valueOf(itemstring.toUpperCase());
										} catch (java.lang.IllegalArgumentException e){
											item = null;
										}
										boolean materialNotFound = false;
										if(item==null){
											Integer itemid = null;
											try{
												itemid = Integer.valueOf(itemstring);
											} catch(java.lang.NumberFormatException e)
											{
												itemid = null;
											}
											if(itemid!=null){
												item = Material.getMaterial(itemid);
												if(item==null){
													materialNotFound = true;
												}
											} else {
												materialNotFound = true;
											}
										}
										if(!materialNotFound){
											List<Material> filter = chest.getFilter(type);
											if(filter.contains(item)){
												filter.remove(item);
												sender.sendMessage(ChatColor.GREEN.toString()+item.toString()+" is no more being "+type.toString().toLowerCase()+"ed.");
											} else {
												filter.add(item);
												sender.sendMessage(ChatColor.GREEN.toString()+item.toString()+" is now being "+type.toString().toLowerCase()+"ed.");
											}
										} else {
											sender.sendMessage("Material "+itemstring+" not found.");
										}
										save();
									}
								} else {
									log.log(Level.INFO,"No such chest "+cheststring+".");
									syntaxerror = true;
								}
							} else {
								log.log(Level.INFO,"Too much arguments.");
								syntaxerror = true;
							}
						} else if(typestring.equalsIgnoreCase("finish")) {
							if(dplayer!=null)
							{
								dplayer.setEditingFilter(false);
								dplayer.getPlayer().sendMessage(ChatColor.GREEN.toString()+"You're now leaving interactive mode!");
							} else {
								sender.sendMessage("You can't use interactive mode from a console!");
							}
						} else {
							log.log(Level.INFO,"Filter type not found.");
							syntaxerror = true;
						}
					}
				}else {
					log.log(Level.INFO, "Command not found.");
					syntaxerror = true;
				}
			} else {
				log.log(Level.INFO, "Argument count invalid.");
				syntaxerror = true;
			}
			if(syntaxerror){
				if(1==2&&onPermissionSend(sender, "dropchest", ChatColor.BLUE.toString()+"DropChest Usage:")){
					sender.sendMessage(ChatColor.BLUE.toString()+"{this} is a required variable argument");
					sender.sendMessage(ChatColor.BLUE.toString()+"[this] is an optional argument");
					onPermissionSend(sender, "dropchest.create", " /dropchest add [radius]: Add a chest to the list, default radius is 2");
					onPermissionSend(sender, "dropchest.remove", " /dropchest remove {chestid} : Remove a chest from the list");
					onPermissionSend(sender, "dropchest.list", " /dropchest list [page] : Lists all DropChests");
					onPermissionSend(sender, "dropchest.radius.set", " /dropchest setradius {chestid} {radius} : Sets the suck-radius of the chest");
					onPermissionSend(sender, "dropchest.which", " /dropchest which : Check if a Chest is a DropChest and which id it has");
					onPermissionSend(sender, "dropchest.teleport", " /dropchest tp {chestid} : Teleports you to DropChest with ID chestid");
					onPermissionSend(sender, "dropchest.filter", "/dropchest filter {chestid} {suck|push|pull} {itemid|itemtype|clear} : Adds/Removes the given Item to/from the given filter or clears it");
					int max = getMaximumRadius(player);
					String maxs = String.valueOf(max);
					if(hasPermission(player, "dropchest.radius.setBig")||max==65536){
						maxs = "unlimited";
					}
					sender.sendMessage("Your maximum radius is "+maxs);
				}
				log.log(Level.WARNING,"Syntax error.");
			}
		}
		return false;
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
			if(Permissions==null){
				return 65536;
			}
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
		if(worldid==0){
			return getServer().getWorlds().get(0);
		}
		return null;
	}

	public Location locationOf(Block block){
		Location ret = new Location(block.getWorld(), block.getX(), block.getY(), block.getZ());
		return ret;
	}

	public boolean locationsEqual(Location loc1, Location loc2){
		return loc1.getWorld().getId()==loc2.getWorld().getId()&&loc1.getX()==loc2.getX()&&loc1.getY()==loc2.getY()&&loc1.getZ()==loc2.getZ();
	}

	public DropChestItem getChestByBlock(Block block)
	{
		for(DropChestItem dcic : chests){
			if(locationsEqual(dcic.getBlock().getLocation(), block.getLocation())){
				return dcic;
			}
		}
		return null;
	}
	
	public DropChestItem getChestById(int id){
		for(DropChestItem dci : chests)
		{
			if(dci.getId()==id){
				return dci;
			}
		}
		return null;
	}
}

