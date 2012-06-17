package net.croxis.plugins.research;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

/**
 * @author croxis
 *
 */
public class TechManager {
	public static HashMap <String, Tech> techs = new HashMap<String, Tech>();
	public static HashMap <Player, RPlayer> players = new HashMap<Player, RPlayer>();
	public static HashSet<String> permissions;
	public static HashSet<Integer> canPlace;
	public static HashSet<Integer> canBreak;
	public static HashSet<Integer> canCraft;
	public static HashSet<Integer> cantUse;
	private static Research plugin;
	
	public TechManager(Research plugin){
		TechManager.plugin = plugin;
	}
	
	/**
	 * Creates a new player if and only if they do not already exist in memory
	 * 
	 * @param player
	 */
	public static void initPlayer(Player player){
		if(players.containsKey(player))
			return;
		RPlayer rplayer = new RPlayer();
		rplayer.name = player.getName();
		//rplayer.cantPlace.addAll(canPlace);
		//rplayer.cantBreak.addAll(canBreak);
		//rplayer.cantCraft.addAll(canCraft);
		rplayer.cantPlace.removeAll(canPlace);
		rplayer.cantBreak.removeAll(canBreak);
		rplayer.cantCraft.removeAll(canCraft);
		rplayer.cantUse.addAll(cantUse);
		rplayer.permissions.addAll(permissions);
		/*for(int item : plugin.cantPlace)
			rplayer.cantPlace.add(item);
		for(int item : plugin.cantBreak)
			rplayer.cantBreak.add(item);
		for(int item : plugin.cantCraft)
			rplayer.cantCraft.add(item);
		for(String item : plugin.permissions)
			rplayer.permissions.add(item);*/
		players.put(player, rplayer);
		SQLPlayer sqlplayer = getSQLPlayer(player);
		for(String techName : sqlplayer.getResearched().split(",")){
			applyLearnedTech(player, techs.get(techName));
		}
		plugin.logDebug("Init player " + rplayer.name);
		plugin.logDebug("Can't Place: " + rplayer.cantPlace.toString());
	}
	
	public static void unloadPlayer(Player player){
		SQLPlayer sqlplayer = getSQLPlayer(player);
		PermissionAttachment att = player.addAttachment(plugin);
		for(String techName : sqlplayer.getResearched().split(",")){
			Tech tech = techs.get(techName);
			for (String perm : tech.permissions){
				att.setPermission(perm, false);
			}
		}
		player.recalculatePermissions();
		players.remove(player);
	}
	
	
	/**
	 * Adds points to a player existing total. If enough pointed are earned the tech is learned.
	 * Returns Tech if learned, return null if not. 
	 * @param player
	 * @param points
	 * @return
	 */
	public static Tech addPoints(Player player, int points){
		return addPoints(player.getName(), points);
	}
	
	public static Tech addPoints(String playerName, int points){
		SQLPlayer sqlplayer = getSQLPlayer(playerName);
		int currentPoints = sqlplayer.getCurrentPoints();
		currentPoints += points;
		String techName = sqlplayer.getCurrentResearch();
		sqlplayer.setCurrentPoints(currentPoints);
		plugin.getDatabase().save(sqlplayer);
		Tech tech = techs.get(techName);
		if(tech == null)
			return null;
		
		if(currentPoints >= tech.cost){
			applyLearnedTech(plugin.getServer().getPlayer(playerName), tech);
			String researched = sqlplayer.getResearched();
			if (researched.isEmpty())
				sqlplayer.setResearched(tech.name);
			else
				sqlplayer.setResearched(researched + "," + tech.name);
			sqlplayer.setCurrentPoints(currentPoints - tech.cost);
			sqlplayer.setCurrentResearch(null);
			plugin.getDatabase().save(sqlplayer);
			return tech;
		}
		return null;
	}
	
	/**
	 * Sets points to a player existing total. If enough pointed are earned the tech is learned.
	 * Returns true if tech learned, return false if not. 
	 * @param player
	 * @param points
	 * @return
	 */
	public static boolean setPoints(Player player, int points){
		SQLPlayer sqlplayer = getSQLPlayer(player);
		String techName = sqlplayer.getCurrentResearch();
		Tech tech = techs.get(techName);
		sqlplayer.setCurrentPoints(points);
		plugin.getDatabase().save(sqlplayer);
		if(points >= tech.cost){
			applyLearnedTech(player, tech);
			sqlplayer.setResearched(sqlplayer.getResearched() + "," + tech.name);
			sqlplayer.setCurrentPoints(points - tech.cost);
			sqlplayer.setCurrentResearch(null);
			plugin.getDatabase().save(sqlplayer);
			return true;
		}
		return false;
	}
	
