package net.croxis.plugins.research;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TechLearnEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Tech tech;
	public TechLearnEvent(Tech tech) {
		super();
		this.tech = tech;
	}
	
	public HandlerList getHandlers() {
	    return handlers;
	}
	 
	public static HandlerList getHandlerList() {
	    return handlers;
	} 
	
	public Tech getTech(){
		return this.tech;
	}
}
