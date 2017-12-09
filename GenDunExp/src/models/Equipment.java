package models;

import java.util.Random;

public class Equipment {
	private String name;
	
	//a = head, b = body, c = arms, d = legs, e = melee weapon, f = magic weapon, g = ranged weapon, h = shield
	private char type;
	
	//c = common, u = uncommon, r = rare, e = epic, l = legendary
	private char rarity;
	private int level;
	
	//modifies unit's stats
	private int[] modifiers;
	
	//attack, defense, accuracy, evasion, block
	private int[] bonuses;
	
	private int value;	//money value
	
	private int id;	//unique identifier for each piece of equipment
	private int storageindex;	//which index of User.equipmentstorage this item is in, -1 if equipped on a unit
	
	//make a random new piece of equipment after player party defeats an enemy party, need to input level for gameplay reasons
	public Equipment(int level, int weight, int id) {
		Random r = new Random();
		int temp = r.nextInt(100);
		temp = temp + weight;
		this.level = level;
		value = level * 10;
		this.id = id;
		name = "Lv " + level + " ";
		
		//determine rarity of item
		if (temp > 96) {
			name += "Legendary "; rarity = 'l'; value = value * 25;
		} else if (temp > 90) {
			name += "Epic "; rarity = 'e'; value = value * 15;
		} else if (temp > 75) {
			name += "Rare "; rarity = 'r'; value = value * 7;
		} else if (temp > 40) {
			name += "Uncommon "; rarity = 'u'; value = value * 3;
		} else {
			name += "Common "; rarity = 'c';
		}
		
		//item stat assignment
		int[] rawmods = new int[7];
		int[] rawbons = new int[5];
		
		//determine item type
		temp = r.nextInt(8);
		switch (temp) {
		case 0: name += "Headgear"; type = 'a'; rawmods = new int[] {0, 2, 1, 1, 3, 3, 0}; rawbons = new int[] {0, 3, 1, 1, 0}; break;
		case 1: name += "Armor"; type = 'b'; rawmods = new int[] {0, 4, 1, 0, 0, 3, 0}; rawbons = new int[] {0, 7, 0, 2, 0}; break;
		case 2: name += "Armwear"; type = 'c'; rawmods = new int[] {2, 2, 0, 1, 0, 0, 2}; rawbons = new int[] {2, 2, 2, 2, 0}; break;
		case 3: name += "Leggings"; type = 'd'; rawmods = new int[] {0, 2, 3, 0, 0, 0, 2}; rawbons = new int[] {0, 3, 0, 3, 0}; break;
		case 4: name += "Sword"; type = 'e'; rawmods = new int[] {4, 0, 0, 3, 0, 0, 0}; rawbons = new int[] {7, 0, 3, 0, 0}; break;
		case 5: name += "Staff"; type = 'f'; rawmods = new int[] {0, 0, 0, 0, 7, 0, 1}; rawbons = new int[] {5, 0, 5, 0, 0}; break;
		case 6: name += "Bow"; type = 'g'; rawmods = new int[] {3, 0, 2, 4, 0, 0, 3}; rawbons = new int[] {5, 0, 7, 0, 0}; break;
		case 7: name += "Shield"; type = 'h'; rawmods = new int[] {2, 2, 1, 1, 0, 3, 0}; rawbons = new int[] {0, 5, 0, 10, 20}; break;
		}
		
		int modifier = 0;
		
		switch(rarity) {
		case 'l': modifier = 10; break;
		case 'e': modifier = 7; break;
		case 'r': modifier = 5; break;
		case 'u': modifier = 2; break;
		case 'c': modifier = 1; break;
		}
		
		modifier = (int)(modifier * Math.sqrt(level));
		
		for (int i = 0; i < rawmods.length; i ++) {
			rawmods[i] *= (modifier / 2);
		}
		for (int i = 0; i < rawbons.length; i ++) {
			rawbons[i] *= modifier;
		}
		
		modifiers = rawmods;
		bonuses = rawbons;
	}
	
