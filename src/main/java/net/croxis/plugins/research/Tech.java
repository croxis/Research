package net.croxis.plugins.research;

import java.util.ArrayList;
import java.util.List;

public class Tech {
	public String name = "";
	public int cost = 0;
	public ArrayList<Tech> parents = new ArrayList<Tech>();
	public ArrayList<Tech> children = new ArrayList<Tech>();
	public List<String> permissions = new ArrayList<String>();
	public List<Integer> canPlace = new ArrayList<Integer>();
	public List<Integer> canBreak = new ArrayList<Integer>();
	public List<Integer> canCraft = new ArrayList<Integer>();
	public List<String> preReqs = new ArrayList<String>();
	public String description = "";
	
	
}
