package models;

import java.util.Random;

public class EnemyUnit extends Unit {
	
	private String race;
	private String type;
	private String grade;
	private int uid;
	private int[] lvupres;
	private int[] sgrowths;
	
	public EnemyUnit(String nam, int lv, int r, int c, int g) {
		super(nam);
		level = lv;
		
		raa = new Random();
		
		//max hp, max mana, max stamina per level
		lvupres = new int[] {0, 0, 0};
		//strength, endurance, agility, dexterity, intelligence, willpower, initiative per level
		sgrowths = new int[] {0, 0, 0, 0, 0, 0, 0};		//stat growths per level
		int[] racemodsres = {};	//race modifiers for resources
		int[] racemodsgrowths = {};	//race modifiers for stats
		int[] typemodsres = {};	//type modifiers for resources
		int[] typemodsgrowths = {};	//type modifiers for stats
		double mult = 1.0;	//multiplier due to grade
		
		//-1 indicates unspecified, randomly generate properties, level must be specified for balance issues
		if (r == -1) {
			r = raa.nextInt(7);
		}
		if (c == -1) {
			c = raa.nextInt(7);
		}
		if (g == -1) {
			g = raa.nextInt(100);
			if (g > 96) {
				grade = "Legendary"; mult = 3.0;
			} else if (g > 90) {
				grade = "Epic"; mult = 2.2;
			} else if (g > 75) {
				grade = "Rare"; mult = 1.7;
			} else if (g > 40) {
				grade = "Uncommon"; mult = 1.3;
			} else {
				grade = "Common";
			}
		} else {
			switch (g) {
			case 0 : grade = "Common"; break;
			case 1 : grade = "Uncommon"; mult = 1.3; break;
			case 2 : grade = "Rare"; mult = 1.7; break;
			case 3 : grade = "Epic"; mult = 2.2; break;
			case 4 : grade = "Legendary"; mult = 3.0; break;
			}
		}
		
		//create stat growth per level arrays according to enemy race and type
		switch (r) {
		case 0 : race = "Zombie"; racemodsres = new int[] {40, 5, 10}; racemodsgrowths = new int[] {1, 4, 1, 1, 0, 0, 0}; baseHit = 40; baseDodge = -15; break; 
		case 1 : race = "Skeleton"; racemodsres = new int[] {20, 15, 15}; racemodsgrowths = new int[] {2, 1, 2, 2, 0, 0, 0}; baseHit = 55; baseDodge = 0; break;
		case 2 : race = "Demon"; racemodsres = new int[] {50, 25, 20}; racemodsgrowths = new int[] {5, 6, 3, 3, 2, 2, 0}; baseHit = 80; baseDodge = 10; break;
		case 3 : race = "Golem"; racemodsres = new int[] {70, 10, 20}; racemodsgrowths = new int[] {4, 6, 1, 1, 0, 0, 0}; baseHit = 50; baseDodge = -10; break;
		case 4 : race = "Orc"; racemodsres = new int[] {40, 15, 25}; racemodsgrowths = new int[] {4, 4, 2, 2, 0, 0, 0}; baseHit = 70; baseDodge = 5; break;
		case 5 : race = "Specter"; racemodsres = new int[] {10, 50, 10}; racemodsgrowths = new int[] {1, 1, 2, 2, 5, 3, 0}; baseHit = 65; baseDodge = 20; break;
		case 6 : race = "Elemental"; racemodsres = new int[] {35, 35, 20}; racemodsgrowths = new int[] {3, 3, 2, 2, 1, 1, 0}; baseHit = 70; baseDodge = 10; break;
		}
		
		switch (c) {
		case 0 : type = "Warrior"; typemodsres = new int[] {15, 0, 5}; typemodsgrowths = new int[] {2, 3, 0, 0, 0, 0, 0}; baseHit -= 5; break;
		case 1 : type = "Mage"; typemodsres = new int[] {0, 25, 0}; typemodsgrowths = new int[] {0, 0, 0, 1, 3, 1, 0}; baseDodge -= 5;break;
		case 2 : type = "Archer"; typemodsres = new int[] {8, 5, 5}; typemodsgrowths = new int[] {2, 0, 2, 1, 0, 0, 0}; baseHit += 10; baseDodge += 5; break;
		case 3 : type = "Shaman"; typemodsres = new int[] {5, 20, 5}; typemodsgrowths = new int[] {0, 0, 1, 0, 2, 2, 0}; baseHit += 5; break;
		case 4 : type = "Assassin"; typemodsres = new int[] {5, 10, 10}; typemodsgrowths = new int[] {3, 0, 2, 1, 0, 0, 0}; baseHit += 10; baseDodge += 10; break;
		case 5 : type = "Spearman"; typemodsres = new int[] {10, 0, 10}; typemodsgrowths = new int[] {1, 3, 1, 1, 0, 0, 0}; baseHit += 5; baseDodge += 5; break;
		case 6 : type = "Pugilist"; typemodsres = new int[] {12, 0, 7}; typemodsgrowths = new int[] {2, 2, 1, 1, 0, 0, 0}; baseHit +=7; baseDodge += 7; break;
		}
		
		//combine race and type modifiers for overall growths per level
		for (int i = 0; i < 3; i ++) {
			lvupres[i] = racemodsres[i] + typemodsres[i];
		}
		for (int i = 0; i < 7; i ++) {
			sgrowths[i] = racemodsgrowths[i] + typemodsgrowths[i];
		}
		
		//scale resources and stats to level + 'grade' multiplier
		resources[1] += (int) (level * mult * lvupres[0]);
		resources[3] += (int) (level * mult * lvupres[1]);
		resources[5] += (int) (level * mult * lvupres[2]);
		resources[0] = resources[1];
		resources[2] = resources[3];
		resources[4] = resources[5];
		
		for (int i = 0; i < 7; i ++) {
			stats[i] += (int) (level * mult * sgrowths[i]);
		}

		//if a custom name was put in
		if (name.compareTo("") != 0) {
			this.name = nam;
		} else {	//generic enemy name
			this.name = grade + " " + race + " " + type;
		}
	}
	
	public int getUID() {
		return this.uid;
	}
	
	public int turn(Unit[] playerParty, Unit[] enemyParty) {
		int i = super.turn(playerParty, enemyParty);
		
		//dead or frozen, skip turn
		if (i == 0 || i == -1) {
			return 0;
		}
		
		//can act, take turn, determine enemy action
		
		//basic attack on a random target
		int target = raa.nextInt(playerParty.length);
		while (playerParty[target] == null || !playerParty[target].isAlive()) {
			target = raa.nextInt(playerParty.length);
		}
		basicAttack(playerParty[target]);
		
		return 1;
	}
	
}
