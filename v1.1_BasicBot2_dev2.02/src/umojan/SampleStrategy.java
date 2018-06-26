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
	
	List<Territory> territories;
	Map<Region, Territory> regionTerritoryMap;
	List<Territory> mainTerritories;
	HashSet<Territory>[] playerTerritories;
	
	int clkidx = -1, unclkidx = -1;
	
	Position targetPosition;

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
	
	boolean zerglingMode;
	boolean zerglingScoutMode;
	boolean zerglingAttackMode;
	
	@SuppressWarnings("unchecked")
	public SampleStrategy() {

		territories = new ArrayList<>(BWTA.getRegions().size() + 1);
		regionTerritoryMap = new HashMap<>();
		mainTerritories = new ArrayList<>();
		playerTerritories = new HashSet[2];
		for (int index = 0; index < playerTerritories.length; index++) playerTerritories[index] = new HashSet<>();
		
		targetPosition = null;
		
		hatcheryTryCnt = 1;	// hatchery 는 기본적으로 하나를 가지고 있으니까...
		spawningpoolTryCnt = 0;
		extractorTryCnt = 0;
		zerglingMode = false;
		zerglingScoutMode = false;
		zerglingAttackMode = false;
		
		
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
		
		
		Collections.sort(mainTerritories, new Comparator<Territory>() {

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
			
		});
		
		
		
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
		
		
	}
	
	
	
	private void whoseRegion(Unit unit) {
		int pidx = -1;
		if (unit.getPlayer().equals(Common.SELF)) pidx = 0;
		else if (unit.getPlayer().equals(Common.ENEMY)) pidx = 1;
		whoseRegion(BWTA.getRegion(unit.getPosition()), pidx);
	}
	
	private void whoseRegion(Region region, int pidx) {
		Territory terri = regionTerritoryMap.get(region);
		if (terri != null) {
			terri.addOwner(pidx);
			playerTerritories[pidx].add(terri);
		}
	}
	
	
	
	@Override
	public void update(UnitInfo ui) {
		
		if (ui.getPlayer().equals(Common.ENEMY)){
			
			if (ui.getType().isBuilding()) {
				
				whoseRegion(ui.getUnit());
			}
			
		} else if (ui.getPlayer().equals(Common.SELF)) {
		
			//Util.println("ui : " + ui.getType().toString());
	
			if (ui.getType().isBuilding()) {
				
				//whoseRegion(BWTA.getRegion(ui.getUnit().getPosition()));
				whoseRegion(ui.getUnit());
				
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
						}
						
						if (zerglingScoutMode) {
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
						} else if (zerglingAttackMode) {
							//info.existsPlayerBuildingInRegion(region, player)
							// idle 이 되는지 확인 및 position 위에 건물이 있는지 확인 하는 방법
							if (ui.getJob() == UnitJobs.Idle) {
								UnitInfo closest = null;
								double dist = -1.0;
								Iterator<UnitInfo> iter = info.getUnitAndUnitInfoMap(Common.ENEMY).values().iterator();
								while (iter.hasNext()) {
									UnitInfo enemyui = iter.next();
									if(enemyui.getType().isBuilding()) {
										if (dist == -1 || dist > targetPosition.getDistance(enemyui.getLastPosition())) {
											dist = targetPosition.getDistance(enemyui.getLastPosition());
											closest = enemyui;
										}
									}
								}
								targetPosition = closest.getLastPosition();
							}
							command.attackMove(ui.getUnit(), targetPosition);
							ui.setJob(UnitJobs.Combat);
						}
					}
					else if (ui.getType().equals(Zerg_Overlord)) {	overlordCnt++;	supplyProvidedCnt += ui.getType().supplyProvided();	}
				}
			}
			
		} // end of if self
	}
	
	
	//DroneController droneCon = new DroneController();
	
	
	//Map<String, Integer> trinCnt = new HashMap<>();
	
