package models;
import java.util.Arrays;
import java.util.Random;

public class User {

	private String username;
	private PlayerUnit[] characters;
	private Random rar;
	
	private Equipment[] equipmentstorage;
	private int storagespace;
	
	private int money;
	private int nexteid;  //id of next piece of equipment to be acquired
	
	private int currfloor;	//what floor of the dungeon the player is on
	private boolean refresh; //ok or not to generate new items in market, characters at guild
	private int refreshcount;
	private boolean rested; //allow sleep at inn if false
	private int restedcount;
	
	private static Equipment[] efs;	//persistant storage of what equipment market has for sale
	private static int[] ifs;	//persistant storage of what items market has for sale 0=smallhp, 1=medhp, 2=largehp, 3=smallmp/stam, 4=medmp/stam, 5=largemp/stam
	private int[] iii;	//items in inventory
	
	public User (String un) {
		username = un;
		
		rar = new Random();
		characters = new PlayerUnit[9];
		equipmentstorage = new Equipment[200];
		storagespace = equipmentstorage.length;
		
		money = 0;
		nexteid = 0;
		currfloor = 0;
		rested = true;
		refresh  = true;
		restedcount = 0;
		refreshcount  = 0;
		iii = new int[6];
	}
	
	public int getMoney() {
		return money;
	}
	
	//change user's money value
	//positive sum for increase, negative sum for decrease
	//check to make sure result is valid (when decreasing money do to spending)
	//returns true if valid action, false otherwise
	public boolean augmentMoney(int sum) {
		if (money + sum < 0) {
			return false;
		}
		money += sum;
		return true;
	}
	
	public int getNextEID() {
		nexteid ++;
		return nexteid;
	}
	
	//returns an int value symbolyzing the power of the player's party, used to determine what enemies the party faces
	public int avgPwrLvl() {
		double numchars = 0;
		double totlvs = 0;
		for (int i = 0; i < characters.length; i ++) {
			if (characters[i] != null) {
				numchars ++;
				totlvs += characters[i].getLevel();
			}
		}
		if (numchars == 0) {
			return 0;
		}
		return (int) (totlvs / numchars);
	}
	
	public int getFloor() {
		return this.currfloor;
	}
	
	public void incRefresh() {
		refreshcount ++;
		if (refreshcount > 4) {
			int pal = avgPwrLvl();
			efs = new Equipment[10];
			for (int i = 0; i < efs.length; i ++) {
				int elvl = Math.min(1, pal - (int) Math.sqrt(pal)/2 + rar.nextInt((int) Math.sqrt(pal)));
				efs[i] = new Equipment(elvl, 0, getNextEID());
			}
			ifs = new int[] {15, 10, 5, 15, 10, 5};
			refreshcount = 0;
		}
	}
	
	public Equipment[] getEquipsForSale() {
		return efs;
	}
	
	public int[] getItemsForSale() {
		return ifs;
	}
	
	public int[] getiii() {
		return iii;
	}
	
	//buying / selling potions, return true if successful, false if not
	//changing player's money value upon successful transaction is done inside this function
	public boolean augmentiii (int type, int quantity) {
		if (quantity > 0) {
			//buying
			int bprice = avgPwrLvl() * 25;
			//change prices based on potion level
			switch (type) {
			case 1: bprice *= 3; break;
			case 2: bprice *= 7; break;
			case 4: bprice *= 3; break;
			case 5: bprice *= 7; break;
			}
			if (augmentMoney(-bprice * quantity)) {
				iii[type] += quantity;
				return false;
			}
		} else {
			//selling
			int sprice = avgPwrLvl() * 10;
			//change prices based on potion level
			switch (type) {
			case 1: sprice *= 3; break;
			case 2: sprice *= 7; break;
			case 4: sprice *= 3; break;
			case 5: sprice *= 7; break;
			}
			if (iii[type] - quantity >= 0) {
				iii[type] -= quantity;
				augmentMoney(sprice * quantity);
				return true;
			}
		}
		return false;
	}
	
