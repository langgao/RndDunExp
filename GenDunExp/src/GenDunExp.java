import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.io.*;

import models.*;

public class GenDunExp {
	
	private static PlayerUnit[] playerParty;
	private static EnemyUnit[] enemyParty;
	private static Scanner scan;
	private static Random res;
		
	public static void main (String[] args) {
		
		boolean playing = true;
		while (playing) {
			playing = false;
			scan = new Scanner(System.in);
			res = new Random();
			
			//new player, create a party
			System.out.println("Please input a player name");
			User player = new User(scan.nextLine());
			int charsleft = 4;
			System.out.println("Create your party");
			while (charsleft > 0) {
				System.out.println("Select a class (" + charsleft + " characters left)");
				System.out.println("Warrior (0) | Swordsman(1) | Sorcerer (2) | Priest (3) | Archer (4) | Monk (5)");
				String c = checkinput("012345", "");
				switch (c) {
				case "0" : c = "Warrior"; break;
				case "1" : c = "Swordsman"; break;
				case "2" : c = "Sorcerer"; break;
				case "3" : c = "Priest"; break;
				case "4" : c = "Archer"; break;
				case "5" : c = "Monk"; break;
				}
				System.out.println("Input a name for your character");
				String n = scan.nextLine();
				PlayerUnit u = new PlayerUnit(n, c, 1, player);
				charsleft --;
			}
			
			playerParty = new PlayerUnit[9];
			enemyParty = new EnemyUnit[9];
			
			while (!player.partyDead()) {
				//offer player options on what to do next
				System.out.println("What should the party do now?");
				System.out.println("Continue Exploring (a)");
				System.out.println("Set up camp (s)");
				System.out.println("Exit the Dungeon (d)");
				String next = checkinput("asd", "");
				
				//combat
				if (next.compareTo("a") == 0) {	
					System.out.println("You encountered a party of monsters!");
					playerParty = player.getCharacters();
				
					//generate new enemy party
					int numenemies = player.getNumChars() - 2 + res.nextInt(5);
					int pal = player.avgPwrLvl();
					int epa = 0;
					
					//scale possible num enemies to make early levels easier, later levels harder
					if (pal < 10) {
						numenemies -= 3;
					} else if (pal < 15) {
						numenemies -= 2;
					} else if (pal < 20) {
						numenemies --;
					} else if (pal < 40) {
						numenemies ++;
					} else if (pal < 60) {
						numenemies += 2;
					} else {
						numenemies += 3;
					}
					
					if (numenemies < 1) {
						numenemies = 1;
					} else if (numenemies > 9) {
						numenemies = 9;
					}
					
					for (int i = 0; i < numenemies; i ++) {
						int elvl = Math.min(1, pal - (int) Math.sqrt(pal)/2 + res.nextInt((int) Math.sqrt(pal)));
						EnemyUnit e = new EnemyUnit("", elvl, -1, -1, -1);
						enemyParty[i] = e;
						epa += e.getLevel();
					}
					epa /= numenemies;
					
					ArrayList<Unit> order = new ArrayList<Unit>();

					//run combat until one party is defeated
					while(partyAlive(playerParty) && partyAlive(enemyParty)) {
						//print state of current combat
						System.out.println("\n\n\n");
						printBattleState();
						//start new turn if needed; add all alive characters to order, print out status of all units
						if (order.isEmpty()) {
							for (int i = 0; i < playerParty.length; i ++) {
								if (playerParty[i] != null) {
									order.add(playerParty[i]);
								}
							}
							for (int i = 0; i < enemyParty.length; i ++) {
								if (enemyParty[i] != null) {
									order.add(enemyParty[i]);
								}
							}
							//determine order for this turn
							Collections.sort(order);
						}	
						//have characters take their turns in order
						System.out.println("-----");
						Unit c = order.remove(0);
						c.turn(playerParty, enemyParty);
					}
					//player's party won the fight
					if (partyAlive(playerParty)) {
						//give exp to all party members
						for (int i = 0; i < playerParty.length; i ++) {
							if (playerParty[i] != null) {
								playerParty[i].gainEXP(epa * 50);
							}
						}
						//give player a piece of equipment
						Equipment e = new Equipment(epa, 0, player.getNextEID());
						System.out.println("You obtained a " + e);
						player.putInStorage(e);
						//give misc
						player.incRested();
						player.incRefresh();
						player.augmentMoney((int) (epa * 15 * (70.0 + res.nextInt(60))/100));
					} else {
						//game over
						System.out.println("*** GAME OVER ***");
						System.out.println("Your Party Died");
						System.out.println("Play again? (Y/N)");
						String i = checkinput("yn", "");
						if (i.compareToIgnoreCase("y") == 0) {
							playing = true;
						}
					}	
				//camp			
				} else if (next.compareTo("s") == 0) {
					boolean camping = true;
					while (camping) {
						System.out.println("Manage Party (a)");
						System.out.println("Pack up camp (f)");
						String asd = checkinput("asf", "");
						
						if (asd.compareTo("a") == 0) {
							manageParty(player);
						} else if (asd.compareTo("s") == 0) {
							//manage equipment deleted

						} else if (asd.compareTo("f") == 0) {
							//pack up camp
							camping = false;
						}
					}
				} else if (next.compareTo("d") == 0) {
					boolean town = true;
					PlayerUnit[] pu = player.getCharacters();
					
					/**
					//refresh market if enough 'time' has passed
					if (player.getRefresh()) {
						int pal = player.avgPwrLvl();
						efs = new Equipment[10];
						for (int i = 0; i < efs.length; i ++) {
							int elvl = Math.min(1, pal - (int) Math.sqrt(pal)/2 + res.nextInt((int) Math.sqrt(pal)));
							efs[i] = new Equipment(elvl, 0, player.getNextEID());
						}
						ifs = new int[] {15, 10, 5, 15, 10, 5};
						player.changeRefresh(false);
					}
					*/
					
					while (town) {
						String vis = "asdf";
						//present player options in town
						if (!player.getRested()) {
							System.out.println("Visit the Inn [Rest and Recover HP/MP/ST] (a)");
						} else {
							vis = vis.substring(0, vis.indexOf('a')) + vis.substring(vis.indexOf('a') + 1, vis.length());
						}
						System.out.println("Visit the Marketplace [Buy and Sell Items/Equipment] (s)");
						System.out.println("Visit the Adventurer's Guild [Recruit/Dismiss Party Members] (d)");
						System.out.println("Manage Party Members (f)");
						String act = checkinput(vis, "");
						
						if (act.compareTo("a") == 0) {
							//visit inn
							System.out.println("Welcome to the Inn!");
							System.out.println("Your funds : " + player.getMoney() + "G");
							int baserate = player.avgPwrLvl() * 100;
							System.out.println("Basic Room | " + baserate + "G | [Restore 10% HP/MP/ST] (0)");
							System.out.println("Spacious Room | " + baserate * 2 + "G | [Restore 20% HP/MP/ST] (1)");
							System.out.println("Delux Room | " + baserate * 5 + "G | [Restore 40% HP/MP/ST] (2)");
							System.out.println("VIP Room | " + baserate * 10 + "G | [Restore 80% HP/MP/ST] (3)");
							System.out.println("Exit (b)");
							
							boolean rooming = true;
							while (rooming) {
								String rom = checkinput("0123b", "");
								boolean success = false;
								int respercent = 0;
								switch (rom) {
								case "0": success = player.augmentMoney(-baserate); respercent = 10; break;
								case "1": success = player.augmentMoney(-baserate * 2); respercent = 20; break;
								case "2": success = player.augmentMoney(-baserate * 5); respercent = 40; break;
								case "3": success = player.augmentMoney(-baserate * 10); respercent = 80; break;
								case "b": rooming = false; break;
								}
								if (success) {
									//successful transaction, party rests and restores resources, cannot rest again until more combat
									player.changeRested(true);
									for (int i = 0; i < pu.length; i ++) {
										if (pu[i] != null) {
											pu[i].updateRes(respercent, respercent, respercent, 1);
											rooming = false;
										}
									}
								} else {
									System.out.println("You don't have enough gold to rent that room");
								}
							}

						} else if (act.compareTo("s") == 0) {
							//visit market
							System.out.println("Welcome to the Market!");
							boolean marketing = true;
							while (marketing) {
								System.out.println("Buy Equipment (a)");
								System.out.println("Sell Equipment (s)");
								System.out.println("Buy Items (d)");
								System.out.println("Sell Items (f)");
								System.out.println("Exit (b)");
								String ne = checkinput("asdfb", "");
								
								if (ne.compareTo("b") == 0) {
									marketing = false;
								} else if (ne.compareTo("a") == 0) {
									//buy equipment
									boolean buying = true;
									while (buying) {
										System.out.println("You have " + player.getMoney() + "G");
										System.out.println("Equipment For Sale");
										String okbuy = "";
										//show equipment for sale, also find which inputs are valid or not
										Equipment[] efs = player.getEquipsForSale();
										for (int i = 0; i < efs.length; i ++) {
											if (efs[i] != null) {
												System.out.println(efs[i] + " | " + efs[i].getValue() * 2.5 + "G | (" + i + ")");
												okbuy += i;
											}
										}
										System.out.println("Exit (b)");
										String itb = checkinput(okbuy + "b", "Not Enough Gold to buy that");
										if (itb.compareTo("b") == 0) {
											buying = false;
										} else {
											//put equipment in player's inventory, delete equipment from efs
											player.putInStorage(efs[Integer.parseInt(itb)]);
											efs[Integer.parseInt(itb)] = null;
										}
									}
								} else if (ne.compareTo("s") == 0) {
									//sell equipment
									System.out.println("Sellable Equipment");
									
								}
							}

						} else if (act.compareTo("d") == 0) {
							
						} else if (act.compareTo("f") == 0) {
							manageParty(player);
						}
					}
				}
				
			}
		}
		
	}
	
