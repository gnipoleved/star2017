package umojan;

import static bwapi.UnitType.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;
import mybot.BuildManager;
import mybot.BuildOrderItem;
import mybot.CommandUtil;
import mybot.Config;
import mybot.ConstructionManager;
import umojan.UnitInfo.UnitJobs;
import umojan.util.Util;

public class SampleStrategy implements Strategy {
	
	private static InformationManager info = InformationManager.Instance();
	private static CommandUtil command = new CommandUtil();
	
	private static final int TX = 128, TY = 128;

	
	static class UnitMode {
		int value;
		UnitMode(int value) {this.value = value;}
	}
	
	static class State {
		int value;
		State(int value) {this.value = value;}
	}
	
	
	static UnitMode initMode = new UnitMode(0);
	static UnitMode zergling = new UnitMode(100);
	static UnitMode zergling_only = new UnitMode(200);
	static UnitMode hydralisk = new UnitMode(300);
	static UnitMode hydralisk_only = new UnitMode(400);
	static UnitMode mutalisk = new UnitMode(500);
	static UnitMode mutalisk_only = new UnitMode(600);
	
	static State initState = new State(0);
	static State defense = new State(100);
	static State scout = new State(200);
	static State scout_drone = new State(201);
	static State scout_overlord = new State(202);
	static State scout_first_zergling = new State(203);
	static State attack = new State(300);
	static State wait_attack = new State(400);
	static State eli = new State(500);
	
	UnitMode unitMode;
	State state;
	
	
	List<Territory> territories;
	Map<Region, Territory> regionTerritoryMap;
	List<Territory> mainTerritories;
	HashSet<Territory>[] playerTerritories;
	
	int tndex;
	boolean[][] tilesFlag;
	TilePosition[][] tilesPool;
	
	int clkidx = -1, unclkidx = -1;
	
	Position targetPosition;
	
	double minDist = -1.1;
	UnitInfo closestEnemyBuildingInThisFrame = null;

	// Drone, Zergling, Hydra, Mutal, Queen, Defiler, Ultra
	
	int unitCnt;
	int unitMorphingCnt;
	
	int supplyOccupiedCnt;
	int supplyOccupiedMorphingCnt;
	
	int supplyProvidedCnt;
	int supplyProvidedMorphingCnt;
	
	int overlordCnt;
	int overlordMorphingCnt;
	
	int larvaCnt;
	
	int droneCnt;
	int droneMorphingCnt;
	//int droneBookedCnt;
	
	int zerglingCnt;
	int zerglingMorphingCnt;
	
	int hatcheryCnt;	// Lair, Hive 를 포함할것인가? 일단 포함하자
	int hatcheryMorphingCnt;
	int hatcheryTryCnt;
	
	int spawningpoolCnt;
	int spawningpoolMorphingCnt;
	int spawningpoolTryCnt;
	
	int extractorCnt;
	int extractorMorphingCnt;
	int extractorTryCnt;
	
//	boolean zerglingMode;
//	boolean zerglingScoutMode;
//	boolean zerglingAttackMode;
//	
//	boolean eliMode;
	
	int unitCntInTargetPostion;

	
	@SuppressWarnings("unchecked")
	public SampleStrategy() {

		territories = new ArrayList<>(BWTA.getRegions().size() + 1);
		regionTerritoryMap = new HashMap<>();
		mainTerritories = new ArrayList<>();
		playerTerritories = new HashSet[2];
		for (int index = 0; index < playerTerritories.length; index++) playerTerritories[index] = new HashSet<>();
		
		clkidx = -1;
		unclkidx = -1;
		
		targetPosition = null;
		
				
		
		hatcheryTryCnt = 1;	// hatchery 는 기본적으로 하나를 가지고 있으니까...
		spawningpoolTryCnt = 0;
		extractorTryCnt = 0;
//		zerglingMode = false;
//		zerglingScoutMode = false;
//		zerglingAttackMode = false;
//		
//		eliMode = false;
		
		unitMode = initMode;
		state = initState;
		
		//tilesFlag = new boolean[129][129];
		tilesPool = new TilePosition[TX+1][TY+1];
		for (int xndex = 1; xndex <= TX; xndex++) {
			for (int yndex = 1; yndex <= TY; yndex++) {
				tilesPool[xndex][yndex] = new TilePosition(xndex, yndex);
			}
		}
	}
	
	
	
