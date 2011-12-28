package net.croxis.plugins.research;

import java.util.HashSet;

public class Tech {
	public String name = "";
	public int cost = 0;
	public HashSet<Tech> parents = new HashSet<Tech>();
	public HashSet<Tech> children = new HashSet<Tech>();
	public HashSet<String> permissions = new HashSet<String>();
	public HashSet<Integer> canPlace = new HashSet<Integer>();
	public HashSet<Integer> canBreak = new HashSet<Integer>();
	public HashSet<Integer> canCraft = new HashSet<Integer>();
	public HashSet<Integer> canUse = new HashSet<Integer>();
	public HashSet<String> preReqs = new HashSet<String>();
	public String description = "";
	
	
}
