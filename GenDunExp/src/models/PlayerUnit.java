package models;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class PlayerUnit extends Unit {

	private String allowedequips;
	private String clas;
	private int uid;
	Scanner cmd;
	
	private String validinputs;	//for keyboard commands
	
	//a, b, c, d, e, f, g, h
	private Equipment[] gear;
	private int[] gearMod;
	private int[] gearBon;
	
	private int cexp;	//current experience
	private int texp;	//experience for next level
	//max hp, max mana, max stamina per level
	private int[] lvupres;
	//strength, endurance, agility, dexterity, intelligence, willpower, initiative per level
	private int[] lvupstats;
	
	private User owner;
	
	public PlayerUnit(String n, String cla, int lv, User o) {
		super(n);
		clas = cla;
		owner = o;
		cexp = 0;
		texp = 100;
		uid = owner.getNextEID();
		owner.addCharacter(this);
		raa = new Random();
		cmd = new Scanner(System.in);
		
		validinputs = "xb";
		allowedequips = "abcd";
		
		clas = cla;
		switch (clas) {
		case "Warrior": lvupres = new int[] {25, 5, 15}; lvupstats = new int[] {2, 4, 0, 1, 0, 1, 0}; baseHit = 70; baseDodge = 0; validinputs += "c"; allowedequips += "eh";break;
		case "Swordsman": lvupres = new int[] {20, 10, 15}; lvupstats = new int[] {3, 2, 1, 1, 0, 1, 0}; baseHit = 75; baseDodge = 5; validinputs += "c"; allowedequips += "e";break;
		case "Sorcerer": lvupres = new int[] {10, 25, 10}; lvupstats = new int[] {0, 0, 1, 1, 4, 2, 0}; baseHit = 65; baseDodge = -5; allowedequips += "f"; break;
		case "Priest": lvupres = new int[] {15, 15, 15}; lvupstats = new int[] {0, 1, 1, 0, 3, 3, 0}; baseHit = 70; baseDodge = 0; validinputs += "h"; allowedequips += "f"; break;
		case "Archer": lvupres = new int[] {15, 10, 20}; lvupstats = new int[] {2, 0, 3, 3, 0, 0, 0}; baseHit = 77; baseDodge = 7; allowedequips += "g"; break;
		case "Monk": lvupres = new int[] {25, 0, 20}; lvupstats = new int[] {3, 3, 0, 0, 0, 2, 0}; baseHit = 70; baseDodge = 3; validinputs += "c"; break;
		}
		
		for (int i = 0; i < lv; i ++) {
			levelUP();
		}
		
		gear = new Equipment[8];
		gearMod = new int[7];
		gearBon = new int[5];
		
	}
	
	public Equipment[] ges() {
		return this.gear;
	}
	
	public int getUID() {
		return this.uid;
	}
	
	public int getPriority() {
		//frozen, cannot take action
		if (conditions[1] > 0) {
			return -1;
		}
		//slowed, reduced priority
		if (conditions[2] > 0) {
			return (int) 0.5*(stats[2] + gearMod[2] + stats[6] + gearMod[6]);
		}
		//normal priority
		return stats[2] + gearMod[2] + stats[6] + gearMod[6];
	}
	
	public int turn(Unit[] playerParty, Unit[] enemyParty) {
		int r = super.turn(playerParty, enemyParty);
		
		//dead or frozen, skip turn
		if (r == 0 || r == -1) {
			return 0;
		}
		
		//present availiable actions
		boolean okaction = false;
		String action = "";
		String target = "";
		boolean redo = true;	//redo action selection
		boolean redoinner = true;	//redo target selection
		
		while (redo) {
			redo = false;
			redoinner = true;
			okaction = false;
			
			//select action
			while (!okaction) {
				System.out.println("What will " + name + " do?");
				
				//list all abilities current PlayerUnit has
				String vidupe = validinputs;
				for (int i = 0; i < vidupe.length(); i ++) {
					char it = vidupe.charAt(i);
					String okmsg = ""; String badmsg = "";
					int hpc = 0; int mpc = 0; int spc = 0;
					
					switch (it) {
					case 'x' : okmsg = "Basic Attack (x)"; break;
					case 'c' : okmsg = "Heavy Strike [-10 SP] (c)"; badmsg = "---Insufficient SP (10) for Heavy Strike---"; spc = 10; break;
					case 'h' : okmsg = "Heal [-15 MP] (h)"; badmsg = "---Insufficient MP (15) for Heal---"; mpc = 15; break;
					case 'b' : okmsg = "Rest [restore 10% max MP and SP] (b)"; break;
					}
					
					if (resources[0] >= hpc && resources[2] >= mpc && resources[4] >= spc) {
						System.out.println(okmsg);
					} else {
						System.out.println(badmsg);
						vidupe = vidupe.substring(0, vidupe.indexOf(it)) + vidupe.substring(vidupe.indexOf(it) + 1, vidupe.length());
					}

				}
				
				//recieve user input and validate it
				action = cmd.nextLine();
				action.toLowerCase();
				if (action.length() != 1 || vidupe.indexOf(action) == -1 || action.compareTo("") == 0) {
					System.out.println("Please select a valid action");
				} else {
					okaction = true;
				}
			}
			
			//carry out action
			while (redoinner) {
				redoinner = false;
				
				String epabilities = "xc";	//abilities that target enemyParty members
				String ppabilities = "h";	//abilities that target playerParty members
				String nir = "b";	//abilities that do not require specifying target
				Unit[] targetParty = {};
				
				//print correct target selection if needed
				if (epabilities.indexOf(action) > -1 && action.compareTo("") != 0) {
					printTargets(enemyParty);
					targetParty = enemyParty;
				} else if (ppabilities.indexOf(action) > -1 && action.compareTo("") != 0) {
					printTargets(playerParty);
					targetParty = playerParty;
				}
				
				//if action is one that does not require a target selection, carry it out
				if (nir.indexOf(action) > -1 && action.compareTo("") != 0) {
					if (action.compareTo("b") == 0) {
						this.rest();
					}
				} else {
					//get target, check if it's 'cancel' and attempt to complete action
					target = cmd.nextLine();
					target = target.toLowerCase();
					if (target.compareTo("p") == 0) {
						redo = true;
						break;
					}
					
					//if not cancel proceed to target validation
					if (validateTargets(target, targetParty)) {
						//validated, perform action on target
						if (action.compareTo("x") == 0) {
							this.basicAttack(targetParty[Integer.parseInt(target)]);
						} else if (action.compareTo("c") == 0) {
							this.heavyStrike(targetParty[Integer.parseInt(target)]);
						} else if (action.compareTo("h") == 0) {
							this.heal(targetParty[Integer.parseInt(target)]);
						}
					} else {
						//invalid target, redo target selection
						redoinner = true;
					}
				}
				
			}

		}

		return 1;
	}
	
	//print out enemies
	public void printTargets(Unit[] t) {
		System.out.println("Select a target, 'p' to cancel");
		for (int i = 0; i < t.length; i ++) {
			if (t[i] != null && t[i].isAlive()) {
				System.out.println(t[i] + " (" + i + ")");
			}
		}
	}
	
	//check if target selected is ok
	public boolean validateTargets(String t, Unit[] targets) {
		try {
			int a2i = Integer.parseInt(t);

			if (targets[a2i] == null || !targets[a2i].isAlive()) {
				//array oob or invalid selection (target dead / nonexistant)
				System.out.println("Please select a valid target");
				return false;
			} else {
				//valid target, execute action
				return true;
			}
			
		} catch (Exception e) {
			//could not parse incoming arg as int, automatically invalid, or array oob
			System.out.println("Please select a valid target");
			return false;
		}
	}
	
	//calling unit performs a heavy strike on target
	//no hit chance modifiers, attacking unit's damage amplified by 50% onto target
	public void heavyStrike(Unit target) {
		this.updateRes(0, 0, -10, 0);
		System.out.println(name + " uses Heavy Strike on " + target);
		int threshold = this.hitChance() - target.dodgeChance();
		
		if (raa.nextInt(100) <= threshold) {
			int damage = Math.max(0, (int) (1.5 * this.physicalBaseDmg()) - target.physicalBaseDef());
			System.out.println(name + " hit! " + damage + " damage dealt");
			target.updateRes(-damage, 0, 0, 0);
			if (!target.isAlive()) {
				System.out.println(target + " died!");
			}
		} else {
			System.out.println(name + " missed!");
		}
	}
	
	public void gainEXP(int exp) {
		int t = exp;
		int c = 0;
		while (t > 0) {
			c = Math.min(t, texp - cexp);
			t -= c;
			if (cexp + c == texp) {
				levelUP();
			} else {
				cexp += c;
			}
		}
	}
	
	public void levelUP() {
		level ++;
		cexp = 0;
		texp = level * 100;
		
		//extra stats + res every 5 levels
		if (level % 5 == 0) {
			for (int i = 0; i < 7; i ++) {
				stats[i] += 1;
			}
			resources[1] += 10;
			resources[3] += 10;
			resources[5] += 10;
		}
		
		//increase max resources, refresh current resources to max
		resources[1] += lvupres[0];
		resources[3] += lvupres[1];
		resources[5] += lvupres[2];
		resources[0] = resources[1];
		resources[2] = resources[3];
		resources[4] = resources[5];
		
		//increase stats
		for (int i = 0; i < 7; i ++) {
			stats[i] += lvupstats[i];
		}

	}
	
	//returns the valid equipment slots of the character in string form, prints out equipment slots
	public String showEquips() {
		String vis = "0123";	//all chars have head body legs arms available
		int gi;
		for (int i = 0; i < allowedequips.length(); i ++) {
			gi = i;
			char t = allowedequips.charAt(i);
			switch (t) {
			case 'e' : vis += "4"; gi = 4; break;
			case 'f' : vis += "5"; gi = 5; break;
			case 'g' : vis += "6"; gi = 6; break;
			case 'h' : vis += "7"; gi = 7; break;
			}
			if (gear[gi] != null) {
				System.out.println(gear[gi] + " (" + gi + ")");
			} else {
				switch (gi) {
				case 0 : System.out.println("---No Headgear Equipped--- (" + gi + ")"); break;
				case 1 : System.out.println("---No Armor Equipped--- (" + gi + ")"); break;
				case 2 : System.out.println("---No Armwear Equipped--- (" + gi + ")"); break;
				case 3 : System.out.println("---No Leggings Equipped--- (" + gi + ")"); break;
				case 4 : System.out.println("---No Sword Equipped--- (" + gi + ")"); break;
				case 5 : System.out.println("---No Staff Equipped--- (" + gi + ")"); break;
				case 6 : System.out.println("---No Bow Equipped--- (" + gi + ")"); break;
				case 7 : System.out.println("---No Shield Equipped--- (" + gi + ")"); break;
				}
			}
		}
		return vis;
	}
	
	//also functions as unequip if e = null
	public void equip(char slot, Equipment e) {
		int index = allowedequips.indexOf(slot);
		if (index == -1) {
			//equipment cannot be equipped by this character
		} else {
			switch (slot) {
			case 'e': index = 4; break;
			case 'f': index = 5; break;
			case 'g': index = 6; break;
			case 'h': index = 7; break;
			}
			if (gear[index] != null) {
				owner.putInStorage(gear[index]);
			}
			gear[index] = e;
			if (e != null) {
				e.setSInd(-1);
			}
			System.out.println(gear[index]);
			updateEquipMods();
			updateEquipBons();
		}
	}
	
	//returns stat modifiers of all equipment summed up, maybe make private if nobody outside PlayerUnit uses this
	public void updateEquipMods() {
		int[] t = new int[7];
		for (int i = 0; i < 8; i ++) {
			if (gear[i] != null) {
				int[] e = gear[i].getMods();
				for (int ii = 0; ii < 7; ii++) {
					t[ii] += e[ii];
				}
			}
		}
		this.gearMod = t;
	}
	
	//returns bonus modifiers of all equipment summed up, maybe make private if nobody outside PlayerUnit uses this
	public void updateEquipBons() {
		int[] t = new int[5];
		for (int i = 0; i < 8; i ++) {
			if (gear[i] != null) {
				int[] e = gear[i].getBons();
				for (int ii = 0; ii < 5; ii++) {
					t[ii] += e[ii];
				}
			}
		}
		this.gearBon = t;
	}
	
	//in addition to returning whether this PlayerUnit is alive or dead, remove it from the owner player's
	//characters pool if dead
	public boolean isAlive() {
		boolean r = super.isAlive();
		if (!r) {
			owner.removeCharacter(this);
		}
		return r;
	}
	
	//overwrites on methods from Unit since PlayerUnits have equipment that changes these calculations
	public int hitChance() {
		return (int) (super.hitChance() + gearMod[3] + 0.5 * gearMod[2] + gearBon[2]);
	}
	
	public int dodgeChance() {
		return (int) (super.dodgeChance() + gearMod[2] + gearBon[3]);
	}
	
	public int blockChance() {
		return (int) (super.blockChance() + (0.25 * (gearMod[2] + gearMod[3] + gearMod[6])) + gearBon[4]);
	}
	
	public int physicalBaseDmg() { 
		return (int) (2 * stats[0] + 2 * gearMod[0] + gearBon[0]);
	}
	
	public int magicalBaseDmg() {
		return (int) (2 * stats[4] + 2 * gearMod[4] + gearBon[0]);
	}
	
	public int physicalBaseDef() { 
		return (int) (0.2 * (stats[0] + gearMod[0]) + 0.35 * (stats[1] + gearMod[1]) + gearBon[1]);
	}
	
	public int magicalBaseDef() {
		return (int) (0.15 * (stats[4] + gearMod[4]) + 0.25 * (stats[5] + gearMod[5]) + gearBon[1]);
	}
	
	public String toString() {
		return clas + " " + super.toString();
	}
	
	public void printAttributes() {
		System.out.println(name + " | Lv " + level + " " + clas + " | EXP : " + cexp + " / " + texp);
		System.out.println("HP : " + resources[0] + "/" + resources[1] + " | MP : " +
		resources[2] + "/" + resources[3] + " | SP : " + resources[4] + "/" + resources[5]);
		System.out.println("STR " + stats[0] + " | END  " + stats[1] + " | AGI " + stats[2] + " | DEX " + stats[3] +  " | INT " + stats[4] +  " | WIL " + stats[5]);
		System.out.println("Bonuses from Equipment -");
		System.out.println("STR " + gearMod[0] + " | END  " + gearMod[1] + " | AGI " + gearMod[2] + " | DEX " + gearMod[3] +  " | INT " + gearMod[4] +  " | WIL " + gearMod[5]);
		System.out.println("ATK " + gearBon[0] + " | DEF " + gearBon[1] + " | ACC " + gearBon[2] + " | EVA " + gearBon[3] + " | BLO " + gearBon[4]);
	}
	
}
