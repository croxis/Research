package net.croxis.plugins.research;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Research extends JavaPlugin {
	static TechManager techManager;
	public boolean debug = false;
	
	public Logger logger;
	
	private FileConfiguration techConfig = null;
	//private File techConfigFile = new File(getDataFolder(), "tech.yml");
	private File techConfigFile = null;
	private RBlockListener blockListener = new RBlockListener();
	private RPlayerListener playerListener = new RPlayerListener();
	
    public void onDisable() {
        // TODO: Place any custom disable code here.
        System.out.println(this + " is now disabled!");
    }
    
    public void logDebug(String message){
    	if(debug)
    		logger.log(Level.INFO, "[Research - Debug] " + message);
    }
    
    public void logInfo(String message){
    	logger.log(Level.INFO, "[Research] " + message);
    }
    
    public void logWarning(String message){
    	logger.log(Level.WARNING, "[Research] " + message);
    }

	public void onEnable() {
    	logger = Logger.getLogger(JavaPlugin.class.getName());
    	techManager = new TechManager(this);
    	// Set up default systems
    	debug = this.getConfig().getBoolean("debug", false);
    	TechManager.permissions = new HashSet<String>(this.getConfig().getStringList("default.permissions"));
    	TechManager.cantPlace = new HashSet<Integer>( this.getConfig().getIntegerList("default.cantPlace"));
    	TechManager.cantBreak = new HashSet<Integer>( this.getConfig().getIntegerList("default.cantBreak"));
    	TechManager.cantCraft = new HashSet<Integer>( this.getConfig().getIntegerList("default.cantCraft"));
    	TechManager.cantUse = new HashSet<Integer>( this.getConfig().getIntegerList("default.cantUse"));
    	getConfig().options().copyDefaults(true);
        saveConfig();
        logInfo("Loaded default permissions. Now loading techs.");        
        
        // Load tech config
        this.reloadTechConfig();
        getTechConfig().options().copyDefaults(true);
        saveTechConfig();
        
        Set<String> techNames = techConfig.getKeys(false);
        int i = 0;
        for (String techName : techNames){
        	Tech tech = new Tech();
        	tech.name = techName;
        	if(!techConfig.contains(techName + ".cost"))
        		continue;
        	logDebug("Loading " + tech.name + " with recorded cost " + Integer.toString(techConfig.getInt(techName + ".cost")));
        	tech.cost = techConfig.getInt(techName + ".cost");        	
        	if(techConfig.contains(techName + ".permissions"))
        		tech.permissions = new HashSet<String>( techConfig.getStringList(techName + ".permissions"));        	
        	if(techConfig.contains(techName + ".description"))
        		tech.description = techConfig.getString(techName + ".description");
        	if(techConfig.contains(techName + ".prereqs"))
        		tech.preReqs = new HashSet<String>( techConfig.getStringList(techName + ".prereqs"));        	
        	if(techConfig.contains(techName + ".canPlace"))
        		tech.canPlace = new HashSet<Integer>( techConfig.getIntegerList(techName + ".canPlace"));        	
        	if(techConfig.contains(techName + ".canBreak"))
        		tech.canBreak = new HashSet<Integer>( techConfig.getIntegerList(techName + ".canBreak"));        	
    		if(techConfig.contains(techName + ".canCraft"))
        		tech.canCraft = new HashSet<Integer>( techConfig.getIntegerList(techName + ".canCraft"));
    		if(techConfig.contains(techName + ".canUse"))
        		tech.canUse = new HashSet<Integer>( techConfig.getIntegerList(techName + ".canUse"));
        	
        	techManager.techs.put(techName, tech);
        	i++;
        }        
        logInfo(Integer.toString(i) + " techs loaded. Now linking tree.");
        
        for (Tech tech : techManager.techs.values()){
        	// Check if it is a starting tech
        	//if(tech.preReqs.isEmpty()){
        	//	tech.parents.add(techManager.techs.get("root"));
        	//	techManager.techs.get("root").children.add(tech);
        	//} else {
        		for(String parentName : tech.preReqs){
        			if(techManager.techs.containsKey(parentName)){
        				tech.parents.add(techManager.techs.get(parentName));
        				techManager.techs.get(parentName).children.add(tech);
        			} else {
        				this.logWarning("Could not link " + tech.name + " with " + parentName + ". One of them is malformed!");
        			}
        		}
        	//}
        }
        logInfo("Tech tree linking complete. Mounting player database.");
        setupDatabase();
        logInfo("Database mounted. Setup complete.");
        
        // This should be it. Permission setups should happen onPlayerJoin
        
        this.getServer().getPluginManager().registerEvents(playerListener, this);
        this.getServer().getPluginManager().registerEvents(blockListener, this);
        this.getServer().getPluginManager().registerEvents(blockListener, this);
        this.getServer().getPluginManager().registerEvents(new RInventoryListener(), this);
        
        System.out.println(this + " is now enabled!");
    }
    
    public static TechManager getTechManager(){
    	return techManager;
    }
    
    public void reloadTechConfig() {
    	if (techConfigFile == null) {
    	    techConfigFile = new File(getDataFolder(), "tech.yml");
    	    }
        techConfig = YamlConfiguration.loadConfiguration(techConfigFile);
     
        // Look for defaults in the jar
        InputStream defConfigStream = getResource("tech.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            techConfig.setDefaults(defConfig);
        }
    }
    
    public FileConfiguration getTechConfig() {
        if (techConfig == null) {
            reloadTechConfig();
        }
        return techConfig;
    }
    
    public void saveTechConfig() {
        try {
            techConfig.save(techConfigFile);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not save config to " + techConfigFile, ex);
        }
    }
    
    private void setupDatabase() {
        try {
            getDatabase().find(SQLPlayer.class).findRowCount();
        } catch (PersistenceException ex) {
            System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
            installDDL();
        }
    }
    
    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(SQLPlayer.class);
        return list;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
    	if(args.length == 0)
    		return false;
    	if(args[0].equalsIgnoreCase("player") && args.length == 2){
    		RPlayer rplayer = TechManager.players.get(getServer().getPlayer(args[1]));
    		sender.sendMessage("[Research] Debug info for " + args[1]);
    		sender.sendMessage("CantPlace: " + rplayer.cantPlace.toString());
    		sender.sendMessage("CantBreak: " + rplayer.cantBreak.toString());
    		sender.sendMessage("CantCraft: " + rplayer.cantCraft.toString());
    		sender.sendMessage("CantUse: " + rplayer.cantUse.toString());
    	}
    	return true;
    }
}
