package models;

import java.util.Random;

public class Unit implements Comparable<Unit>{
	String name;
	int level;
	Random raa;
	
	//current hp, max hp, current mana, max mana, current stamina, max stamina
	int[] resources;
	
	//strength, endurance, agility, dexterity, intelligence, willpower, initiative
	int[] stats;
	
	//burn, freeze, slow, disorient, silent
	int[] conditions;
	
	int baseHit;
	int baseDodge;
	
	//default constructor makes a defunct Unit
	public Unit() {
		name = "";
		level = 0;
		resources = new int[] {0, 0, 0, 0, 0, 0};
		stats = new int[] {0, 0, 0, 0, 0, 0, 0};
		conditions = new int[] {0, 0, 0, 0, 0};
	}
	
	//functional constructor
	public Unit(String nam) {
		name = nam;
		level = 0;
		resources = new int[] {100, 100, 50, 50, 20, 20};
		stats = new int[] {10, 10, 10, 10, 10, 10, 10};
		conditions = new int[] {0, 0, 0, 0, 0};
	}

	//return -1 if unit is dead, 0 if unit cannot move this turn, 1 if ok
	public int turn(Unit[] playerParty, Unit[] enemyParty) {
		
		//burn 5% max hp per tick
		if (conditions[0] > 0) {
			resources[0] = resources[0] - (int) (0.05 * resources[1]);
			conditions[0] --;
		}
		
		if (this.isAlive()) {
			if (conditions[2] > 0) {
				conditions[2] --;
			}
			if (conditions[3] > 0) {
				conditions[3] --;
			}
			if (conditions[4] > 0) {
				conditions[4] --;
			}
			//frozen, cannot move
			if (conditions[1] > 0) {
				conditions[1] --;
				System.out.println(name + " is frozen");
				return 0;
			}
		} else {
			return -1;
		}
		return 1;
	}
	
	//update currenthp, currentmana, currentstamina by hp, mana, stam values, t = 0 for flat value, 1 for percent of max value
	//use in battle
	public void updateRes(int hp, int mana, int stam, int t) {
		if (t == 0) {
			this.resources[0] = Math.min(this.resources[0] + hp, this.resources[1]);
			this.resources[2] = Math.min(this.resources[2] + mana, this.resources[3]);
			this.resources[4] = Math.min(this.resources[4] + stam, this.resources[5]);
		} else if (t == 1) {
			this.resources[0] = Math.min(this.resources[0] + this.resources[1] * hp/100, this.resources[1]);
			this.resources[2] = Math.min(this.resources[2] + this.resources[3] * mana/100, this.resources[3]);
			this.resources[4] = Math.min(this.resources[4] + this.resources[5] * stam/100, this.resources[5]);
		}
	}
	
	//apply status condition c for t ticks
	public void applyCond(int c, int t) {
		conditions[c] = Math.max(conditions[c], t);
	}
	
	public int getPriority() {
		//frozen, cannot take action
		if (conditions[1] > 0) {
			return -1;
		}
		//slowed, reduced priority
		if (conditions[2] > 0) {
			return (int) 0.5*(stats[2] + stats[6]);
		}
		//normal priority
		return stats[2] + stats[6];
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public int[] getRes() {
		return this.resources;
	}
	
	public int[] getStats() {
		return this.stats;
	}
	
	public int hitChance() {
		//base chance (based on unit parameters + dexterity + 0.5 agility
		return (int) (baseHit + stats[3] + 0.5 * stats[2]);
	}
	
	public int dodgeChance() {
		//base chance based on unit parameters + agility
		return baseDodge + stats[2];
	}
	
	public int blockChance() {
		//0.25 * (agility + dexterity + willpower)
		return (int) (0.25 * (stats[2] + stats[3] + stats[6]));
	}
	
	//base physical damage = 2*strength
	public int physicalBaseDmg() { 
		return (int) (2 * stats[0]);
	}
	
	//base magical damage = 2 * intelligence
	public int magicalBaseDmg() {
		return (int) (2 * stats[4]);
	}

	//base physical defense = 0.2 * strength + 0.35 * endurance
	public int physicalBaseDef() { 
		return (int) (0.2 * stats[0] + 0.35 * stats[1]);
	}
	
	//base magical defense = 0.15 * intelligence + 0.25 * willpower
	public int magicalBaseDef() {
		return (int) (0.15 * stats[4] + 0.25 * stats[5]);
	}
	
	//calling unit performs a basic attack on target
	//no hit chance modifiers, no damage modifiers
	public void basicAttack(Unit target) {
		System.out.println(name + " attacks " + target);
		int threshold = this.hitChance() - target.dodgeChance();
		
		if (raa.nextInt(100) <= threshold) {
			int damage = Math.max(0, this.physicalBaseDmg() - target.physicalBaseDef());
			System.out.println(name + " hit! " + damage + " damage dealt");
			target.updateRes(-damage, 0, 0, 0);
			if (!target.isAlive()) {
				System.out.println(target + " died!");
			}
		} else {
			System.out.println(name + " missed!");
		}
		
	}
	
	//calling unit heals target ally
	//cannot miss, no healing modifier
	public void heal(Unit target) {
		this.updateRes(0, -15, 0, 0);
		int heal = Math.max(0, (int) (this.magicalBaseDmg()));
		target.updateRes(heal, 0, 0, 0);
		System.out.println(name + " heals " + target + " for " + heal);
	}
	
	//rest and restore resources if applicable
	public void rest() {
		//restore 10% max MP and ST to character
		int m = (int) (0.1 * this.resources[3]);
		int s = (int) (0.1 * this.resources[5]);
		this.updateRes(0, m, s, 0);
		
		System.out.println(name + " rested");
		System.out.println(m + " MP and " + s + " SP restored!");
	}
	
	public boolean isAlive() {
		if (this.resources[0] > 0) {
			return true;
		}
		return false;
	}

	public String toString() {
		return this.name;
	}
	
	//print out status of unit in battle
	public void printStatus() {
		System.out.println("Lv " + level + " " + name);
		System.out.println("HP : " + resources[0] + "/" + resources[1] + " | MP : " +
		resources[2] + "/" + resources[3] + " | SP : " + resources[4] + "/" + resources[5]);
	}
	
	//print out details of unit
	public void printAttributes() {
		System.out.println(name);
		System.out.println("STR " + stats[0] + " | END  " + stats[1] + " | AGI " + stats[2] + " | DEX " + stats[3] +  " | INT " + stats[4] +  " | WIL " + stats[5]);
	}

	//used to determine which order units take action at each turn
	//higher priority (via Unit.getPriority()) goes before lower priority
	public int compareTo(Unit o) {
		return Integer.compare(o.getPriority(), this.getPriority());
	}

}
