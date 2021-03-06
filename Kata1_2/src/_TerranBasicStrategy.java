import static bwapi.UnitType.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

public class _TerranBasicStrategy implements _Strategy {
	
//	private static _TerranBasicStrategy instance = new _TerranBasicStrategy();
//	public static _TerranBasicStrategy getInstance() {
//		return instance;
//	}
	
	
	private static WorkerManager workers = WorkerManager.Instance();
	private static ScoutManager scouter = ScoutManager.Instance();
	private static _InformationManager info = _InformationManager.Instance();
	private static CommandUtil command = new CommandUtil();
	private static BuildOrderQueue bq = BuildManager.Instance().buildQueue; 

	private static final int TX = 128, TY = 128;
	
//	static UnitMode initMode = new UnitMode(0);
//	static UnitMode bionic = new UnitMode(100);
//	static UnitMode bichanic = new UnitMode(200);
//	static UnitMode mechanic = new UnitMode(300);
//	static UnitMode hydralisk_only = new UnitMode(400);
//	static UnitMode mutalisk = new UnitMode(500);
//	static UnitMode mutalisk_only = new UnitMode(600);
	
	static State initState = new State(0);
	static State defense = new State(100);
	static State defense_base = new State(101);
	static State scout = new State(200);
	static State scout_scv = new State(201);
	static State scout_unit = new State(202);
	static State scout_building = new State(203);
	static State attack = new State(300);
	static State wait_attack = new State(400);
	static State eli = new State(500);
	
	
	_MapStrategy mapStrategy;
	
	
//	UnitMode unitMode;
	private State state;
	
	
	List<_Territory> territories;
	Map<Region, _Territory> regionTerritoryMap;
	List<_Territory> mainTerritories;
	HashSet<_Territory>[] playerTerritories;
	
	int tndex;
	boolean[][] tilesFlag;
	TilePosition[][] tilesPool;
	
	int clkidx = -1, unclkidx = -1;
	
	Position targetPosition;
	
	int[] idleCnt;
	
	double minDist = -1.1;
	_UnitInfo closestEnemyBuildingInThisFrame = null;
	
	
	int unitCnt = 0;
	int unitConstCnt = 0;
	
	int supplyOccupiedCnt = 0;
	int supplyOccupiedConstCnt = 0;
	
	int supplyProvidedCnt = 0;
	int supplyProvidedConstCnt = 0;
	
	int scvCnt;
	int scvConstCnt;
	//int scvNoPathCnt;
	int scvTryCnt = 0;
	
	int cmdCenterCnt = 0;
	int cmdCenterConstCnt = 0;
	int cmdCenterTryCnt = 0;
	
	int barracksCnt = 0;
	int barracksConstCnt = 0;
	int barracksTryCnt = 0;
	
	int supplyDepotCnt = 0;
	int supplyDepotConstCnt = 0;
	int supplyDepotTryCnt = 0;
	
	int factoryCnt = 0;
	int factoryConstCnt = 0;
	int factoryTryCnt = 0;
	
	int machineShopCnt = 0;
	int machineShopConstCnt = 0;
	int machineShopTryCnt = 0;
	
	int academyCnt = 0;
	int academyConstCnt = 0;
	int academyTryCnt = 0;
	
	int armoryCnt = 0;
	int armoryConstCnt = 0;
	int armoryTryCnt = 0;
	
	int engbayCnt = 0;
	int engbayConstCnt = 0;
	int engbayTryCnt = 0;
	
	int starportCnt = 0;
	int starportConstCnt = 0;
	int starportTryCnt = 0;
	
	int controlTowerCnt = 0;
	int controlTowerConstCnt = 0;
	int controlTowerTryCnt = 0;
	
	int sciFacilityCnt = 0;
	int sciFacilityConstCnt = 0;
	int sciFacilityTryCnt = 0;
	
	int marineCnt = 0;
	int marineConstCnt = 0;
	int marineTryCnt = 0;
	
	int tankCnt = 0;
	int tankConstCnt = 0;
	int tankTryCnt = 0;
	
	int vultureCnt = 0;
	int vultureConstCnt = 0;
	int vultureTryCnt = 0;
	
