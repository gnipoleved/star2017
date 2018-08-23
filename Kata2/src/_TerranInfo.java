import static bwapi.UnitType.*;
import bwapi.Unit;

import java.util.ArrayList;
import java.util.List;

public class _TerranInfo {

    private static WorkerManager workers = WorkerManager.Instance();
    private static ScoutManager scouter = ScoutManager.Instance();

    public int[] idleCnt;

    public int unitCnt = 0;
    public int unitConstCnt = 0;

    public int supplyOccupiedCnt = 0;
    public int supplyOccupiedConstCnt = 0;

    public int supplyProvidedCnt = 0;
    public int supplyProvidedConstCnt = 0;

    public int scvCnt;
    public int scvConstCnt;
    //public int scvNoPathCnt;
    public int scvTryCnt = 0;

    public int cmdCenterCnt = 0;
    public int cmdCenterConstCnt = 0;
    public int cmdCenterTryCnt = 0;

    public int comsatStationCnt = 0;
    public int comsatStationConstCnt = 0;

    public int barracksCnt = 0;
    public int barracksConstCnt = 0;
    public int barracksTryCnt = 0;

    public int supplyDepotCnt = 0;
    public int supplyDepotConstCnt = 0;
    public int supplyDepotTryCnt = 0;

    public int refineryCnt = 0;
    public int refineryConstCnt = 0;

    public int factoryCnt = 0;
    public int factoryConstCnt = 0;
    public int factoryTryCnt = 0;

    public int machineShopCnt = 0;
    public int machineShopConstCnt = 0;
    public int machineShopTryCnt = 0;

    public int academyCnt = 0;
    public int academyConstCnt = 0;
    public int academyTryCnt = 0;

    public int armoryCnt = 0;
    public int armoryConstCnt = 0;
    public int armoryTryCnt = 0;

    public int engbayCnt = 0;
    public int engbayConstCnt = 0;
    public int engbayTryCnt = 0;

    public int starportCnt = 0;
    public int starportConstCnt = 0;
    public int starportTryCnt = 0;

    public int controlTowerCnt = 0;
    public int controlTowerConstCnt = 0;
    public int controlTowerTryCnt = 0;

    public int sciFacilityCnt = 0;
    public int sciFacilityConstCnt = 0;
    public int sciFacilityTryCnt = 0;

    public int marineCnt = 0;
    public int marineConstCnt = 0;
    public int marineTryCnt = 0;

    public int medicCnt = 0;
    public int medicConstCnt = 0;

    public int tankCnt = 0;
    public int tankConstCnt = 0;
    public int tankTryCnt = 0;

    public int vultureCnt = 0;
    public int vultureConstCnt = 0;
    public int vultureTryCnt = 0;

    public int goliathCnt = 0;
    public int goliathConstCnt = 0;
    public int goliathTryCnt = 0;

    public int dropshipCnt = 0;
    public int dropshipConstCnt = 0;
    public int dropshipTryCnt = 0;

    public int wraithCnt = 0;
    public int wraithConstCnt = 0;
    public int wraithTryCnt = 0;

    public int valkyrieCnt = 0;
    public int valkyrieConstCnt = 0;
    public int valkyrieTryCnt = 0;

    public int vesselCnt = 0;
    public int vesselConstCnt = 0;
    public int vesselTryCnt = 0;

    public int underAttackBuildingCnt = 0;
    public int underAttackUnitCnt = 0;

    //public int underAttackScvCnt = 0;

    public Unit targetUnitWhenScouting = null;
    public int enemyWorkerAroundScouterCnt = 0;
    public int enemyUnitAttackingScouterCnt = 0;

    public Unit underAttackUnit;
    public Unit underAttackBuilding;

    public List<Unit> enemyHided = new ArrayList<>();
    public List<Integer> frameForHided = new ArrayList<>();

    public List<Unit> comsatStations = new ArrayList<>();

    public List<Unit> tanks = new ArrayList<>();

    public List<UnitInfo> self_units = new ArrayList<>();
    public List<UnitInfo> self_marines = new ArrayList<>();
    public List<UnitInfo> self_medics = new ArrayList<>();
    public List<UnitInfo> self_underAttackSCVS = new ArrayList<>();


    public List<UnitInfo> list_cmdCenter = new ArrayList<>();   public int limit_cmdCenter; // limit 값들은 각 strategy 에서 변경한다.
    public List<UnitInfo> list_supplyDepot = new ArrayList<>(); public int limit_supplyDepot;
    public List<UnitInfo> list_barracks = new ArrayList<>();    public int limit_barracks;
    public UnitInfo academy;