	/**
	 * Applies effect of learned tech. Does not persist it.
	 * @param player
	 * @param tech
	 */
	public static void applyLearnedTech(Player player, Tech tech){
		if(tech == null)
			return;
		if(!players.containsKey(player))
			return;
		plugin.logDebug("Applying tech: " + tech.name + " to player " + player.getName());
		RPlayer rplayer = players.get(player);
		plugin.logDebug("Previous cantPlace: " + rplayer.cantPlace.toString());
		rplayer.cantPlace.removeAll(tech.canPlace);
		plugin.logDebug("New cantPlace: " + rplayer.cantPlace.toString());
		plugin.logDebug("Previous cantBreak: " + rplayer.cantBreak.toString());
		plugin.logDebug("Tech canBreak: " + tech.canBreak.toString());
		rplayer.cantBreak.removeAll(tech.canBreak);
		plugin.logDebug("New cantBreak: " + rplayer.cantBreak.toString());
		plugin.logDebug("Previous cantCraft: " + rplayer.cantCraft.toString());
		plugin.logDebug("Tech canCraft: " + tech.canCraft.toString());
		rplayer.cantCraft.removeAll(tech.canCraft);
		plugin.logDebug("New cantCraft: " + rplayer.cantCraft.toString());
		rplayer.cantUse.removeAll(tech.canUse);
		rplayer.permissions.addAll(tech.permissions);
		//TODO: Process permission nodes
		PermissionAttachment att = player.addAttachment(plugin);
		for(String perm : tech.permissions){
			att.setPermission(perm, true);
		}
		player.recalculatePermissions();
	}
	
	/**
	 * Manually adds and implements a new tech.
	 * @param player
	 * @param tech
	 */
	public static void addTech(Player player, Tech tech){
		applyLearnedTech(player, tech);
		SQLPlayer sqlplayer = getSQLPlayer(player);
		sqlplayer.setResearched(sqlplayer.getResearched() + "," + tech.name);
		plugin.getDatabase().save(sqlplayer);
	}
	
	/**
	 * Manually adds and implements a new tech.
	 * @param playerName
	 * @param tech
	 */
	public static void addTech(String playerName, Tech tech){
		OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
		if (player.isOnline())
			applyLearnedTech(player.getPlayer(), tech);
		SQLPlayer sqlplayer = getSQLPlayer(playerName);
		sqlplayer.setResearched(sqlplayer.getResearched() + "," + tech.name);
		plugin.getDatabase().save(sqlplayer);
	}
	
	/**
	 * Add and implements a set of techs
	 * @param playerName
	 * @param techs
	 */
	public static void addTechs(String playerName, HashSet<Tech> techs){
		for(Tech t : techs){
			addTech(playerName, t);
		}
	}
	
	/**
	 * Replaces existing player tech knowledge. If only adding tech please use addTech() instead.
	 * @param player
	 * @param techs
	 */
	public static void setTech(Player player, Tech[] techs){
		SQLPlayer sqlplayer = getSQLPlayer(player);
		String techNames = "";
		Iterator<Tech> techi = Arrays.asList(techs).iterator();
		while(techi.hasNext()){
			techNames += techi.next().name;
			if(techi.hasNext())
				techNames += ",";
		}
		sqlplayer.setResearched(techNames);
		plugin.getDatabase().save(sqlplayer);
		unloadPlayer(player);
		initPlayer(player);
	}
	
	/**
	 * Replaces existing player tech knowledge. If only adding tech please use addTech() instead.
	 * @param player
	 * @param techs
	 */
	public static void setTech(Player player, HashSet<Tech> techs){
		SQLPlayer sqlplayer = getSQLPlayer(player);
		String techNames = "";
		Iterator<Tech> techi = techs.iterator();
		Tech tech;
		while(techi.hasNext()){
			tech = techi.next();
			if (tech != null){
				techNames += tech.name;
				if(techi.hasNext())
					techNames += ",";
			}
		}
		sqlplayer.setResearched(techNames);
		plugin.getDatabase().save(sqlplayer);
		unloadPlayer(player);
		initPlayer(player);
	}
	
	
	/**
	 * Returns persistance of a player.
	 * Not intended for public consumption.
	 * @param player
	 * @return
	 */
	public static SQLPlayer getSQLPlayer(Player player){
		return getSQLPlayer(player.getName());
	}
	