	//to make an item according to specific parameters, used to recreate equipment on save file load
	public Equipment(char r, int l, char t, int id) {
		this.level = l;
		value = level * 10;
		this.id = id;
		name = "Lv " + level + " ";
		rarity = r;
		type = t;
		
		int modifier = 1;
		
		switch(rarity) {
		case 'l': modifier = 10; name += "Legendary "; value = value * 25; break;
		case 'e': modifier = 7; name += "Epic "; value = value * 15; break;
		case 'r': modifier = 5; name += "Rare "; value = value * 7; break;
		case 'u': modifier = 2; name += "Uncommon "; value = value * 3; break;
		case 'c': modifier = 1; name += "Common "; break;
		}
		
		int[] rawmods = new int[7];
		int[] rawbons = new int[5];
		
		switch (type) {
		case 'a': name += "Headgear"; rawmods = new int[] {0, 2, 1, 1, 3, 3, 0}; rawbons = new int[] {0, 3, 1, 1, 0}; break;
		case 'b': name += "Armor"; rawmods = new int[] {0, 4, 1, 0, 0, 3, 0}; rawbons = new int[] {0, 7, 0, 2, 0}; break;
		case 'c': name += "Armwear"; rawmods = new int[] {2, 2, 0, 1, 0, 0, 2}; rawbons = new int[] {2, 2, 2, 2, 0}; break;
		case 'd': name += "Leggings"; rawmods = new int[] {0, 2, 3, 0, 0, 0, 2}; rawbons = new int[] {0, 3, 0, 3, 0}; break;
		case 'e': name += "Sword"; rawmods = new int[] {4, 0, 0, 3, 0, 0, 0}; rawbons = new int[] {7, 0, 3, 0, 0}; break;
		case 'f': name += "Staff"; rawmods = new int[] {0, 0, 0, 0, 7, 0, 1}; rawbons = new int[] {5, 0, 5, 0, 0}; break;
		case 'g': name += "Bow"; rawmods = new int[] {3, 0, 2, 4, 0, 0, 3}; rawbons = new int[] {5, 0, 7, 0, 0}; break;
		case 'h': name += "Shield"; rawmods = new int[] {2, 2, 1, 1, 0, 3, 0}; rawbons = new int[] {0, 5, 0, 10, 20}; break;
		}
		
		modifier = (int)(modifier * Math.sqrt(level));
		
		for (int i = 0; i < rawmods.length; i ++) {
			rawmods[i] *= (modifier / 2);
		}
		for (int i = 0; i < rawbons.length; i ++) {
			rawbons[i] *= modifier;
		}
		
		modifiers = rawmods;
		bonuses = rawbons;
	}
	
	public String toString() {
		return this.name;
	}
	
	public String stats() {
		return "STR " + modifiers[0] + " END " + modifiers[1] + " AGI " + modifiers[2] + " DEX " + modifiers[3] + " INT " + modifiers[4] +
				" WIL " + modifiers[5] + " |  ATK " + bonuses[0] + " DEF " + bonuses[1] + " HIT " + bonuses[2] + " EVA " + bonuses[3] + 
				" BLO " + bonuses[4];
	}
	
	public int getID() {
		return this.id;
	}
	
	public char getType() {
		return this.type;
	}
	
	public int getSInd() {
		return this.storageindex;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public void setSInd(int si) {
		this.storageindex = si;
	}
	
	public void adjustModifiers(int[] params) {
		for (int i = 0; i < modifiers.length; i ++) {
			modifiers[i] += params[i];
		}
	}
	
	public void adjustBonuses(int[] params) {
		for (int i = 0; i < bonuses.length; i ++) {
			bonuses[i] += params[i];
		}
	}
	
	public int[] getMods() {
		return this.modifiers;
	}
	
	public int[] getBons() {
		return this.bonuses;
	}
}