    // 매 frame 먀다 초기화 해서 count 또는 add 하는 변수들
    public void init() {
        list_cmdCenter.clear();
        list_supplyDepot.clear();
        list_barracks.clear();

        self_units.clear();
        self_marines.clear();
        self_medics.clear();
        //self_underAttackSCVS.clear();	// 이건 매 frame 마다 초기화 히지 않고.. 한번 add 된 건 starategyManager 에서 underAttack 아닌 경우 해제 하는 걸로...

        idleCnt = new int[3000];

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

        comsatStationCnt = 0;
        comsatStationConstCnt = 0;

        barracksCnt = 0;
        barracksConstCnt = 0;

        supplyDepotCnt = 0;
        supplyDepotConstCnt = 0;

        refineryCnt = 0;
        refineryConstCnt = 0;

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

        medicCnt = 0;
        medicConstCnt = 0;

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

        //underAttackScvCnt = 0;

        underAttackUnit = null;
        underAttackBuilding = null;

        enemyHided.clear();
        frameForHided.clear();

        comsatStations.clear();

        tanks.clear();

        enemy_listProbe.clear();
        enemy_listZealot.clear();
        enemy_listDragon.clear();

        academy = null;

        //enemy_listPhoton.clear();
    }

    public void updateSelfUnitInfo(UnitInfo ui) {
        if (ui.getType().isBuilding()) {

            if (ui.getUnit().isUnderAttack()) {
                underAttackBuildingCnt++;
                underAttackBuilding = ui.getUnit();
            }

            if (ui.getUnit().isCompleted()) {

                supplyProvidedCnt += ui.getType().supplyProvided();
                if (ui.getType().equals(Terran_Command_Center)) {cmdCenterCnt++;    list_cmdCenter.add(ui);}
                else if (ui.getType().equals(Terran_Comsat_Station)) {comsatStationCnt++;	comsatStations.add(ui.getUnit());	}
                else if (ui.getType().equals(Terran_Barracks)) {barracksCnt++;  list_barracks.add(ui);}
                else if (ui.getType().equals(Terran_Supply_Depot)) {supplyDepotCnt++;   list_supplyDepot.add(ui);}
                else if (ui.getType().equals(Terran_Refinery)) {refineryCnt++;}
                else if (ui.getType().equals(Terran_Factory)) factoryCnt++;
                else if (ui.getType().equals(Terran_Machine_Shop)) machineShopCnt++;
                else if (ui.getType().equals(Terran_Academy)) { academyCnt++; academy = ui;}
                else if (ui.getType().equals(Terran_Armory)) armoryCnt++;
                else if (ui.getType().equals(Terran_Engineering_Bay)) engbayCnt++;
                else if (ui.getType().equals(Terran_Starport)) starportCnt++;
                else if (ui.getType().equals(Terran_Control_Tower)) controlTowerCnt++;
                else if (ui.getType().equals(Terran_Science_Facility)) sciFacilityCnt++;
            } else {
                supplyProvidedConstCnt += ui.getType().supplyProvided();
                if (ui.getType().equals(Terran_Command_Center)) cmdCenterConstCnt++;
                else if (ui.getType().equals(Terran_Comsat_Station)) comsatStationConstCnt++;
                else if (ui.getType().equals(Terran_Barracks)) barracksConstCnt++;
                else if (ui.getType().equals(Terran_Supply_Depot)) supplyDepotConstCnt++;
                else if (ui.getType().equals(Terran_Refinery)) {refineryConstCnt++;}
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

                self_units.add(ui);

                if (ui.getUnit().isIdle()) {
                    idleCnt[ui.getUnitID()]++;
                }

                if (ui.getUnit().isUnderAttack()) {
                    underAttackUnitCnt++;
                    underAttackUnit = ui.getUnit();
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
                        }

//						}
                        if (ui.getUnit().equals(scouter.getScoutUnit())) {
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
                                    //underAttackScvCnt++;
                                    self_underAttackSCVS.add(ui);
                                }
//                                if (state == defense_base) {
////									command.attackMove(ui.getUnit(), ui.getLastPosition());
//                                    workers.setCombatWorker(ui.getUnit());
//                                }
                            }

                        }

                    } else if (ui.getType().equals(Terran_Marine)) {marineCnt++;    self_marines.add(ui); }
                    else if (ui.getType().equals(Terran_Medic)) {medicCnt++;    self_medics.add(ui);}
                    else if (ui.getType().equals(Terran_Siege_Tank_Tank_Mode) || ui.getType().equals(Terran_Siege_Tank_Siege_Mode)) {tankCnt++;	tanks.add(ui.getUnit());}
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
                    else if (ui.getType().equals(Terran_Medic)) {medicConstCnt++;}
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

    
    // 상대 종족이 terran 인 경우에만 쓰임
    public int enemy_attackedCntBunkerConstScv = 0;
    public Unit enemy_attackedBunkerConstScv = null;