	//called by Unit.usePotion, returns true if it is valid, false if not valid
	public boolean usePotion(int type) {
		if (iii[type] > 0) {
			iii[type] --;
			return true;
		}
		return false;
	}
	
	public boolean getRested() {
		return rested;
	}
	
	public void incRested() {
		restedcount ++;
		if (restedcount > 2) {
			rested = false;
			restedcount = 0;
		}
	}
	
	public void changeRested(boolean r) {
		rested = r;
	}
	
	public void augFloor(int a) {
		this.currfloor += a;
	}
	
	public boolean spaceAvailable() {
		return storagespace > 0;
	}
	
	//only run after checking if ok w/spaceAvailable(), otherwise equipment e will be forced into equipmentstorage[lastindex]
	//if there is no space available
	public void putInStorage(Equipment e) {
		int ind = 0;
		while (ind < equipmentstorage.length) {
			if (equipmentstorage[ind] == null) {
				break;
			}
			ind ++;
		}
		equipmentstorage[ind] = e;
		e.setSInd(ind);
		storagespace --;
	}
	
	//search by itemid
	//
	//can call without vetting itemid, but may return null if so
	//takes item with corresponding itemid out and returns it, clearing its slot in equipmentstorage in the process
	//if no match returns null
	public Equipment takeOutItem(int itemid) {
		for (int i = 0; i < equipmentstorage.length; i ++) {
			if (equipmentstorage[i].getID() == itemid) {
				Equipment e = equipmentstorage[i];
				equipmentstorage[i] = null;
				storagespace ++;
				return e;
			}
		}
		return null;
	}
	
	//get directly by storage index of item
	//
	//basic check to avoid indexoutofbounds included
	//returns specified equipment, cleans up slot in equipmentstorage
	public Equipment takeOutItemWIndex(int itemindex) {
		if (itemindex < 0 || itemindex >= equipmentstorage.length) {
			return null;
		}
		Equipment e = equipmentstorage[itemindex];
		equipmentstorage[itemindex] = null;
		storagespace ++;
		return e;
	}
	
	//displays player's stored items, returns array indicating which slots are valid inputs
	public boolean[] storageInteract(String filter) {
		if (filter.compareTo("") == 0) {
			filter = "abcdefgh";
		}
		System.out.println("### stored items ###");
		//display all items matching criteria in inventory
		boolean[] r = new boolean[200];
		for (int i = 0; i < equipmentstorage.length; i ++) {
			if (equipmentstorage[i] != null && filter.indexOf(equipmentstorage[i].getType()) > -1) {
				System.out.println(equipmentstorage[i] + " (" + i + ")");
				r[i] = true;
			}
		}
		return r;
	}
	
	public PlayerUnit[] getCharacters() {
		return this.characters;
	}
	
	public int getNumChars() {
		int c = 0;
		for (int i = 0; i < characters.length; i ++) {
			if (characters[i] != null) {
				c ++;
			}
		}
		return c;
	}
	
	//add a character to the User's character array
	public void addCharacter(PlayerUnit u) {
		for (int i = 0; i < characters.length; i ++) {
			if (characters[i] == null) {
				characters[i] = u;
				break;
			}
		}
	}

	//remove a character from the User's character array
	public void removeCharacter(PlayerUnit u) {
		int identifier = u.getUID();
		for (int i = 0; i < characters.length; i ++) {
			if (characters[i] != null && characters[i].getUID() == identifier) {
				characters[i] = null;
				break;
			}
		}
	}
	
	//test if User's party is all dead
	//whenever a PlayerUnit dies it is erased form characters (characters[PlayerUnit] is set to null)
	public boolean partyDead() {
		for (int i = 0; i < characters.length; i ++) {
			if (characters[i] != null) {
				return false;
			}
		}
		return true;
	}
	
}
