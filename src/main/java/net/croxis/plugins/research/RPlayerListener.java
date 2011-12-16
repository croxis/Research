package net.croxis.plugins.research;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

public class RPlayerListener extends PlayerListener{
	private Research plugin;
	
	public RPlayerListener(Research plugin) {
		super();
		this.plugin = plugin;
	}



	@Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().sendMessage("Research placeholder.");
    }
}