    public List<UnitInfo> enemy_listBuildings = new ArrayList<>();

    public List<UnitInfo> enemy_listProbe = new ArrayList<>();
    public List<UnitInfo> enemy_listZealot = new ArrayList<>();
    public List<UnitInfo> enemy_listDragon = new ArrayList<>();


    public void updateEnemyUnitInfo(UnitInfo eui) {
    	
    	if (eui == null || eui.getUnit().getPlayer() != MyBotModule.Broodwar.enemy()) return;
    	
    	if (eui.getType().isBuilding()) {

    		if (eui.getUnit().getType() == Terran_Bunker) {
    			if (eui.getUnit().isBeingConstructed()) {
	    			Unit buildUnit = eui.getUnit().getBuildUnit();
	    			if (buildUnit.isUnderAttack() && (enemy_attackedBunkerConstScv == null || !enemy_attackedBunkerConstScv.equals(buildUnit))) {
	    				enemy_attackedBunkerConstScv = buildUnit;
	    				enemy_attackedCntBunkerConstScv++;
	    			}
    			}
    		}
    	} else {
            if (eui.getUnit().isCompleted()) {
                if (eui.getUnit().getType().equals(Protoss_Probe)) { enemy_listProbe.add(eui);}
                else if (eui.getUnit().getType().equals(Protoss_Zealot)) { enemy_listZealot.add(eui);}
                else if (eui.getUnit().getType().equals(Protoss_Dragoon)) { enemy_listDragon.add(eui);}
            }
        }
    	
//        try {
//            if (eui == null || eui.getUnit().getPlayer() != MyBotModule.Broodwar.enemy()) return;
//
//            for (Unit tank : tanks) {
//                int dist = Common.getDistanceSquared(tank.getPosition(), eui.getUnit().getPosition());
//                if (dist <= 64*64) tank.unsiege();
//                else if (64*64 < dist && dist < 13*13) tank.siege();
//            }
//
//            if (eui.getType().isBuilding()) {
//                if (targetPosition != null) {
//                    double dist = targetPosition.getDistance(eui.getUnit().getPosition());
//                    if (minDist < 0.0 || minDist > dist) {
//                        minDist = dist;
//                        closestEnemyBuildingInThisFrame = eui;
//                    }
//                }
//            } else {
//                if (!scouter.nullScoutUnit()) {
//                    if (eui.getUnit().isFollowing() || eui.getUnit().isAttacking()) {
//                        if ((eui.getUnit().getTarget() != null && eui.getUnit().getTarget().equals(scouter.currentScoutUnit)) || (eui.getUnit().getOrderTarget() != null && eui.getUnit().getOrderTarget().equals(scouter.currentScoutUnit))) {
//                            enemyUnitAttackingScouterCnt++;
//                        }
//                    }
//                    if (eui.getType().equals(Terran_SCV) || eui.getType().equals(Protoss_Probe) || eui.getType().equals(Zerg_Drone)) {
//                        if (scouter.currentScoutUnit.getDistance(eui.getUnit()) <= 4 * Config.TILE_SIZE) {
//                            enemyWorkerAroundScouterCnt++;
//                            if (targetUnitWhenScouting == null || targetUnitWhenScouting.exists() == false || targetUnitWhenScouting.getHitPoints() <= 0) targetUnitWhenScouting = eui.getUnit();
//                        }
//                    }
//                    if ((eui.getType().equals(Zerg_Lurker) && eui.getUnit().isBurrowed()) || eui.getType().equals(Protoss_Dark_Templar) || eui.getType().equals(Terran_Ghost) || (eui.getType().equals(Terran_Wraith) && eui.getUnit().isCloaked())) {
//                        StrategyManager.Instance().canYouScanThis(eui.getUnit());
//                    }
//                }
//            }
//        } catch (Exception e) {
//            throw e;
//        }
    }


    private boolean isAlive(Unit unit) {
        return unit != null && unit.exists() && unit.getHitPoints() > 0;
    }


    public void removeEnemyUnit(Unit unit) {
        //enemy_listBuildings 에서 remove 할 건지 말 건지 생각...

    }

}
