package net.croxis.plugins.research;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.validation.NotNull;

@Entity()
@Table(name = "research_players")
public class SQLPlayer {

	@Id
	private int id;
	@NotNull
	private String playerName;
	@NotNull
	private int currentPoints;
	private String currentResearch;
	@NotNull
	private String researched;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public int getCurrentPoints() {
		return currentPoints;
	}
	public void setCurrentPoints(int currentPoints) {
		this.currentPoints = currentPoints;
	}
	public String getCurrentResearch() {
		return currentResearch;
	}
	public void setCurrentResearch(String currentResearch) {
		this.currentResearch = currentResearch;
	}
	public String getResearched() {
		return researched;
	}
	public void setResearched(String researched) {
		this.researched = researched;
	}
	
	
}
