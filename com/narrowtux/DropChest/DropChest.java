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
import org.bukkit.World;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.narrowtux.DropChest.EntityWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.Block;
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
	private final DropChestPlayerListener playerListener = new DropChestPlayerListener(this);
	private final DropChestBlockListener blockListener = new DropChestBlockListener(this);
	private final DropChestWorldListener worldListener = new DropChestWorldListener();
	private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	private EntityWatcher entityWatcher;
	private int requestedRadius;
	private Player requestPlayer;
	public PermissionHandler Permissions = null;
	@SuppressWarnings("unused")
	private String version = "0.0";
	private DropChestVehicleListener vehicleListener = new DropChestVehicleListener(this);
	public Logger log;
	private int watcherid;
	public DropChest() {
		// NOTE: Event registration should be done in onEnable not here as all events are unregistered when a plugin is disabled
		requestedRadius = 2;
		DropChestPlayer.plugin = this;
		DropChestWorldListener.plugin = this;
	}


	public void onEnable() {
		log = getServer().getLogger();
		setupPermissions();
		entityWatcher = new EntityWatcher(this);
		watcherid = getServer().getScheduler().scheduleSyncRepeatingTask(this, entityWatcher, 20, 20);
		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.VEHICLE_MOVE, vehicleListener, Priority.Normal, this);
		pm.registerEvent(Type.CHUNK_UNLOAD, worldListener, Priority.Normal, this);

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
		try{
			Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

			if(this.Permissions == null) {
				try{
					this.Permissions = ((Permissions)test).getHandler();
				} catch(Exception e) {
					this.Permissions = null;
					log.log(Level.WARNING, "Permissions is not enabled! All Operations are allowed!");
				}
			}
		} catch(java.lang.NoClassDefFoundError e){
			this.Permissions = null;
			log.log(Level.WARNING, "Permissions not found! All Operations are allowed!");
		}
	}

	private void load(){
		File dir = getDataFolder();
		if(!dir.exists()){
			log.log(Level.INFO, "DropChest directory does not exist. Creating on next save!");
			return;
		}
		File file = new File(dir.getAbsolutePath()+"/DropChest.txt");
		if(file.exists()){
			FileInputStream input;
			try {
				input = new FileInputStream(file.getAbsoluteFile());
				InputStreamReader ir = new InputStreamReader(input);
				BufferedReader r = new BufferedReader(ir);
				String locline;
				String version = "0.0";
				while(true){
					locline = r.readLine();
					if(locline == null){
						break;
					}
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
		File dir = getDataFolder();
		if(!dir.exists()){
			log.log(Level.INFO, "Creating DropChest directory.");
			dir.mkdir();
		}
		File file = new File(dir.getAbsolutePath()+"/DropChest.txt");
		if(!file.exists()){
			log.log(Level.INFO, "no file. Trying to create it.");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			FileOutputStream output = new FileOutputStream(file.getAbsoluteFile());
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(output));
			w.write("version 0.6\n");
			w.write("#LEGEND\n#x, y, z, radius, World-Name, nothing, chestid;Suck-Filter;Pull-Filter;Push-Filter\n");
			w.write("#Filtered items are separated by ','. Name the items like the names in org.bukkit.Material, e.g. COBBLESTONE\n");
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
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Unexpected error.");
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
		getServer().getScheduler().cancelTask(watcherid);
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
		if(cmd.getName().equals("dropchest"))
		{
			boolean syntaxerror=false;
			if(!hasPermission(player, "dropchest")){
				player.sendMessage("You may not use DropChest. Please ask your Operator to enable this awesome plugin for you.");
				return false;
			}
			if(args.length==0){
				syntaxerror = true;
			} else {
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
						DropChestItem dci = getChestByIdOrName(args[1]);
						if(dci!=null){
							chests.remove(dci);
							save();
							sender.sendMessage(ChatColor.RED.toString()+"Removed Chest.");
						} else {
							sender.sendMessage(ChatColor.RED+"Dropchest not found.");
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
					if(needed!=1){
						sender.sendMessage(ChatColor.BLUE.toString()+"Page "+String.valueOf(current)+" of "+ String.valueOf(needed));
					}
					sender.sendMessage(ChatColor.BLUE.toString()+"Name | % full | filters | radius");
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
					DropChestItem dci = getChestByIdOrName(args[1]);
					if(dci!=null){
						if(player!=null){
							player.teleport(dci.getBlock().getLocation());
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
						int radius = Integer.valueOf(args[2]);
						DropChestItem dci = getChestByIdOrName(args[1]);
						if(dci != null){
							boolean force=true;
							if(!hasPermission(player, "dropchest.radius.setBig")){
								force =  false;
							}
							if(radius>getMaximumRadius(player)&&!force){
								radius = getMaximumRadius(player);
							}
							dci.setRadius(radius);
							sender.sendMessage("Radius of Chest #"+dci.getId()+" set to "+String.valueOf(dci.getRadius()));
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
						player.sendMessage("You may not use this command.");
						return false;
					}
					if(player != null){
						DropChestPlayer pl = DropChestPlayer.getPlayerByName(player.getName());
						pl.setChestRequestType(ChestRequestType.WHICH);
						sender.sendMessage(ChatColor.GREEN.toString()+"Now rightclick on a chest to get its properties.");
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
								String itemstring = args[3];
								DropChestItem chest = getChestByIdOrName(args[2]);
								Material item = null;
								if(itemstring.equalsIgnoreCase("clear")){
									chest.getFilter(type).clear();
									sender.sendMessage(ChatColor.GREEN.toString()+"Filter cleared.");
								} else {
									if(chest!=null){
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
	
									} else {
										log.log(Level.INFO,"No such chest "+args[1]+".");
										syntaxerror = true;
									}
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
				} else if(args[0].equalsIgnoreCase("setname")){
					/*****************
					 *    SETNAME    *
					 *****************/
					if(!hasPermission(player, "dropchest"))
					{
						player.sendMessage("You may not set names of Dropchests!");
						return false;
					} 
					if(args.length==3){
						String name = args[2];
						DropChestItem item = getChestByIdOrName(args[1]);
						if(item!=null){
							item.setName(name);
							sender.sendMessage(ChatColor.GREEN+"Set name to "+item.getName());
							save();
						}
					} else {
						syntaxerror = true;
					}
				} else if(args[0].equalsIgnoreCase("protect")){
					if(args.length==2){
						String cheststring = args[1];
						String mode = args[2];
						DropChestItem item = getChestByIdOrName(cheststring);
						if(mode.equalsIgnoreCase("off")){
							
						} else if(mode.equalsIgnoreCase("on")){
							
						} else {
							return false;
						}
						return true;
					}
				} else {
					log.log(Level.INFO, "Command not found.");
					syntaxerror = true;
				}
			}

			if(syntaxerror){
				if(onPermissionSend(sender, "dropchest", ChatColor.BLUE.toString()+"DropChest Commands:")){
					sender.sendMessage(ChatColor.BLUE.toString()+"{this} is a required variable argument");
					sender.sendMessage(ChatColor.BLUE.toString()+"[this] can be omitted");
					sender.sendMessage(ChatColor.BLUE.toString()+"{chest} can be either a name or an id");
					onPermissionSend(sender, "dropchest.create", " /dropchest add [radius=2]");
					onPermissionSend(sender, "dropchest.remove", " /dropchest remove {chest}");
					onPermissionSend(sender, "dropchest.list", " /dropchest list [page=1]");
					onPermissionSend(sender, "dropchest.radius.set", " /dropchest setradius {chest} {radius}");
					onPermissionSend(sender, "dropchest.which", " /dropchest which");
					onPermissionSend(sender, "dropchest.teleport", " /dropchest tp {chest}");
					onPermissionSend(sender, "dropchest.filter", " /dropchest filter {suck|push|pull} [{chest} {itemid|itemtype|clear}]");
					onPermissionSend(sender, "dropchest", " /dropchest setname {chest} {name}");
					int max = getMaximumRadius(player);
					String maxs = String.valueOf(max);
					if(hasPermission(player, "dropchest.radius.setBig")||max==65536){
						maxs = "unlimited";
					}
					sender.sendMessage("Your maximum radius is "+maxs);

				}
			}
		}
		if(cmd.getName().equals("item")){
			if(args.length==1){
				int id = 0;
				Material m = null;
				try{
					id = Integer.valueOf(args[0]);
				} catch(Exception e){
					m = Material.matchMaterial(args[0].toUpperCase());
				}
				if(id!=0){
					m = Material.getMaterial(id);
					if(m!=null){
						sender.sendMessage(ChatColor.YELLOW.toString()+id+ChatColor.WHITE+" is "+ChatColor.YELLOW.toString()+m.toString());
					} else {
						sender.sendMessage(ChatColor.RED+"That item does not exist.");
					}
				} else {
					if(m!=null){
						id = m.getId();
						sender.sendMessage(ChatColor.YELLOW+m.toString()+ChatColor.WHITE+" is "+ChatColor.YELLOW+id);
					} else {
						sender.sendMessage(ChatColor.RED+"That item does not exist.");
					}
				}
				return true;
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
			int max = Permissions.getUserPermissionInteger(player.getName(), "dropchestmaxradius","20");
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

	public DropChestItem getChestByIdOrName(String arg){
		int id = 0;
		DropChestItem dci = null;
		try{
			id = Integer.valueOf(arg);
			dci = getChestById(id);
		} catch(Exception e){
			dci = getChestByName(arg);
		}
		return dci;
	}

	public DropChestItem getChestByName(String name){
		DropChestItem dci = null;
		for(DropChestItem d:chests){
			if(d.getName().equalsIgnoreCase(name)){
				dci = d;
				break;
			}
		}
		return dci;
	}
}