	@Override
	public void setupTerritoryInfo() {
		
		//int index = 0;
		boolean baseFound = false;
		int baseRemain = BWTA.getStartLocations().size();

		for (Region region : BWTA.getRegions())
		{
			//territories.add(new Territory());
			Territory terri = new Territory();
			territories.add(terri);
			terri.region = region;

			baseFound = false;
			// for 문이긴 하지만 region 안에서 baselocation 은 거의 한군데 밖에 없어 한번만 돈다고 보면 됨.
			// 단, 투혼 맵에서 센터에 한 region 에 baselocation 이 두개인 곳이 있긴 있다...
			//for (BWTA::BaseLocation* baseInRegion : region->getBaseLocations()) {
			for (BaseLocation baseInRegion : region.getBaseLocations()) {
				//util::out::println(util::str::toString(baseInRegion->getTilePosition()));
				terri.base = baseInRegion;	// 하나의 Region 안에 두개 이상의 BaseLoaction 이 있다고 하더라도 그 중에 하나만... 합시다..

				//PRN(" areas[" + STR(index) + "] : " + STR(areas[index].base->getTilePosition()) + " ---");
				//Util.println(" areas[" + index + "] : " + (terri.base.getTilePosition()) + " ---");

				if (baseRemain > 0) {
					for (BaseLocation startBase : BWTA.getStartLocations()) {
						if (startBase == baseInRegion) {
							// 현재 region 의 baselocation 이 map의 start location 중 하나라도 위치가 같다면
							//PRN(STR(startBase->getTilePosition()) + " is one of start location.");
							Util.println(startBase.getTilePosition() + " is one of start location.");
							baseFound = true;
							baseRemain--;
							terri.main = true;	// 본진인지 여부(main)는 start location 이면 true 이다.
							if (Common.SELF.getStartLocation().equals(startBase.getTilePosition())) {
								Util.println(", which is self start location.");
								terri.ownership = Territory.OWNED_BY_SELF;
								regionTerritoryMap.put(region, terri);
								playerTerritories[0].add(terri);
							}
							mainTerritories.add(terri);
							break;
						}
					}
					if (baseFound) break;
				}
			}
			//index++;
			Util.println("--------------------------");
		}
		
		
		Comparator<? super Territory> territoryComparator = new Comparator<Territory>() {

			@Override
			public int compare(Territory o1, Territory o2) {
				int x1 = o1.base.getTilePosition().getX() - 64;
				int y1 = o1.base.getTilePosition().getY() - 64;
				int x2 = o2.base.getTilePosition().getX() - 64;
				int y2 = o2.base.getTilePosition().getY() - 64;
				
				int q1 = quar(x1, y1);
				int q2 = quar(x2, y2);
				
				return q1 == q2 ? grad(x1, y1, x2, y2) : q1 - q2;
			}
			
			int quar(int x, int y) {
				if (0 <= x && y < 0) return 1;	// top-right
				if (0 < x && 0 <= y) return 2;	// bottom-right
				if (x <= 0 && 0 < y) return 3;	// bottom-left
				if (x < 0 && y <= 0) return 4;	// top-left
				throw new UnsupportedOperationException("quar(" + x + ", " + y + ")");
			}
			
			int grad(int x1, int y1, int x2, int y2) {
				double g1 = grad(x1, y1);
				double g2 = grad(x2, y2);
				return g1 < g2 ? -1
						: g1 > g2 ? 1
						: 0;
			}
			
			double grad(int x, int y) {
				return x == 0 ? Double.MIN_VALUE : y/x;
			}
			
		};
		
		//Collections.sort(territories, territoryComparator);
		Collections.sort(mainTerritories, territoryComparator);
		
		
		
		Util.println("sorted territories >>> ");
		//for (Territory terri : mainTerritories) {
		for (int index = 0; index < mainTerritories.size(); index++) {
			Territory terri = mainTerritories.get(index);
			Util.println(terri.base.getTilePosition().toString());
			if (terri.ownership == Territory.OWNED_BY_SELF) {
				if (index == mainTerritories.size() - 1) clkidx = 0;
				else clkidx = index + 1;
				
				if (index == 0) unclkidx = mainTerritories.size() - 1;
				else unclkidx = index - 1;
				
				break;
			}
		}
		Util.println("................");
		
		
		
	}
	