	public static SQLPlayer getSQLPlayer(String playerName){
		SQLPlayer sqlplayer = plugin.getDatabase().find(SQLPlayer.class).where().ieq("player_name", playerName).findUnique();
		if (sqlplayer == null){
			sqlplayer = new SQLPlayer();
			sqlplayer.setPlayerName(playerName);
			sqlplayer.setCurrentPoints(0);
			sqlplayer.setResearched("");
			plugin.getDatabase().save(sqlplayer);
		}
		return sqlplayer;
	}
	
	public static boolean startResearch(String playerName, String techName){
		if(!techs.containsKey(techName))
			return false;
		return startResearch(playerName, techs.get(techName));
	}
	
	public static boolean startResearch(String playerName, Tech tech){
		SQLPlayer sqlplayer = getSQLPlayer(playerName);
		String learned = sqlplayer.getResearched();
		String[] ll = learned.split(",");
		List<String> learnedList = Arrays.asList(ll);
		
		for(Tech parent : tech.parents){
			if(!learnedList.contains(parent.name))
				return false;
		}
		
		if(sqlplayer.getCurrentResearch() != null)
			sqlplayer.setCurrentPoints(0);
		
		sqlplayer.setCurrentResearch(tech.name);
		plugin.getDatabase().save(sqlplayer);
		//TODO: Check if enough points to complete tech.
		
		return true;
	}
	
	public static boolean startResearch(Player player, String techName){
		if(!techs.containsKey(techName))
			return false;
		return startResearch(player, techs.get(techName));
	}

	public static boolean startResearch(Player player, Tech tech) {
		return startResearch(player.getName(), tech);
	}
	
	public static boolean canResearch(Player player, Tech tech){
		return getAvailableTech(player).contains(tech);
	}
	
	/**
	 * Returns a list of Techs that a player can research next
	 * @param player
	 * @return
	 */
	public static HashSet<Tech> getAvailableTech(Player player){
		return getAvailableTech(player.getName());
	}
	
	/**
	 * Returns a list of Techs that a player can research next
	 * @param playerName
	 * @return
	 */
	public static HashSet<Tech> getAvailableTech(String playerName){
		HashSet<Tech> unknowns = new HashSet<Tech>();
		HashSet<Tech> available = new HashSet<Tech>();
		HashSet<Tech> researched = getResearched(playerName);
		for(Tech t : techs.values()){
			if(!researched.contains(t))
				unknowns.add(t);
		}
		
		for(Tech t : unknowns){
			boolean avail = true;
			for(Tech parent : t.parents){
				if(!researched.contains(parent))
					avail = false;
			}
			if(avail)
				available.add(t);
		}
		
		plugin.logDebug("AvailableTech for " + playerName);
		plugin.logDebug("unknown: " + unknowns.toString());
		plugin.logDebug("available: " + available.toString());
		plugin.logDebug("researched: " + researched.toString());
		
		return available;
		
	}
	
	/**
	 * Returns a list of technologies the player has learned
	 * @param player
	 * @return
	 */
	public static HashSet<Tech> getResearched(Player player){
		return getResearched(player.getName());
	}
	
	/**
	 * Returns a list of technologies the player has learned
	 * @param playerName
	 * @return
	 */
	public static HashSet<Tech> getResearched(String playerName){
		SQLPlayer sqlplayer = getSQLPlayer(playerName);
		String learned = sqlplayer.getResearched();
		HashSet<Tech> ts = new HashSet<Tech>();
		if(learned.equalsIgnoreCase(""))
			return ts;
		
		String[] ll = learned.split(",");
		for (String techName : ll){
			ts.add(techs.get(techName));
		}
		return ts;
	}
	
	
	
	public static int getPoints(Player player){
		SQLPlayer sqlplayer = getSQLPlayer(player);
		return sqlplayer.getCurrentPoints();
	}
	
	public static int getPoints(String playerName){
		SQLPlayer sqlplayer = getSQLPlayer(playerName);
		return sqlplayer.getCurrentPoints();
	}
	
	public static Tech getCurrentResearch(Player player){
		SQLPlayer sqlplayer = getSQLPlayer(player);
		return techs.get(sqlplayer.getCurrentResearch());
	}
	
	public static Tech getCurrentResearch(String playerName){
		SQLPlayer sqlplayer = getSQLPlayer(playerName);
		return techs.get(sqlplayer.getCurrentResearch());
	}

}
