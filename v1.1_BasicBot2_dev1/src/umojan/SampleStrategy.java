package umojan;

import static bwapi.UnitType.*;
import static mybot.MyBotModule.Broodwar;

import java.util.Iterator;
import java.util.Map;

import bwapi.Unit;
import mybot.BuildManager;
import mybot.BuildOrderItem;
import mybot.ConstructionManager;
import mybot.UnitInfo;
import umojan.util.Util;

public class SampleStrategy implements Strategy {

	// Drone, Zergling, Hydra, Mutal, Queen, Defiler, Ultra
	
	int unitCnt;
	int unitMorphingCnt;
	
	int supplyOccupiedCnt;
	int supplyOccupiedMorphingCnt;
	
	int supplyProvidedCnt;
	int supplyProvidedMorphingCnt;
	
	int overlordCnt;
	int overlordMorphingCnt;
	
	int droneCnt;
	int droneMorphingCnt;
	
	int zerglingCnt;
	int zerglingMorphingCnt;
	
	int hatcheryCnt;	// Lair, Hive 를 포함할것인가? 일단 포함하자
	int hatcheryMorphingCnt;
	
	int spawningpoolCnt;
	int spawningpoolMorphingCnt;
	
	int extractorCnt;
	int extractorMorphingCnt;
	
	boolean zerglingMode;
	
	public SampleStrategy() {
		zerglingMode = false;
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
		
		droneCnt = 0;
		droneMorphingCnt = 0;
		
		zerglingCnt = 0;
		zerglingMorphingCnt = 0;
		
		hatcheryCnt = 0;	// Lair, Hive 를 포함할것인가? 일단 포함하자
		hatcheryMorphingCnt = 0;
		
		spawningpoolCnt = 0;
		spawningpoolMorphingCnt = 0;
		
		extractorCnt = 0;
		extractorMorphingCnt = 0;
		
		
	}
	
	@Override
	public void update(UnitInfo ui) {
		
		if (ui.getPlayer() != Common.SELF) return;
		
		//Util.println("ui : " + ui.getType().toString());

		if (ui.getType().isBuilding()) {
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
				supplyOccupiedMorphingCnt += ui.getType().supplyRequired();
				if (ui.getUnit().getBuildType().equals(Zerg_Drone)) droneMorphingCnt++;
				else if (ui.getUnit().getBuildType().equals(Zerg_Zergling)) zerglingMorphingCnt++;
				else if (ui.getUnit().getBuildType().equals(Zerg_Overlord)) {	overlordMorphingCnt++;	supplyProvidedMorphingCnt += ui.getType().supplyProvided();	}
			} else {
				unitCnt++;
				supplyOccupiedCnt += ui.getType().supplyRequired();
				if (ui.getType().equals(Zerg_Drone)) droneCnt++;
				else if (ui.getType().equals(Zerg_Zergling)) zerglingCnt++;
				else if (ui.getType().equals(Zerg_Overlord)) {	overlordCnt++;	supplyProvidedCnt += ui.getType().supplyProvided();	}
			}
		}
	}
	
	
	//DroneController droneCon = new DroneController();
	
	
	//Map<String, Integer> trinCnt = new HashMap<>();
	
//	boolean flag2 = false;
	//boolean flag = false; 
	
	@Override
	public void build() {
		
		
		if (getSpawningPoolCount() > 0) zerglingMode = true;
		
		if (getSupplyOccupiedCount() == 9*2 && getSupplyProvidedCount() == 9*2) {
			if (getExtractorCount() == 0) {
				BuildManager.Instance().buildQueue.queueAsHighestPriority(Zerg_Extractor, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				return;
			}
			
			if (getMorphingCount(Zerg_Extractor) > 0) {
				//ConstructionManager.Instance().cancelConstructionTask(type, desiredPosition);
				Util.println("---->>>>>>>>>>>>>>>");
				Util.println("getSupplyOccupiedCount() : " + getSupplyOccupiedCount());
				Util.println("getSupplyProvidedCount() : " + getSupplyProvidedCount());
				Util.println("extractorCnt : " + extractorCnt);
				Util.println("extractorMorphingCnt : " + extractorMorphingCnt);
				
				Util.println("DroneCnt : " + droneCnt);
				Util.println("droneMorphingCnt : " + droneMorphingCnt);
				
				for (Unit unit : Common.SELF.getUnits()) {
					if (unit.getType() == Zerg_Extractor) {
						unit.cancelMorph();
						Util.println(" --- Zerg Extractor canceld.");
					}
				}
				
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
				
				
			}
			
			return;
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
			
			//Util.println(" >>>>> Spawning Pool inserted into q : " + zerglingMode);
			
			return;
		}
		
		//if (Broodwar.getFrameCount() % 24 == 0) Util.println("-----------------------------------------------------------");
		
		
//		if (getDroneCount() == 5) {
//			createDrone();
//			return;
//		}
		
		if (Broodwar.getFrameCount() % 48 == 0) {
			Util.println("getSupplyOccupiedCount() : " + getSupplyOccupiedCount());
			Util.println("getSupplyProvidedCount() : " + getSupplyProvidedCount());
		}
		
		
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
	
	private int getSpawningPoolCount() {
		return spawningpoolCnt + getMorphingCount(Zerg_Spawning_Pool);
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