	@Override
	public void init() {
		unitCnt = 0;
		unitMorphingCnt = 0;
		
		supplyOccupiedCnt = 0;
		supplyOccupiedMorphingCnt = 0;
		
		supplyProvidedCnt = 0;
		supplyProvidedMorphingCnt = 0;
		
		overlordCnt = 0;
		overlordMorphingCnt = 0;
		
		larvaCnt = 0;
		
		droneCnt = 0;
		droneMorphingCnt = 0;
		//droneBookedCnt = 0;
		
		zerglingCnt = 0;
		zerglingMorphingCnt = 0;
		
		hatcheryCnt = 0;	// Lair, Hive 를 포함할것인가? 일단 포함하자
		hatcheryMorphingCnt = 0;
		
		spawningpoolCnt = 0;
		spawningpoolMorphingCnt = 0;
		
		
		extractorCnt = 0;
		extractorMorphingCnt = 0;
		
		
		minDist = -1.1;
		closestEnemyBuildingInThisFrame = null;
		
		unitCntInTargetPostion = 0;
		
	}
	
	
	
//	private void whoseRegion(Unit unit) {
//		int pidx = -1;
//		if (unit.getPlayer().equals(Common.SELF)) pidx = 0;
//		else if (unit.getPlayer().equals(Common.ENEMY)) pidx = 1;
//		whoseRegion(BWTA.getRegion(unit.getPosition()), pidx);
//	}
	