	int goliathCnt = 0;
	int goliathConstCnt = 0;
	int goliathTryCnt = 0;
	
	int dropshipCnt = 0;
	int dropshipConstCnt = 0;
	int dropshipTryCnt = 0;
	
	int wraithCnt = 0;
	int wraithConstCnt = 0;
	int wraithTryCnt = 0;
	
	int valkyrieCnt = 0;
	int valkyrieConstCnt = 0;
	int valkyrieTryCnt = 0;
	
	int vesselCnt = 0;
	int vesselConstCnt = 0;
	int vesselTryCnt = 0;
	
	int underAttackBuildingCnt = 0;
	int underAttackUnitCnt = 0;
	
	int underAttackScvCnt = 0;
	
	Map<String, Int> completedCnt;
	Map<String, Int> constCnt;
	
	
	@SuppressWarnings("unchecked")
	public _TerranBasicStrategy() {
		territories = new ArrayList<>(BWTA.getRegions().size() + 1);
		regionTerritoryMap = new HashMap<>();
		mainTerritories = new ArrayList<>();
		playerTerritories = new HashSet[2];
		for (int index = 0; index < playerTerritories.length; index++) playerTerritories[index] = new HashSet<>();
		
		clkidx = -1;
		unclkidx = -1;
		
		targetPosition = null;
		
		state = initState;
		
		tilesPool = new TilePosition[TX+1][TY+1];
		for (int xndex = 1; xndex <= TX; xndex++) {
			for (int yndex = 1; yndex <= TY; yndex++) {
				tilesPool[xndex][yndex] = new TilePosition(xndex, yndex);
			}
		}
		
		idleCnt = new int[3000];
		
		constCnt = new HashMap<>();
		completedCnt = new HashMap<>();
		
		//Object o = _Common.Broodwar.all
//		UnitType.AllUnits
		
		
	}
	
	
	@Override
	public void setupTerritoryInfo() {
		
		//int index = 0;
		boolean baseFound = false;
		int baseRemain = BWTA.getStartLocations().size();

		for (Region region : BWTA.getRegions())
		{
			//territories.add(new Territory());
			_Territory terri = new _Territory();
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
							__Util.println(startBase.getTilePosition() + " is one of start location.");
							baseFound = true;
							baseRemain--;
							terri.main = true;	// 본진인지 여부(main)는 start location 이면 true 이다.
							if (_Common.SELF.getStartLocation().equals(startBase.getTilePosition())) {
								__Util.println(", which is self start location.");
								terri.ownership = _Territory.OWNED_BY_SELF;
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
			__Util.println("--------------------------");
		}
		
		
		Comparator<? super _Territory> territoryComparator = new Comparator<_Territory>() {

			@Override
			public int compare(_Territory o1, _Territory o2) {
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
		
		
		
		__Util.println("sorted territories >>> ");
		//for (Territory terri : mainTerritories) {
		for (int index = 0; index < mainTerritories.size(); index++) {
			_Territory terri = mainTerritories.get(index);
			__Util.println(terri.base.getTilePosition().toString());
			if (terri.ownership == _Territory.OWNED_BY_SELF) {
				if (index == mainTerritories.size() - 1) clkidx = 0;
				else clkidx = index + 1;
				
				if (index == 0) unclkidx = mainTerritories.size() - 1;
				else unclkidx = index - 1;
				
				break;
			}
		}
		__Util.println("................");
		
		
		//setInitialBuildOrder();
		
		
	}
	
	private void construct(UnitType building, TilePosition location, BuildOrderItem.SeedPositionStrategy seedPositionStrategy) {
		if (location == null) bq.queueAsLowestPriority(building, seedPositionStrategy);	
		else {
			bq.queueAsFixedPosition(building, location, true);
		}
	}

	private void setInitialBuildOrder() {
		bq.queueAsLowestPriority(Terran_SCV);	// 5th, the first creation
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_SCV);
		//bq.queueAsFixedPosition(Terran_Supply_Depot, mapStrategy.getFirstSupplyDepotTilePosition(), true);
		
		construct(Terran_Supply_Depot, mapStrategy.getFirstSupplyDepotTilePosition(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
		
		bq.queueAsLowestPriority(Terran_SCV);	// 9th
		bq.queueAsLowestPriority(Terran_SCV);	// 10th
//		bq.queueAsLowestPriority(Terran_Barracks, mapStrategy.getFirstBarrackstTilePosition(), true);
		bq.queueAsFixedPosition(Terran_Barracks, mapStrategy.getFirstBarrackstTilePosition(), true);
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_SCV);
		
//		TilePosition secondSuplPosition = mapStrategy.getSecondSupplyDepotTilePosition();
//		if (secondSuplPosition == null) bq.queueAsLowestPriority(Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.MainBaseLocation);	
//		else {
//			//bq.queueAsLowestPriority(Terran_Supply_Depot, secondSuplPosition, true);
//			bq.queueAsFixedPosition(Terran_Supply_Depot, secondSuplPosition, true);
//		}
		
		construct(Terran_Supply_Depot, mapStrategy.getSecondSupplyDepotTilePosition(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
		
		
		bq.queueAsLowestPriority(Terran_Refinery);
		bq.queueAsLowestPriority(Terran_SCV);	//13
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_Factory);
		bq.queueAsLowestPriority(Terran_SCV);	//16
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_SCV);
		
		bq.queueAsLowestPriority(Terran_Machine_Shop);
		
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_SCV);
		
		bq.queueAsLowestPriority(Terran_Supply_Depot);
		
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_Siege_Tank_Tank_Mode);
		
		
		
	}
	
	Unit targetUnitWhenScouting = null;
	int enemyWorkerAroundScouterCnt = 0;
	int enemyUnitAttackingScouterCnt = 0;

	@Override
	public void init() {
		
		unitCnt = 0;
		unitConstCnt = 0;
		
		supplyOccupiedCnt = 0;
		supplyOccupiedConstCnt = 0;
		
		supplyProvidedCnt = 0;
		supplyProvidedConstCnt = 0;
		
		scvCnt = 0;
		scvConstCnt = 0;
		//scvNoPathCnt = 0;
		
		cmdCenterCnt = 0;
		cmdCenterConstCnt = 0;
		
		barracksCnt = 0;
		barracksConstCnt = 0;
		
		supplyDepotCnt = 0;
		supplyDepotConstCnt = 0;
		
		factoryCnt = 0;
		factoryConstCnt = 0;
		
		machineShopCnt = 0;
		machineShopConstCnt = 0;
		
		academyCnt = 0;
		academyConstCnt = 0;
		
		armoryCnt = 0;
		armoryConstCnt = 0;
		
		engbayCnt = 0;
		engbayConstCnt = 0;
		
		starportCnt = 0;
		starportConstCnt = 0;
		
		controlTowerCnt = 0;
		controlTowerConstCnt = 0;
		
		sciFacilityCnt = 0;
		sciFacilityConstCnt = 0;
		
		marineCnt = 0;
		marineConstCnt = 0;
		
		tankCnt = 0;
		tankConstCnt = 0;
		
		vultureCnt = 0;
		vultureConstCnt = 0;
		
		goliathCnt = 0;
		goliathConstCnt = 0;
		
		dropshipCnt = 0;
		dropshipConstCnt = 0;
		
		wraithCnt = 0;
		wraithConstCnt = 0;
		
		valkyrieCnt = 0;
		valkyrieConstCnt = 0;
		
		vesselCnt = 0;
		vesselConstCnt = 0;
		
		
		targetUnitWhenScouting = null;
		enemyWorkerAroundScouterCnt = 0;
		enemyUnitAttackingScouterCnt = 0;
		
		underAttackBuildingCnt = 0;
		underAttackUnitCnt = 0;
		
		underAttackScvCnt = 0;
	}
	
	
	
//	private void whoseRegion(Region region, int pidx) {
//		_Territory terri = regionTerritoryMap.get(region);
//		if (terri != null) {
//			terri.addOwner(pidx);
//			playerTerritories[pidx].add(terri);
//		}
//	}
	
	private boolean isAlive(Unit unit) {
		return unit != null && unit.exists() && unit.getHitPoints() > 0;
	}
	

	@Override
	public void updateSelfUnitInfo(_UnitInfo ui) {
		if (ui.getType().isBuilding()) {
//			whoseRegion(BWTA.getRegion(ui.getUnit().getPosition()), 0);
			
			if (ui.getUnit().isUnderAttack()) {
				underAttackBuildingCnt++;
			}
			
			if (ui.getUnit().isCompleted()) {
				
				supplyProvidedCnt += ui.getType().supplyProvided();
				if (ui.getType().equals(Terran_Command_Center)) cmdCenterCnt++;
				else if (ui.getType().equals(Terran_Barracks)) barracksCnt++;
				else if (ui.getType().equals(Terran_Supply_Depot)) supplyDepotCnt++;
				else if (ui.getType().equals(Terran_Factory)) factoryCnt++;
				else if (ui.getType().equals(Terran_Machine_Shop)) machineShopCnt++;
				else if (ui.getType().equals(Terran_Academy)) academyCnt++;
				else if (ui.getType().equals(Terran_Armory)) armoryCnt++;
				else if (ui.getType().equals(Terran_Engineering_Bay)) engbayCnt++;
				else if (ui.getType().equals(Terran_Starport)) starportCnt++;
				else if (ui.getType().equals(Terran_Control_Tower)) controlTowerCnt++;
				else if (ui.getType().equals(Terran_Science_Facility)) sciFacilityCnt++;
			} else {
				supplyProvidedConstCnt += ui.getType().supplyProvided();
				if (ui.getType().equals(Terran_Command_Center)) cmdCenterConstCnt++;
				else if (ui.getType().equals(Terran_Barracks)) barracksConstCnt++;
				else if (ui.getType().equals(Terran_Supply_Depot)) supplyDepotConstCnt++;
				else if (ui.getType().equals(Terran_Factory)) factoryConstCnt++;
				else if (ui.getType().equals(Terran_Machine_Shop)) machineShopConstCnt++;
				else if (ui.getType().equals(Terran_Academy)) academyConstCnt++;
				else if (ui.getType().equals(Terran_Armory)) armoryConstCnt++;
				else if (ui.getType().equals(Terran_Engineering_Bay)) engbayConstCnt++;
				else if (ui.getType().equals(Terran_Starport)) starportConstCnt++;
				else if (ui.getType().equals(Terran_Control_Tower)) controlTowerConstCnt++;
				else if (ui.getType().equals(Terran_Science_Facility)) sciFacilityConstCnt++;
			}
			
		} else {
			
			if (isAlive(ui.getUnit())) {
				
				if (ui.getUnit().isIdle()) {
					idleCnt[ui.getUnitID()]++;
				}
				
				if (ui.getUnit().isUnderAttack()) {
					underAttackUnitCnt++;
				}
				
				if (ui.getUnit().isCompleted()) {
					unitCnt++;
					supplyOccupiedCnt += ui.getType().supplyRequired();
					//supplyProvidedCnt += ui.getType().supplyProvided(); // zerg가 아니면 의미 없으므로 주석처리
					if (ui.getType().equals(Terran_SCV)) {
						scvCnt++;
						
						if (idleCnt[ui.getUnitID()] >= 50) {
							idleCnt[ui.getUnitID()] = 0;
							workers.setIdleWorker(ui.getUnit());
//							__Util.println(ui.getUnitID() + " : idle ..");
						}
						
						//workers.getWorkerData().getJobCode(unit)
						//if (!ui.getUnit().hasPath(ui.getUnit().getTargetPosition())) {
//						if (!ui.getUnit().canGather(ui.getUnit().getTarget())) {
//							scvNoPathCnt++;
//						}
						if (ui.getUnit().equals(scouter.currentScoutUnit)) {
							if (ui.getUnit().isUnderAttack()) {
								enemyUnitAttackingScouterCnt++;
							}
						} else {
						
							if (ui.getUnit().isConstructing()) {
								if (ui.getUnit().isUnderAttack()) {
									workers.setCombatWorker(ui.getUnit());
									ui.getUnit().stop();
								}
							} else {
								
								if (ui.getUnit().isUnderAttack()) {
									underAttackScvCnt++;
								}
								if (state == defense_base) {
//									command.attackMove(ui.getUnit(), ui.getLastPosition());
									workers.setCombatWorker(ui.getUnit());
								}
							}
							
						}
						
					} else if (ui.getType().equals(Terran_Marine)) marineCnt++;
					else if (ui.getType().equals(Terran_Siege_Tank_Tank_Mode) || ui.getType().equals(Terran_Siege_Tank_Siege_Mode)) tankCnt++;
					else if (ui.getType().equals(Terran_Vulture)) vultureCnt++;
					else if (ui.getType().equals(Terran_Goliath)) goliathCnt++;
					else if (ui.getType().equals(Terran_Dropship)) dropshipCnt++;
					else if (ui.getType().equals(Terran_Wraith)) wraithCnt++;
					else if (ui.getType().equals(Terran_Valkyrie)) valkyrieCnt++;
					else if (ui.getType().equals(Terran_Science_Vessel)) vesselCnt++;
				} else {
					unitConstCnt++;
					supplyOccupiedConstCnt += ui.getType().supplyRequired();
					supplyProvidedConstCnt += ui.getType().supplyProvided(); // zerg가 아니면 의미 없으므로 주석처리
					if (ui.getType().equals(Terran_SCV)) scvConstCnt++;
					else if (ui.getType().equals(Terran_Marine)) marineConstCnt++;
					else if (ui.getType().equals(Terran_Siege_Tank_Tank_Mode) || ui.getType().equals(Terran_Siege_Tank_Siege_Mode)) tankConstCnt++;
					else if (ui.getType().equals(Terran_Vulture)) vultureConstCnt++;
					else if (ui.getType().equals(Terran_Goliath)) goliathConstCnt++;
					else if (ui.getType().equals(Terran_Dropship)) dropshipConstCnt++;
					else if (ui.getType().equals(Terran_Wraith)) wraithConstCnt++;
					else if (ui.getType().equals(Terran_Valkyrie)) valkyrieConstCnt++;
					else if (ui.getType().equals(Terran_Science_Vessel)) vesselConstCnt++;
				}
				
			}
		}
	}
	

	@Override
	public void updateEnemyUnitInfo(_UnitInfo eui) {
		try {
			if (eui == null || eui.getUnit().getPlayer() != _Common.ENEMY) return;
			if (eui.getType().isBuilding()) {
				//whoseRegion(eui.getUnit());
				// 속도 저하의 원인인가???
//				whoseRegion(BWTA.getRegion(eui.getUnit().getPosition()), 1);
				if (targetPosition != null) {
					double dist = targetPosition.getDistance(eui.getUnit().getPosition());
					//Util.println("building : " + eui.getUnit().getType().toString() + " - " + dist);
					if (minDist < 0.0 || minDist > dist) {
						minDist = dist;
						closestEnemyBuildingInThisFrame = eui;
					}
				}
			} else {
				if (!scouter.nullScoutUnit()) {
					//if ((eui.getUnit().isFollowing() || eui.getUnit().isAttacking()) && eui.getUnit().getOrderTarget().equals(scouter.currentScoutUnit)) {
					if (eui.getUnit().isFollowing() || eui.getUnit().isAttacking()) {
						if ((eui.getUnit().getTarget() != null && eui.getUnit().getTarget().equals(scouter.currentScoutUnit)) || (eui.getUnit().getOrderTarget() != null && eui.getUnit().getOrderTarget().equals(scouter.currentScoutUnit))) {
							enemyUnitAttackingScouterCnt++;
						}
					}
					if (eui.getType().equals(Terran_SCV) || eui.getType().equals(Protoss_Probe) || eui.getType().equals(Zerg_Drone)) {
						if (scouter.currentScoutUnit.getDistance(eui.getUnit()) <= 4 * Config.TILE_SIZE) {
							enemyWorkerAroundScouterCnt++;
							if (targetUnitWhenScouting == null || targetUnitWhenScouting.exists() == false || targetUnitWhenScouting.getHitPoints() <= 0) targetUnitWhenScouting = eui.getUnit(); 
						}
					}
				}
			}
		} catch (Exception e) {
			__Util.println("targetPosition : " + targetPosition);
			__Util.println("eui.getUnit().getPosition() : " + eui.getUnit().getPosition().toTilePosition());
			throw e;
		}		
	}

	
	@Override
	public void build() {
		
		if (_Common.Broodwar.getFrameCount() % 500 == 0) {
			__Util.println("");
			__Util.println("Command Center Cnt : " + cmdCenterCnt);
			__Util.println("cmdCenterConstCnt : " + cmdCenterConstCnt);
			__Util.println("getSupplyOccupiedCount() : " + getSupplyOccupiedCount());
			__Util.println("getSupplyProvidedCount() : " + getSupplyProvidedCount());
			//__Util.println("scvNoPathCnt : " + scvNoPathCnt);			
			__Util.println("enemyUnitAttackingScouterCnt : " + enemyUnitAttackingScouterCnt + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
			__Util.println("targetPosition : " + ((targetPosition == null) ? "null" : targetPosition.toTilePosition()));
			__Util.println("closestEnemyBuildingInThisFrame : " + ((closestEnemyBuildingInThisFrame == null) ? "null" : closestEnemyBuildingInThisFrame.getLastPosition().toTilePosition()));
			__Util.println("state : " + state.value);
		}
		
		if (_Common.Broodwar.getFrameCount() % 6 == 0) {
			assignScout();
			executeScouting();
		}
		
		
		if (isBaseState(state, defense)) {
			if (underAttackUnitCnt + underAttackBuildingCnt == 0) state = initState;
		}
		
		if (state == initState) {
			if (underAttackScvCnt > 0) state = defense_base;
		}
		
		
		if (cmdCenterCnt + cmdCenterConstCnt + cmdCenterTryCnt <= 0) {
			
		}
		
		
		
		
		if (scvCnt + scvConstCnt + bq.getItemCount(Terran_SCV) < 8) {
			bq.queueAsLowestPriority(Terran_SCV);
			return;
		}
		
		if (supplyDepotCnt + supplyDepotConstCnt + supplyDepotTryCnt < 0) {
			construct(Terran_Supply_Depot, mapStrategy.getFirstSupplyDepotTilePosition(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			return;
		}
		
		if (scvCnt + scvConstCnt + scvTryCnt < 10) {
			bq.queueAsLowestPriority(Terran_SCV);
			return;
		}
		
		
		
		
		
		
		
		
		
//		if (cmdCenterCnt + cmdCenterConstCnt < 2) {
//			if (!flag) { 
//				bq.queueAsHighestPriority(Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
//				flag = true;
//			}
//		}
		
		
//		if (state == initState) {
//			if (bq.isEmpty()) state = wait_attack;
//			// 공격 받는 건지
//		}
		
		

		
	} 
	
	
	private void assignScout() {
//		BaseLocation enemyBaseLocation = _InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
//
//		if (enemyBaseLocation == null)
//		{
//			if (scouter.currentScoutUnit == null || scouter.currentScoutUnit.exists() == false || scouter.currentScoutUnit.getHitPoints() <= 0)
//			{
//				
//			}
//		}
		
		scouter.assignScoutIfNeeded();
	}


	private void executeScouting() {
		
		if (enemyUnitAttackingScouterCnt > 0) {
			scouter.moveScoutUnit(1, null);	// 빙글 빙글 돈다	
			__Util.println(" > run away : " + enemyUnitAttackingScouterCnt);
		} else {
			//if (enemyWorkerAroundScouterCnt > 0 && targetUnitWhenScouting != null && targetUnitWhenScouting.exists() && targetUnitWhenScouting.getHitPoints() > 0) {
			if (enemyWorkerAroundScouterCnt > 0 && isAlive(targetUnitWhenScouting)) {
				scouter.moveScoutUnit(2, targetUnitWhenScouting);
			} else {
				scouter.moveScoutUnit(1, null);
			}
		}
		
//		scouter.moveScoutUnit(1, null);
//		scouter.moveScoutUnit();
		
		
	}


	private int getSupplyProvidedCount() {
		return supplyProvidedCnt + supplyProvidedConstCnt;
	}

	private int getSupplyOccupiedCount() {
		return supplyOccupiedCnt + supplyOccupiedConstCnt;
	}
	
	private boolean isBaseState(State state, State base) {
		return state.value / 100 == base.value;
	}

}
