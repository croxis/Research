package net.croxis.plugins.research;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Player;

/**
 * @author croxis
 *
 */
public class TechManager {
	public static HashMap <String, Tech> techs = new HashMap<String, Tech>();
	public static HashMap <Player, RPlayer> players = new HashMap<Player, RPlayer>();
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
		rplayer.cantPlace.addAll(plugin.cantPlace);
		rplayer.cantBreak.addAll(plugin.cantBreak);
		rplayer.cantCraft.addAll(plugin.cantCraft);
		rplayer.permissions.addAll(plugin.permissions);
		players.put(player, rplayer);
		SQLPlayer sqlplayer = getSQLPlayer(player);
		for(String techName : sqlplayer.getResearched().split(",")){
			applyLearnedTech(player, techs.get(techName));
		}
	}
	
	public static void unloadPlayer(Player player){
		//TODO: Remove permission nodes
		players.remove(player);
	}
	
	
	/**
	 * Adds points to a player existing total. If enough pointed are earned the tech is learned.
	 * Returns true if tech learned, return false if not. 
	 * @param player
	 * @param points
	 * @return
	 */
	public static boolean addPoints(Player player, int points){
		SQLPlayer sqlplayer = getSQLPlayer(player);
		int currentPoints = sqlplayer.getCurrentPoints();
		currentPoints += points;
		String techName = sqlplayer.getCurrentResearch();
		Tech tech = techs.get(techName);
		if(currentPoints >= tech.cost){
			applyLearnedTech(player, tech);
			sqlplayer.setCurrentResearch(sqlplayer.getCurrentResearch() + "," + tech.name);
			sqlplayer.setCurrentPoints(currentPoints - tech.cost);
			sqlplayer.setCurrentResearch(null);
			plugin.getDatabase().save(sqlplayer);
			return true;
		}
		return false;
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
		RPlayer rplayer = players.get(player);
		rplayer.cantPlace.removeAll(tech.canPlace);
		rplayer.cantBreak.removeAll(tech.canPlace);
		rplayer.cantCraft.removeAll(tech.canPlace);
		rplayer.permissions.addAll(tech.permissions);
		//TODO: Process permission nodes
	}
	
	/**
	 * Manually adds and implaments a new tech.
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
	 * Returns persistance of a player.
	 * Not intended for public consumption.
	 * @param player
	 * @return
	 */
	public static SQLPlayer getSQLPlayer(Player player){
		SQLPlayer sqlplayer = plugin.getDatabase().find(SQLPlayer.class).where().ieq("name", player.getName()).findUnique();
		if (sqlplayer == null){
			sqlplayer = new SQLPlayer();
			sqlplayer.setPlayerName(player.getName());
			sqlplayer.setCurrentPoints(0);
			plugin.getDatabase().save(sqlplayer);
		}
		return sqlplayer;
	}
	
	public static boolean startResearch(Player player, String techName){
		if(!techs.containsKey(techName))
			return false;
		return startResearch(player, techs.get(techName));
	}

	public static boolean startResearch(Player player, Tech tech) {
		SQLPlayer sqlplayer = getSQLPlayer(player);
		String learned = sqlplayer.getResearched();
		String[] ll = learned.split(",");
		List<String> learnedList = Arrays.asList(ll);
		
		for(Tech parent : tech.parents){
			if(!learnedList.contains(parent.name))
				return false;
		}
		
		sqlplayer.setCurrentResearch(tech.name);
		plugin.getDatabase().save(sqlplayer);
		//TODO: Check if enough points to complete tech.
		
		return true;
	}

}