	private void whoseRegion(Region region, int pidx) {
		Territory terri = regionTerritoryMap.get(region);
		if (terri != null) {
			terri.addOwner(pidx);
			playerTerritories[pidx].add(terri);
		}
	}
	
	
	@Override
	public void updateEnemyUnitInfo(UnitInfo eui) {
		try {
			if (eui == null || eui.getUnit().getPlayer() != Common.ENEMY) return;
			if (eui.getType().isBuilding()) {
				//whoseRegion(eui.getUnit());
				whoseRegion(BWTA.getRegion(eui.getUnit().getPosition()), 1);
				if (targetPosition != null) {
					double dist = targetPosition.getDistance(eui.getUnit().getPosition());
					//Util.println("building : " + eui.getUnit().getType().toString() + " - " + dist);
					if (minDist < 0.0 || minDist > dist) {
						minDist = dist;
						closestEnemyBuildingInThisFrame = eui;
					}
				}
			}
		} catch (Exception e) {
			Util.println("targetPosition : " + targetPosition);
			Util.println("eui.getUnit().getPosition() : " + eui.getUnit().getPosition().toTilePosition());
			throw e;
		}
	}
	
	
	@Override
	public void updateSelfUnitInfo(UnitInfo ui) {
		
		//Util.println("ui : " + ui.getType().toString());

		if (ui.getType().isBuilding()) {
			
			//whoseRegion(ui.getUnit());
			whoseRegion(BWTA.getRegion(ui.getUnit().getPosition()), 0);
			
			if (ui.getType().equals(Zerg_Hatchery)) {
				if (ui.getUnit().isMorphing()) {
					hatcheryMorphingCnt++;
					supplyProvidedMorphingCnt += ui.getType().supplyProvided();
				} else {
					hatcheryCnt++;
					supplyProvidedCnt += ui.getType().supplyProvided();
				}
			} else if (ui.getType().equals(Zerg_Spawning_Pool)) {
				if (ui.getUnit().isMorphing()) spawningpoolMorphingCnt++;
				else spawningpoolCnt++;
			} else if (ui.getType().equals(Zerg_Extractor)) {
				if (ui.getUnit().isMorphing()) extractorMorphingCnt++;
				else extractorCnt++;
			}
		} else {
			
			if (ui.getType().equals(Zerg_Egg)) {
				unitMorphingCnt++;
				supplyOccupiedMorphingCnt += ui.getUnit().getBuildType().supplyRequired();
				if (ui.getUnit().getBuildType().equals(Zerg_Drone)) droneMorphingCnt++;
				else if (ui.getUnit().getBuildType().equals(Zerg_Zergling)) {	zerglingMorphingCnt++;	supplyOccupiedMorphingCnt += Zerg_Zergling.supplyRequired();	}	// zergling 은  반 밖에 계산 안되니 반을 한번더 해줌
				else if (ui.getUnit().getBuildType().equals(Zerg_Overlord)) {	overlordMorphingCnt++;	supplyProvidedMorphingCnt += ui.getType().supplyProvided();	}
			} else {
				unitCnt++;
				supplyOccupiedCnt += ui.getType().supplyRequired();
				if (ui.getType().equals(Zerg_Larva)) larvaCnt++;
				else if (ui.getType().equals(Zerg_Drone)) droneCnt++;
				else if (ui.getType().equals(Zerg_Zergling)) {
					zerglingCnt++;
					if (ui.getUnit().isIdle()) {
						ui.setJob(UnitJobs.Idle);
						//Util.println(" @@@ Zergling(" + ui.getUnitID() + ") is idle now. @@@");
						
						//unitCntInTargetPostion
						
						//ui.getUnit().getPosition().getDistance(otherPosition)
						//BWTA.getGroundDistance2(start, end)
						if (targetPosition != null) {
							
//							int dx = ui.getUnit().getPosition().getX() - targetPosition.getX();
//							int dy = ui.getUnit().getPosition().getY() - targetPosition.getY();
//							//Util.println(ui.getUnit().getPosition().getX() + " - " + targetPosition.getX() + " = " + dx);
//							//Util.println(ui.getUnit().getPosition().getY() + " - " + targetPosition.getY() + " = " + dy);
//							
//							if (dx*dx + dy*dy < 64*64) {
//								
//							}
							
							if (Util.getDistanceSquared(ui.getUnit().getPosition(), targetPosition) < 64*64) unitCntInTargetPostion++;
						}
					}
					
					//if (zerglingScoutMode) {
					if (state == scout_first_zergling) {
						if (ui.getJob() != UnitJobs.Scout) {
							
							if (ui.getSsn() % 2 == 0) {
								TilePosition pos = mainTerritories.get(clkidx).base.getTilePosition();
								if (Common.Broodwar.isExplored(pos)) {
									if (clkidx == mainTerritories.size() - 1) clkidx = 0;
									else clkidx++;
								} else {
									command.move(ui.getUnit(), new Position(pos.getX()*Config.TILE_SIZE, pos.getY()*Config.TILE_SIZE));
									ui.setJob(UnitJobs.Scout);
								}
							} else {
								TilePosition pos = mainTerritories.get(unclkidx).base.getTilePosition();
								if (Common.Broodwar.isExplored(pos)) {
									if (unclkidx == 0) clkidx = mainTerritories.size() - 1;
									else unclkidx--;
								} else {
									command.move(ui.getUnit(), new Position(pos.getX()*Config.TILE_SIZE, pos.getY()*Config.TILE_SIZE));
									ui.setJob(UnitJobs.Scout);
								}
							}
						}
						
					} else if (state == eli) {
						//if (ui.getJob() != UnitJobs.Scout) {
						
						if (Common.Broodwar.getFrameCount() % 25 == 0) Util.println(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ tndex first : " + tndex + "/" + (tndex/TX) + "," + (tndex/TY));
							
							//tilesFlag[ui.getLastPosition().toTilePosition().getX()][ui.getLastPosition().toTilePosition().getY()] = true;
							setTileFlagWith(ui.getLastPosition().toTilePosition(), true);
							
							//if (ui.getLastPosition().toTilePosition().getX() == xndex && ui.getLastPosition().toTilePosition().getY() == yndex) {
							if (getTileIndex(ui.getLastPosition().toTilePosition()) == tndex) { // 목표로 했던 tile 위치에 unit이 왔으면 다음 tile을 본다.
								
								Util.println(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ tndex first : " + tndex + "/" + (tndex/TX) + "," + (tndex/TY));
								
								boolean found = false;
								while (true) {
									tndex++;
									int posX = (tndex/TX)*Config.TILE_SIZE;
									int posY = (tndex%TY)*Config.TILE_SIZE;
									if (Common.Broodwar.getRegionAt(posX, posY).isAccessible() == false) continue;
									if (BWTA.getRegion(ui.getLastPosition()).isReachable(BWTA.getRegion(posX, posY)) == false) continue;
									if (getTileFlagOf(tndex/TX, tndex%TY) == false) {
										found = true;
										break;
									}
								}
								
								Util.println(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ tndex after : " + tndex + "/" + (tndex/TX) + "," + (tndex/TY));
								
								if (!found) tndex = 1; // 모든 타일에 다 가봤는데도 못 찾았으면 다시 가봐야 한다.
								ui.setJob(UnitJobs.Idle);

								//command.attackMove(ui.getUnit(), new Position((tndex/TX)*Config.TILE_SIZE, (tndex%TX)*Config.TILE_SIZE));
								
							}
							
							if (ui.getJob() != UnitJobs.Move) {
								TilePosition next = tilesPool[tndex/TX][tndex%TY];
								command.attackMove(ui.getUnit(), next.toPosition());
	
								ui.setJob(UnitJobs.Move);
							}
							
//							BaseLocation enemyFirstExpansionLocation = info.getFirstExpansionLocation(Common.ENEMY); // 먼저 enemy 확장지역을 살펴 본다.
//							if (getTileFlagOf(enemyFirstExpansionLocation.getTilePosition()) == false) {
//								ui.setJob(UnitJobs.Move);
//								command.attackMove(ui.getUnit(), enemyFirstExpansionLocation.getPosition());
//							} else {
//								if (ui.getLastPosition().toTilePosition().getX() == xndex && ui.getLastPosition().toTilePosition().getY() == yndex) {
//									
//								} else {
//									
//								}
//							}
						
							/*
							if (ui.getSsn() % 2 == 0) {
								Position pos = territories.get(clkidx).base.getPosition();
								bwapi.Region bwapiRegion = Common.Broodwar.getRegionAt(pos);
								

								TilePosition pos = territories.get(clkidx).base.getTilePosition();
								if (Common.Broodwar.isExplored(pos)) {
									if (clkidx == territories.size() - 1) clkidx = 0;
									else clkidx++;
								} else {
									command.move(ui.getUnit(), new Position(pos.getX()*Config.TILE_SIZE, pos.getY()*Config.TILE_SIZE));
									ui.setJob(UnitJobs.Scout);
								}
							} else {
								TilePosition pos = territories.get(unclkidx).base.getTilePosition();
								if (Common.Broodwar.isExplored(pos)) {
									if (unclkidx == 0) clkidx = territories.size() - 1;
									else unclkidx--;
								} else {
									command.move(ui.getUnit(), new Position(pos.getX()*Config.TILE_SIZE, pos.getY()*Config.TILE_SIZE));
									ui.setJob(UnitJobs.Scout);
								}
							}
							*/
							
							
							
							
//						}
					//} else if (zerglingAttackMode) {
					} else if (state == defense) {
						
					} else if (state == wait_attack) {
						
					} else if (state == attack) {
						//info.existsPlayerBuildingInRegion(region, player)
						// idle 이 되는지 확인 및 position 위에 건물이 있는지 확인 하는 방법
//						if (ui.getJob() == UnitJobs.Idle) {
//							if (closestEnemyBuildingInThisFrame != null) {
//								targetPosition = closestEnemyBuildingInThisFrame.getLastPosition();
//								Util.println(" $$$ new target position : " + targetPosition.toTilePosition() + " by idle zergling : " + ui.getUnitID());
//							} else {
//								if (targetPosition != null) {
//									// targetPosition 이 한번 정해졌었는데 closestEnemyBuildingInThisFrame 이 null 되었다는 것은
//									// 내가 알고 있는 상대의 건물이 없다는 뜻.. 어디 안 보이는데 있다..
//									
//								}
//							}
//						}
						command.attackMove(ui.getUnit(), targetPosition);
						ui.setJob(UnitJobs.Combat);
					}
				}
				else if (ui.getType().equals(Zerg_Overlord)) {	overlordCnt++;	supplyProvidedCnt += ui.getType().supplyProvided();	}
			}
		}
			
	}
	
	
	
	private boolean getTileFlagOf(int x, int y) {
		return tilesFlag[x][y];
	}



	private boolean getTileFlagOf(TilePosition tilePosition) {
		//return tilesFlag[tilePosition.getX()][tilePosition.getY()];
		return getTileFlagOf(tilePosition.getX(), tilePosition.getY());
	}



	private void setTileFlagWith(TilePosition tilePosition, boolean value) {
		tilesFlag[tilePosition.getX()][tilePosition.getY()] = value;
	}



	@Override
	public void build() {

		if (spawningpoolCnt > 0) unitMode = zergling_only;	// because this strategy is 초반 저글링 러쉬
		
		if (state != attack && zerglingCnt >= 4) {
			
			if (state == eli) {
				if (closestEnemyBuildingInThisFrame != null) {	// 뭔가 빌딩이 보였다는 말은 빌딩을 찾았다는 말...
					state = attack;
					Util.println("Now attack from eli state...");
				}
			} else {
				if (info.getMainBaseLocation(Common.ENEMY) == null) {
					state = scout_first_zergling;
					if (Common.Broodwar.getFrameCount() % 100 == 0) Util.println("Now search in clock and unclockwise directrion by zerglings...");
				} else {
					targetPosition = info.getMainBaseLocation(Common.ENEMY).getPosition();
					state = attack;
					Util.println(" Now Attack to $$$ target position : " + targetPosition.toTilePosition());
				}
			}
		}
		
		if (state == attack) {
			
			//if (info.getUnitAndUnitInfoMap(player))
			if (unitCntInTargetPostion >= 6) {
				if (closestEnemyBuildingInThisFrame == null) {
					state = eli;
					Util.println("Now elimainate !!!");
					tilesFlag = new boolean[TX+1][TY+1];
					
					
					BaseLocation enemyFirstExpansionLocation = info.getFirstExpansionLocation(Common.ENEMY); // 먼저 enemy 확장지역을 살펴 본다.
					if (getTileFlagOf(enemyFirstExpansionLocation.getTilePosition()) == false) {
//						ui.setJob(UnitJobs.Move);
//						command.attackMove(ui.getUnit(), enemyFirstExpansionLocation.getPosition());
						//(enemyFirstExpansionLocation.getTilePosition().getX()-1)*128 + (enemyFirstExpansionLocation.getTilePosition().getY())
						tndex = getTileIndex(enemyFirstExpansionLocation.getTilePosition()); 
					} else {
						tndex = 1;
					}
					
//					for (int index = 0; index < territories.size(); index++) {
//						Territory terri = territories.get(index);
//						Util.println(terri.base.getTilePosition().toString());
//						if (terri.ownership == Territory.OWNED_BY_SELF) {
//							if (index == territories.size() - 1) clkidx = 0;
//							else clkidx = index + 1;
//							
//							if (index == 0) unclkidx = territories.size() - 1;
//							else unclkidx = index - 1;
//							
//							break;
//						}
//					}
					
					
					
				} else {
					targetPosition = closestEnemyBuildingInThisFrame.getLastPosition();
				}
			}
			
		}
		
		if (getSupplyOccupiedCount() >= 10*2) {
			
			if (getSupplyProvidedCount() - hatcheryCnt*3 <= getSupplyOccupiedCount() && getSupplyOccupiedCount() <= getSupplyProvidedCount() + hatcheryCnt*3) {
				//getHatchery().train(Zerg_Overlord);
				trainUnit(Zerg_Overlord, 1);
				return;
			}
		}
		

		
		// lair , hive 도 더해줘야 할수도 있음... 후반에는..
		if (hatcheryTryCnt == hatcheryCnt + hatcheryMorphingCnt && larvaCnt*50 + 300 + 50 < Common.SELF.minerals()) {
			BuildManager.Instance().buildQueue.queueAsHighestPriority(Zerg_Hatchery, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			hatcheryTryCnt++;
			return;
		}
		
		if (hatcheryCnt >= 2) {
			//if (getSupplyOccupiedCount() >= 20*2 && getSupplyOccupiedCount() <= 30*2) {
			if (getSupplyOccupiedCount() >= 20*2) {
				if (getDroneCount() < 9) {
					createDrone();
					return;
				}
				
			} 
		}
		
		
		if (getSupplyOccupiedCount() == 9*2 && getSupplyProvidedCount() == 9*2) {
			if (getExtractorCount() == 0 && larvaCnt > 0 && Common.SELF.minerals() >= 75) {
				BuildManager.Instance().buildQueue.queueAsHighestPriority(Zerg_Extractor, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				extractorTryCnt++;
				return;
			}
			
			//if (getMorphingCount(Zerg_Extractor) > 0) {
			if (extractorMorphingCnt > 0) {
				Iterator<UnitInfo> iter = info.getUnitsOf(Zerg_Extractor).values().iterator();
				while (iter.hasNext()) {
					UnitInfo ui = iter.next();
					if (ui.getUnit().isMorphing()) {
						ui.getUnit().cancelMorph();
						Util.println(" --- Zerg Extractor canceld.");
						break;
					}
					
				}
				return;	
			}
			
			
		}
		
		//if (getDroneCount() < 6 && zerglingMode == false) {
		if (!(unitMode == zergling_only || unitMode == zergling) && getDroneCount() < 6) {
			//Util.println("zergling ??? : " + zerglingMode);
			createDrone();
			return;
		}
		
		
		if (getSpawningPoolCount() < 1) {		
			
			//if (SELF.minerals() > 200)
			BuildManager.Instance().buildQueue.queueAsHighestPriority(Zerg_Spawning_Pool, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			spawningpoolTryCnt++;
			
			//Util.println(" >>>>> Spawning Pool inserted into q : " + zerglingMode);
			
			return;
		}
		
		
		if (Common.Broodwar.getFrameCount() % 50*5 == 0) {
			Util.println("\r\nLarvaCnt : " + larvaCnt);
			Util.println("getSupplyOccupiedCount() : " + getSupplyOccupiedCount());
			Util.println("getSupplyProvidedCount() : " + getSupplyProvidedCount());
			Util.println("targetPosition : " + ((targetPosition == null) ? "null" : targetPosition.toTilePosition()));
			Util.println("closestEnemyBuildingInThisFrame : " + ((closestEnemyBuildingInThisFrame == null) ? "null" : closestEnemyBuildingInThisFrame.getLastPosition().toTilePosition()));
			Util.println("state : " + state.value);
		}
		
		
		if (getSupplyOccupiedCount() < getSupplyProvidedCount()) {
			if ((unitMode == zergling_only || unitMode == zergling)) createZergling();
			return;
		}
		
		
		
		
		
		
		
		
	}
	
	private int getTileIndex(TilePosition tilePosition) {
		return (tilePosition.getX()-1)*128 + (tilePosition.getY());
	}



	private boolean isBaseState(State state, State base) {
		return state.value / 100 == base.value;
	}



	private int getExtractorCount() {
		return extractorCnt + getMorphingCount(Zerg_Extractor);
	}

	private int getSupplyProvidedCount() {
		return supplyProvidedCnt + supplyProvidedMorphingCnt;
	}

	private int getSupplyOccupiedCount() {
		return supplyOccupiedCnt + supplyOccupiedMorphingCnt;
	}

	private int getDroneCount() {
		return droneCnt + droneMorphingCnt;
	}
	
	// 만드려고 하는 개수 및 만들어 지고 있는 개수 포함
	private int getSpawningPoolCount() {
		//return spawningpoolCnt + getMorphingCount(Zerg_Spawning_Pool);
		
//		if (spawningpoolBookedCnt > spawningpoolCnt + spawningpoolMorphingCnt) {	// 만드려고 풀에 넣었단 얘기
//			return  + spawningpoolBookedCnt;
//		}
		
		return spawningpoolTryCnt;
	}
	
	private int getMorphingCount(bwapi.UnitType type) {
		return BuildManager.Instance().buildQueue.getItemCount(type) + ConstructionManager.Instance().getConstructionQueueItemCount(type, null);
	}
	
	
	private void createDrone() {
		//getHatchery().train(Zerg_Drone);
		trainUnit(Zerg_Drone, 1);
	}
	
	private void trainUnit(UnitType unitType, int howMany) {
		int numOfUnit = 0;
		int numOfHatchery = 0;
		for (Unit hatcheryUnitFiltered : Common.SELF.getUnits()) {
			if (hatcheryUnitFiltered.getType().equals(Zerg_Hatchery) || hatcheryUnitFiltered.getType().equals(Zerg_Lair) || hatcheryUnitFiltered.getType().equals(Zerg_Hive)) {
				if (hatcheryUnitFiltered.isCompleted()) {
					numOfHatchery++;
	//				int cntLarva = unit.getLarva().size();
	//				for (int index = 0; index < cntLarvaunit.train
					for (Unit larva : hatcheryUnitFiltered.getLarva()) {
						if (hatcheryUnitFiltered.canTrain(unitType)) {
							numOfUnit++;
							hatcheryUnitFiltered.train(unitType);
							if (numOfUnit >= howMany) break;
						}
					}
				}
			}
			if (numOfUnit >= howMany) break;
			if (numOfHatchery >= hatcheryCnt) break;
		}
	}
	
	private void createZergling() {
		//getHatchery().train(Zerg_Zergling);
//		Iterator<UnitInfo> iter = InformationManager.Instance().getUnitsOf(Zerg_Hatchery).values().iterator();
//		while (iter.hasNext()) {
//			UnitInfo hatcheryInfo = iter.next();
//			int hLarvaCnt = hatcheryInfo.getUnit().getLarva().size();
//			if (hLarvaCnt > 0) {
//				hatcheryInfo.getUnit().train(Zerg_Zergling);
//				//break;
//			}
//		}
		trainUnit(Zerg_Zergling, 1000);	// 충분히 큰 수를 입력하면 만들 수 있는 만큼 만들겠지..
	}

//	private Unit getHatchery() {
//		Map<Integer, UnitInfo> innerMap = InformationManager.Instance().getUnitsOf(Zerg_Hatchery);
//		Iterator<UnitInfo> iter = innerMap.values().iterator();
//		return iter.next().getUnit();
//		
//	}

}