//	boolean flag2 = false;
	//boolean flag = false; 
	
	@Override
	public void build() {
		
		
		
		
		//if (getSpawningPoolCount() > 0 && getDroneCount() == 6) zerglingMode = true;
		
		//if (spawningpoolTryCnt - spawningpoolCnt - spawningpoolMorphingCnt == 0 && spawningpoolTryCnt > 0) zerglingMode = true;
		if (spawningpoolCnt > 0) zerglingMode = true;
		
//		if (zerglingMode && zerglingCnt >= 4) zerglingAttackMode = true;
//		
//		if (zerglingAttackMode) {
//			if (info.getMainBaseLocation(Common.ENEMY) != null) {
//				
//			}
//		}
		
		if (zerglingMode && zerglingCnt >= 4) {
			//if (playerTerritories[1].size() == 0) {
			if (info.getMainBaseLocation(Common.ENEMY) == null) {
				zerglingScoutMode = true;
				 
			} else {
				zerglingScoutMode = false;
				zerglingAttackMode = true;
				
				targetPosition = info.getMainBaseLocation(Common.ENEMY).getPosition();
			}
		}
		
		
		if (zerglingMode && zerglingCnt >= 4) zerglingAttackMode = true;
		
		if (getSupplyOccupiedCount() >= 10*2) {
//			if (hatcheryCnt == 1) {
//				if (getSupplyProvidedCount() - 3 <= getSupplyOccupiedCount() && getSupplyOccupiedCount() <= getSupplyProvidedCount() + 3) {
//					getHatchery().train(Zerg_Overlord);
//				}
//			} else if (1 < hatcheryCnt && 2 <= hatcheryCnt) {
//				if (getSupplyProvidedCount() - 3 <= getSupplyOccupiedCount() && getSupplyOccupiedCount() <= getSupplyProvidedCount() + 3) {
//			}
			
			if (getSupplyProvidedCount() - hatcheryCnt*3 <= getSupplyOccupiedCount() && getSupplyOccupiedCount() <= getSupplyProvidedCount() + hatcheryCnt*3) {
				getHatchery().train(Zerg_Overlord);
				return;
			}
		}
		
		
		// lair , hive 도 더해줘야 할수도 있음... 후반에는..
		if (hatcheryTryCnt == hatcheryCnt + hatcheryMorphingCnt && larvaCnt*50 + 300 + 50 < Common.SELF.minerals()) {
			BuildManager.Instance().buildQueue.queueAsHighestPriority(Zerg_Hatchery, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			hatcheryTryCnt++;
			return;
		}
		
		if (hatcheryCnt == 2) {
			if (getSupplyOccupiedCount() >= 20*2 && getSupplyOccupiedCount() <= 30*2) {
				if (getDroneCount() < 9) createDrone();
				return;
			}
		}
		
		
		if (getSupplyOccupiedCount() == 9*2 && getSupplyProvidedCount() == 9*2) {
			if (getExtractorCount() == 0 && getHatchery().getLarva().size() > 0 && Common.SELF.minerals() >= 75) {
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
				
				//ConstructionManager.Instance().cancelConstructionTask(type, desiredPosition);
//				Util.println("---->>>>>>>>>>>>>>>");
//				Util.println("getSupplyOccupiedCount() : " + getSupplyOccupiedCount());
//				Util.println("getSupplyProvidedCount() : " + getSupplyProvidedCount());
//				Util.println("extractorCnt : " + extractorCnt);
//				Util.println("extractorMorphingCnt : " + extractorMorphingCnt);
//				
//				Util.println("DroneCnt : " + droneCnt);
//				Util.println("droneMorphingCnt : " + droneMorphingCnt);
//				
//				for (Unit unit : Common.SELF.getUnits()) {
//					if (unit.getType() == Zerg_Extractor) {
//						unit.cancelMorph();
//						Util.println(" --- Zerg Extractor canceld.");
//					}
//				}
				
				
				
				/*
				 * 아래 해 본 결과 cancel 명령 날리면 그 frame 에서 바로 drone 수가 원래대로 늘어남
				 * [2017-08-07 21:47:09] getSupplyOccupiedCount() : 18
[2017-08-07 21:47:09] getSupplyProvidedCount() : 18
[2017-08-07 21:47:09] ---->>>>>>>>>>>>>>>
[2017-08-07 21:47:09] getSupplyOccupiedCount() : 18
[2017-08-07 21:47:09] getSupplyProvidedCount() : 18
[2017-08-07 21:47:09] extractorCnt : 0
[2017-08-07 21:47:09] extractorMorphingCnt : 1
[2017-08-07 21:47:09] DroneCnt : 5
[2017-08-07 21:47:09] droneMorphingCnt : 0
[2017-08-07 21:47:09]  --- Zerg Extractor canceld.
[2017-08-07 21:47:09] newExtCnt : 0
[2017-08-07 21:47:09] newExtMPcnt : 0
[2017-08-07 21:47:09] newDroneCnt : 6
[2017-08-07 21:47:09] newDroneMPcnt : 0
[2017-08-07 21:47:09] 
Construction Failed case . remove ConstructionTask Zerg_Extractor
[2017-08-07 21:47:10] getSupplyOccupiedCount() : 20
[2017-08-07 21:47:10] getSupplyProvidedCount() : 18

				int newDroneCnt = 0, newDroneMPcnt = 0;
				int newExtCnt = 0, newExtMPcnt = 0;
				
				for (Unit unit : Common.SELF.getUnits()) {
					if (unit.getType().isBuilding()) {
						if (unit.getType().equals(Zerg_Extractor)) {
							if (unit.isMorphing()) newExtMPcnt++;
							else newExtCnt++;
						}
					} else {
						if (unit.getType().equals(Zerg_Egg)) {
							if (unit.getBuildType().equals(Zerg_Drone)) newDroneMPcnt++;
						} else {
							if (unit.getType().equals(Zerg_Drone)) newDroneCnt++;
						}
					}
				}

				Util.println("newExtCnt : " + newExtCnt);
				Util.println("newExtMPcnt : " + newExtMPcnt);
				
				Util.println("newDroneCnt : " + newDroneCnt);
				Util.println("newDroneMPcnt : " + newDroneMPcnt);
				
				Util.println("");
				
				*/
				
				return;	
			}
			
			
		}
		
		
		if (getDroneCount() < 6 && zerglingMode == false) {
			
			//if (getDroneCount() == 5) zerglingMode = true;
			
			//Util.println("zergling ??? : " + zerglingMode);
			createDrone();
			
			
			
			return;
		}
		
		
//		int spwanPoolBuildQcnt = BuildManager.Instance().buildQueue.getItemCount(Zerg_Spawning_Pool);
//		int spwanConstQcnt = ConstructionManager.Instance().getConstructionQueueItemCount(Zerg_Spawning_Pool, null);
//		
//		if (Broodwar.getFrameCount() % 24 == 0) {
//			Util.println("drone cnt : " + droneCnt);
//			Util.println("drone moprhing cnt : " + droneMorphingCnt);
//			
//			Util.println("spawningpoolCnt : " + spawningpoolCnt);
//			//Util.println("spawningpoolMorphingCnt : " + spawningpoolMorphingCnt);
//			Util.println("spwanPoolBuildQcnt : " + spwanPoolBuildQcnt);
//			Util.println("spwanConstQcnt : " + spwanConstQcnt);
//		}

		//int curSpawnPoolCnt = spawningpoolCnt + spawningpoolMorphingCnt + spwanPoolBuildQcnt + spwanConstQcnt;
		//int curSpawnPoolCnt = spawningpoolCnt + spwanPoolBuildQcnt + spwanConstQcnt;
		
		if (getSpawningPoolCount() < 1) {		
			
			//if (SELF.minerals() > 200)
			BuildManager.Instance().buildQueue.queueAsHighestPriority(Zerg_Spawning_Pool, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			spawningpoolTryCnt++;
			
			//Util.println(" >>>>> Spawning Pool inserted into q : " + zerglingMode);
			
			return;
		}
		
		//if (Broodwar.getFrameCount() % 24 == 0) Util.println("-----------------------------------------------------------");
		
		
//		if (getDroneCount() == 5) {
//			createDrone();
//			return;
//		}
		
//		if (Common.Broodwar.getFrameCount() % 75 == 0) {
//			Util.println("\r\nLarvaCnt : " + larvaCnt);
//			Util.println("getSupplyOccupiedCount() : " + getSupplyOccupiedCount());
//			Util.println("getSupplyProvidedCount() : " + getSupplyProvidedCount());
//		}
		
		
		if (getSupplyOccupiedCount() < getSupplyProvidedCount()) {
			createZergling();
			return;
		}
		
		
		
		
		
		
		
		
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
		getHatchery().train(Zerg_Drone);
	}
	
	private void createZergling() {
		getHatchery().train(Zerg_Zergling);
	}

	private Unit getHatchery() {
		Map<Integer, UnitInfo> innerMap = InformationManager.Instance().getUnitsOf(Zerg_Hatchery);
		Iterator<UnitInfo> iter = innerMap.values().iterator();
		return iter.next().getUnit();
		
	}

}