	public static boolean partyAlive(Unit[] party) {
		for (int i = 0; i < party.length; i ++) {
			if(party[i] != null && party[i].isAlive()) {
				return true;
			}
		}
		return false;
	}
	
	public static void printBattleState() {
		System.out.println("-----Friendly Party-----");
		for (int i = 0; i < playerParty.length; i ++) {
			if (playerParty[i] != null) {
				playerParty[i].printStatus();
			}
		}
		System.out.println("-----Enemy Party-----");
		for (int i = 0; i < enemyParty.length; i ++) {
			if (enemyParty[i] != null) {
				enemyParty[i].printStatus();
			}
		}
	}
	
	//checks user input against valid inputs vis (string of valid 1 char inputs), output error messaage em if em is not ""
	public static String checkinput(String vis, String em) {
		boolean checkinput = true;
		String a = "";
		while (checkinput) {
			try {
				a = scan.nextLine();
				a.toLowerCase();
				if (a.length() == 1 && vis.indexOf(a) > -1) {
					checkinput = false;
				} else {
					if (em.compareTo("") != 0) {
						System.out.println(em);
					}
				}
			} catch (Exception e) {
				
			}

		}
		return a;
	}
	
	public static void manageParty(User player) {
		//manage characters
		boolean mcs = true;
		while (mcs) {
			String vis = "";
			System.out.println("Party Members -");
			PlayerUnit[] cs = player.getCharacters();
			for (int i = 0; i < cs.length; i ++) {
				if (cs[i] != null) {
					System.out.println(cs[i] + " (" + i + ")");
					vis += i;
				}
			}
			System.out.println("exit (b)");
			//print character chosen + equipment
			String cc = checkinput(vis + "b", "");
			if (cc.compareTo("b") != 0) {
				PlayerUnit u = cs[Integer.parseInt(cc)];
				boolean equipping = true;
				while (equipping) {
					u.printAttributes();
					String es = u.showEquips();
					System.out.println("exit (b)");
					//get equipment to change or exit
					cc = checkinput(es + "b", "");
					switch (cc) {
					case "0" : cc = "a"; break;
					case "1" : cc = "b"; break;
					case "2" : cc = "c"; break;
					case "3" : cc = "d"; break;
					case "4" : cc = "e"; break;
					case "5" : cc = "f"; break;
					case "6" : cc = "g"; break;
					case "7" : cc = "h"; break;
					case "b" : equipping = false; break;
					}
					System.out.println(cc.charAt(0));
					if (equipping) {
						boolean[] aok = player.storageInteract(cc);
						System.out.println("unequip (666)");
						System.out.println("cancel (999)");
						boolean cki = true;
						while (cki) {
							String ak = scan.nextLine();
							//try to equip item, if any issues will trigger exception and invalidate action
							try {
								int ind = Integer.parseInt(ak);
								if (ind == 999) {
									cki = false;
								} else if (ind == 666) {
									u.equip(cc.charAt(0), null);
									cki = false;
								}
								else {
									if (aok[ind]) {
										u.equip(cc.charAt(0), player.takeOutItemWIndex(ind));
										cki = false;
										Equipment[] we = u.ges();
										for (int i = 0; i < we.length; i ++) {
											System.out.println(we[i]);
										}
									}
								}
							} catch (Exception e) {
								
							}
						}
					}
				}
			} else {
				mcs = false;
			}
		}
	}
}
