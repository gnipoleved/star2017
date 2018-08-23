import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

import java.util.ArrayList;
import java.util.List;

import static bwapi.UnitType.*;

public abstract class _VsCommonStrategy extends _TerranStrategy {

	protected int conScvCnt = 0;
	protected Unit conScv1, conScv2;
//	protected List<Unit> bdgAttackers = new ArrayList<>();
//	protected List<Unit> workerAttackers = new ArrayList<>();
	
	protected Unit[] bdgAttackers = new Unit[2];
	protected Unit[] workerAttackers = new Unit[1];

	protected List<Unit> defenseWorkers;

	protected Common.State state;
	protected int frameAttackStarted;

	public static int STRATEGY_MODE = 1; // 0=only marine, 1=bionic, 2=vulture

	@Override
	public void start() {
		terranInfo.limit_cmdCenter = 4;
		terranInfo.limit_supplyDepot = 25;
		terranInfo.limit_barracks = 8;

		defenseWorkers = new ArrayList<>();

		state = Common.initState;
		frameAttackStarted = 0;

		_ScoutManager_8Brracks.Instance().base1st = mapTactic.Scout1stPosition();
		_ScoutManager_8Brracks.Instance().base2nd = mapTactic.Scout2ndPosition();
		_ScoutManager_8Brracks.Instance().base3rd = mapTactic.Scout3rdPosition();
	}

//	private boolean testFlag = false;
//	private int testint = 0;
//
//	private void showOut(UnitType unitType)
//	{
//		__Util.println("count whole: " + Common.WholeUnitCount(unitType) + "\tcount completed: " + Common.CountCompleted(unitType) + "\tcount Incompleted: " + Common.CountIncompleted(unitType)
//				+ "\tcount in-buildQ: " + Common.CountInBuildQ(unitType));
//		__Util.println("testint : " + testint);
//	}

	public void buildScvInHQ() {
		terranInfo.list_cmdCenter.get(0).getUnit().train(Terran_SCV);
	}

	public int getScvCount() {
		return terranInfo.scvCnt + terranInfo.scvConstCnt;// + terranInfo.list_cmdCenter.get(0).getUnit().getTrainingQueue().size();
	}

	public int getSupplyDepotCount() {
		return terranInfo.supplyDepotCnt + terranInfo.supplyDepotConstCnt + Common.CountInBuildQ(Terran_Supply_Depot) + Common.CountInConstQ(Terran_Supply_Depot);
	}

	@Override
	public void execute() {

//		if (terranInfo.scvCnt >= 18) Config.WorkersPerRefinery = 3;
//		else Config.WorkersPerRefinery = 1;

		if (terranInfo.self_underAttackSCVS.size() > 0) {
			if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) return;	// WokrerManager 처럼

			List<UnitInfo> tempUnderAttackScvList = new ArrayList<>();
			for (UnitInfo underAttackScv : terranInfo.self_underAttackSCVS) {

//				if (underAttackScv.getUnit().getHitPoints() <= 10) {
//					underAttackScv.getUnit().ga
//				}
				Unit scv = underAttackScv.getUnit();

				if (CommandUtil.IS_VALID_UNIT(scv)) {
					if (scv.isUnderAttack()) {
						tempUnderAttackScvList.add(underAttackScv);

						WorkerManager.Instance().setCombatWorker(scv);
						Unit target = WorkerManager.Instance().getClosestEnemyUnitFromWorker(scv);
						if (CommandUtil.IS_VALID_UNIT(target))
						{
							CommandUtil.ATTACK_UNIT(scv, target);
						}
					} else {
						WorkerManager.Instance().setMineralWorker(scv);
					}
				}
			}
			terranInfo.self_underAttackSCVS = tempUnderAttackScvList;
			return;
		}

		if (terranInfo.underAttackBuilding != null) {
			if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) return;	// WokrerManager 처럼

			if (terranInfo.list_cmdCenter.get(0).getUnit().getDistance(terranInfo.underAttackBuilding.getPosition()) < 20*32) {
				List<Unit> unitsNearAttack = MyBotModule.Broodwar.getUnitsInRadius(terranInfo.underAttackBuilding.getPosition(), 10*32);
				Unit enemy = null;
				for (Unit unit : unitsNearAttack) {

					if (CommandUtil.IS_VALID_UNIT(unit)) {
						if (unit.getPlayer().equals(InformationManager.Instance().enemyPlayer)) {
							enemy = unit;
							break;
						}
					}

				}
				if(CommandUtil.IS_VALID_UNIT(enemy)) {
					int defCnt = 0;
					List<Unit> tmp = new ArrayList<>();
					for (Unit def : defenseWorkers) {
						if (CommandUtil.IS_VALID_UNIT(def)) {
							defCnt++;
							tmp.add(def);
						}
					}
					defenseWorkers = tmp;

					for(Unit unit: MyBotModule.Broodwar.getUnitsInRadius(enemy.getPosition(), 10*32)) {
						if (defCnt < 1 && CommandUtil.IS_VALID_UNIT(unit) && unit.getPlayer().equals(InformationManager.Instance().selfPlayer) && unit.getType().equals(Terran_SCV)) {
							defenseWorkers.add(unit);
						}
					}

					for (Unit def : defenseWorkers) {
						if (def.getDistance(mapTactic.StartingPosition().toPosition()) > 10*32) {
							WorkerManager.Instance().setMineralWorker(def);
						} else {
							WorkerManager.Instance().setCombatWorker(def);
							CommandUtil.ATTACK_UNIT(def, enemy);
						}
					}
				} else {
					for (Unit def : defenseWorkers) {
						WorkerManager.Instance().setMineralWorker(def);
					}
				}
			}
			return;
		}

//		__Util.println("^_______________________________________________________________________^");
		if (MyBotModule.Broodwar.getFrameCount() % 24 == 0) {	// WokrerManager 처럼
			UnitInfo targetEnemyBdgInMyRegion = null;
			boolean inMyRegion = false;
			for (UnitInfo enemyBuilding : terranInfo.enemy_listBuildings) {
	
				if (!CommandUtil.IS_VALID_UNIT_2(enemyBuilding.getUnit())) continue;
//				BaseLocation nearestBaseLocation = BWTA.getNearestBaseLocation(enemyBuilding.getUnit().getPosition());
//				__Util.println(enemyBuilding.getUnit().getType() + ":(" + enemyBuilding.getUnit().getID() + ") >>> " 
//						+ nearestBaseLocation.getTilePosition() + " / " + MyBotModule.Broodwar.self().getStartLocation().toPosition());
//				if (nearestBaseLocation.equals(InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()))
//				__Util.println(enemyBuilding.getUnit().getType().toString() + " : " + 
//						BWTA.getRegion(enemyBuilding.getLastPosition()).toString() + " / " + BWTA.getRegion(MyBotModule.Broodwar.self().getStartLocation()).toString());
				if (BWTA.getRegion(enemyBuilding.getLastPosition()) == BWTA.getRegion(MyBotModule.Broodwar.self().getStartLocation())) {
					inMyRegion = true;
					targetEnemyBdgInMyRegion = enemyBuilding;
					for (int index = 0; index < bdgAttackers.length; index++) {
						//if (bdgAttackers.size() < index + 1) break;
						if (!CommandUtil.IS_VALID_UNIT(bdgAttackers[index])) {
							Unit worker = WorkerManager.Instance().chooseConstuctionWorkerClosestTo(Terran_Supply_Depot, enemyBuilding.getUnit().getPosition().toTilePosition(), false, 0);
							__Util.println("          bdg setup  >> " + ((worker!=null) ? worker.getID() : "null worker"));
							WorkerManager.Instance().setCombatWorker(worker);
							bdgAttackers[index] = worker;
						}
					}
					for (int index = 0; index < workerAttackers.length; index++) {
						//if (workerAttackers.size() < index+1) break;
						if (!CommandUtil.IS_VALID_UNIT(workerAttackers[index])) {
							Unit worker = WorkerManager.Instance().chooseConstuctionWorkerClosestTo(Terran_Supply_Depot, enemyBuilding.getUnit().getPosition().toTilePosition(), false, 0);
							WorkerManager.Instance().setCombatWorker(worker);
							__Util.println("        work att setup    >> " + ((worker!=null) ? worker.getID() : "null worker"));
							workerAttackers[index] = worker;
						}
					}
				}
			}
	
			if (inMyRegion) {
				__Util.println("inMyRegion ? : " + inMyRegion);
				//				__Util.println("bdgAttackers size : " + bdgAttackers.size());
//				__Util.println("workerAttacker size : " + workerAttackers.size());
				if (CommandUtil.IS_VALID_UNIT_2(targetEnemyBdgInMyRegion.getUnit())) {
					for (Unit bdgAttacker : bdgAttackers) {
						__Util.println("     bdt attacking >>> " + (bdgAttacker!=null?bdgAttacker.getID()+"":"null bdg"));
						if (CommandUtil.IS_VALID_UNIT(bdgAttacker)) CommandUtil.ATTACK_UNIT(bdgAttacker, targetEnemyBdgInMyRegion.getUnit());
					}
					for (Unit workerAttacker : workerAttackers) {
						__Util.println("     worker attacking >>> " + (workerAttacker!=null?workerAttacker.getID()+"":"null workerAttacker"));
						Unit target = WorkerManager.Instance().getClosestEnemyUnitNotBdg(workerAttacker);
						if (CommandUtil.IS_VALID_UNIT_2(target)) {
							if (CommandUtil.IS_VALID_UNIT(workerAttacker)) CommandUtil.ATTACK_UNIT(workerAttacker, target);
						}
					}
				}
			} else {
				for (Unit bdgAttacker : bdgAttackers) {
					if (CommandUtil.IS_VALID_UNIT(bdgAttacker)) {
						WorkerManager.Instance().setMineralWorker(bdgAttacker);
						__Util.println("     bdg revoke >>> " + (bdgAttacker!=null?bdgAttacker.getID()+"":"null bdg"));
					}
				}
				bdgAttackers = new Unit[bdgAttackers.length];
//				bdgAttackers = new Unit[1];
				for (Unit workerAttacker : workerAttackers) {
					if (CommandUtil.IS_VALID_UNIT(workerAttacker)) {
						WorkerManager.Instance().setMineralWorker(workerAttacker);
						__Util.println("     worker revoke >>> " + (workerAttacker!=null?workerAttacker.getID()+"":"null bdg"));
					}
				}
				workerAttackers = new Unit[workerAttackers.length];
//				workerAttackers = new Unit[2];
			}
		}

		if (getScvCount() < 8)
		{
			if (MyBotModule.Broodwar.self().minerals() >= 50) {
				if (terranInfo.list_cmdCenter.get(0).getUnit().getTrainingQueue().isEmpty()) {
					buildScvInHQ();
					//return;
				}
			}

			if (conScvCnt == 0 && getScvCount() == 7 && terranInfo.list_cmdCenter.get(0).getUnit().getRemainingTrainTime() < 180) {
				for (Unit scv : WorkerManager.Instance().getWorkerData().getWorkers()) {
					if (WorkerManager.Instance().isMineralWorker(scv) && !scv.isCarryingMinerals()) {
						conScv1 = scv;
						conScvCnt = 1;
						__Util.println("        ~~~~ conscv1 pickedup : " + conScv1.getID());
						WorkerManager.Instance().moveWorkerTo(conScv1, MapGrid.GetTileFromPool(64, 64));
						break;
					}
				}
				if (conScvCnt >= 1) return;
			}

			return;
		}

		if (conScvCnt == 1 && terranInfo.scvCnt == 8) {
			for (Unit scv : WorkerManager.Instance().getWorkerData().getWorkers()) {
				if (WorkerManager.Instance().isMineralWorker(scv) && !scv.isCarryingMinerals()) {
					conScv2 = scv;
					conScvCnt = 2;
					__Util.println("        ~~~~ conscv2 pickedup : " + conScv2.getID());
					WorkerManager.Instance().moveWorkerTo(conScv2, MapGrid.GetTileFromPool(64, 64));
					break;
				}
			}
			if (conScvCnt >= 2) return;
		}
		
		// barracks 짓던 scv 가 4드론에 당해 죽었을 경우 어떻게 할건지 생각
		if (conScvCnt == 2 && terranInfo.scvCnt >= 8) {
			if (terranInfo.barracksCnt + terranInfo.barracksConstCnt < 1) {
				if (CommandUtil.IS_VALID_UNIT(conScv1)) conScv1.build(Terran_Barracks, MapGrid.GetTileFromPool(60, 62));
				return;
			}
			if (terranInfo.barracksCnt + terranInfo.barracksConstCnt == 1 ) {
				if (CommandUtil.IS_VALID_UNIT(conScv2)) conScv2.build(Terran_Barracks, MapGrid.GetTileFromPool(64, 62));
				return;
			}
		}
		
		if (conScvCnt >= 2) {
//			if (!CommandUtil.IS_VALID_UNIT(conScv1)) {
//				for (Unit scv : WorkerManager.Instance().getWorkerData().getWorkers()) {
//					if (WorkerManager.Instance().isMineralWorker(scv) && !scv.isCarryingMinerals()) {
//						conScv1 = scv;
//						__Util.println("        ~~~~ conscv1 pickedup again : " + conScv1.getID());
//						WorkerManager.Instance().moveWorkerTo(conScv1, MapGrid.GetTileFromPool(64, 64));
//						break;
//					}
//				}
//			}
//			if (!CommandUtil.IS_VALID_UNIT(conScv2)) {
//				for (Unit scv : WorkerManager.Instance().getWorkerData().getWorkers()) {
//					if (WorkerManager.Instance().isMineralWorker(scv) && !scv.isCarryingMinerals()) {
//						conScv2 = scv;
//						__Util.println("        ~~~~ conscv2 pickedup again : " + conScv2.getID());
//						WorkerManager.Instance().moveWorkerTo(conScv1, MapGrid.GetTileFromPool(64, 62));
//						break;
//					}
//				}
//			}
			for (UnitInfo barrack : terranInfo.list_constBarracks) {
				if (barrack.getUnit().getTilePosition().equals(MapGrid.GetTileFromPool(64, 64))) {
					if (barrack.getUnit().getBuildUnit() == null) {
						if (!CommandUtil.IS_VALID_UNIT(conScv1)) {
							for (Unit scv : WorkerManager.Instance().getWorkerData().getWorkers()) {
								if (WorkerManager.Instance().isMineralWorker(scv) && !scv.isCarryingMinerals()) {
									conScv1 = scv;
									__Util.println("        ~~~~ conscv1 pickedup again : " + conScv1.getID());
									//WorkerManager.Instance().moveWorkerTo(conScv1, MapGrid.GetTileFromPool(64, 64));
									break;
								}
							}
						}
						CommandUtil.RIGHT_CLICK(conScv1, barrack.getUnit());
					}
				}
				if (barrack.getUnit().getTilePosition().equals(MapGrid.GetTileFromPool(64, 62))) {
					if (barrack.getUnit().getBuildUnit() == null) {
						if (!CommandUtil.IS_VALID_UNIT(conScv2)) {
							for (Unit scv : WorkerManager.Instance().getWorkerData().getWorkers()) {
								if (WorkerManager.Instance().isMineralWorker(scv) && !scv.isCarryingMinerals()) {
									conScv2 = scv;
									__Util.println("        ~~~~ conscv2 pickedup again : " + conScv2.getID());
									//WorkerManager.Instance().moveWorkerTo(conScv1, MapGrid.GetTileFromPool(64, 62));
									break;
								}
							}
						}
						CommandUtil.RIGHT_CLICK(conScv2, barrack.getUnit());
					}
				}
				
			}
		}

		// 8마리 scv 생산 이후에 barrack2 개 지은 이후에 supply 먼저 짓고, scv 한마리 더
		if (getScvCount() == 8 && terranInfo.barracksCnt + terranInfo.barracksConstCnt >= 2) {
			if (getSupplyDepotCount() == 0) {
				BuildManager.Instance().buildQueue.queueAsFixedPosition(Terran_Supply_Depot, mapTactic.SupplyDepot1Position(), true);
//				if (MyBotModule.Broodwar.self().minerals() >= 100) {
//					Unit supplyScv = WorkerManager.Instance().chooseConstuctionWorkerClosestTo(Terran_Supply_Depot, mapTactic.SupplyDepot1Position(), true, 0);
//					supplyScv.build(Terran_Supply_Depot, mapTactic.SupplyDepot1Position());
//				}
				return;
			}
			if (terranInfo.supplyDepotCnt == 0 && (terranInfo.supplyDepotConstCnt == 1)) {// || Common.CountInBuildQ(Terran_Supply_Depot) == 1 || Common.CountInConstQ(Terran_Supply_Depot) == 1)) {
				if (terranInfo.list_cmdCenter.get(0).getUnit().getTrainingQueue().isEmpty()) {
					if (MyBotModule.Broodwar.self().minerals() >= 50) buildScvInHQ();
					return;
				}
			}
		}

		// marine 생산 // return 로직 없음.
//		if (terranInfo.barracksCnt >= 1) {
//			Unit barrack1 = terranInfo.list_barracks.get(0).getUnit();
//			if (MyBotModule.Broodwar.self().minerals() >= 50 && barrack1.getTrainingQueue().isEmpty()) {
//				barrack1.train(Terran_Marine);
//			}
//			if (terranInfo.barracksCnt == 2) {
//				Unit barrack2 = terranInfo.list_barracks.get(1).getUnit();
//				if (MyBotModule.Broodwar.self().minerals() >= 50 && barrack2.getTrainingQueue().isEmpty()) {
//					barrack2.train(Terran_Marine);
//				}
//			}
//		}

		for (UnitInfo barrack : terranInfo.list_barracks) {
			if (CommandUtil.IS_VALID_UNIT(barrack.getUnit()) == false) continue;
			if (MyBotModule.Broodwar.self().minerals() >= 50 && barrack.getUnit().getTrainingQueue().isEmpty()) {
				//barrack.getUnit().train(Terran_Marine);
				if (STRATEGY_MODE == 1) {
					int medicUnitCnt = terranInfo.medicCnt + terranInfo.medicConstCnt;
					int marineUnitCnt = terranInfo.marineCnt + terranInfo.marineConstCnt;
					//double ratioMeMa = (marineUnitCnt == 0) ? 100.0 : ((double)medicUnitCnt/(double)(medicUnitCnt + marineUnitCnt)) * 100.0;
					//__Util.println("MEMA _____________________________:" + ratioMeMa);
					double ratioMeMa = 100.0;
//					if (!(terranInfo.academyCnt == 0 || marineUnitCnt < 6)) ratioMeMa = ((double)medicUnitCnt/(double)(medicUnitCnt + marineUnitCnt)) * 100.0;
					if (terranInfo.academyCnt == 0) ratioMeMa = 100.0;
					else {
						if (marineUnitCnt < 6) ratioMeMa = 100.0;
						else ratioMeMa = ((double)medicUnitCnt/(double)(medicUnitCnt + marineUnitCnt)) * 100.0;
					}

					if (ratioMeMa >= 19.8) {
						barrack.getUnit().train(Terran_Marine);
						terranInfo.marineConstCnt++;
					} else {
						barrack.getUnit().train(Terran_Medic);
					}
				}
			}
		}

		if (terranInfo.list_supplyDepot.size() >= 2 && getScvCount() < 22*terranInfo.list_cmdCenter.size() && (terranInfo.marineCnt > 7 || terranInfo.vultureCnt > 3)) {
			if (MyBotModule.Broodwar.self().minerals() >= 50) {
				if (terranInfo.list_cmdCenter.get(0).getUnit().getTrainingQueue().isEmpty()) {
					buildScvInHQ();
				}
			}
		}

		// barrack 1 완성 후 conSvc1 을 정찰 unit 으로 지정한다.
		if (terranInfo.barracksCnt >=1) {
			if (!WorkerManager.Instance().isScoutWorker(conScv1) && !WorkerManager.Instance().isCombatWorker(conScv1)) {
				//WorkerManager.Instance().setScoutWorker(conScv1);
				_ScoutManager_8Brracks.Instance().setScouter1(conScv1);
				return;
			}
		}

		// barrack 2 완성 후 conSvc2 을 정찰 unit 으로 지정한다.
		if (terranInfo.barracksCnt == 2) {
			if (!WorkerManager.Instance().isScoutWorker(conScv2) && !WorkerManager.Instance().isCombatWorker((conScv2))) {
				//WorkerManager.Instance().setScoutWorker(conScv2);
				_ScoutManager_8Brracks.Instance().setScouter2(conScv2);
				return;
			}
		}

		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
		Position firstAssemblyArea = null, targetAttackPos = null;
		if (enemyBaseLocation == null) {
			if (terranInfo.marineCnt >= 5 && !CommandUtil.IS_VALID_UNIT(conScv1) && !CommandUtil.IS_VALID_UNIT(conScv2)) {
				if (!MyBotModule.Broodwar.isExplored(mapTactic.Scout2ndPosition())) {
					state = Common.attack;
					firstAssemblyArea = mapTactic.Scout2ndPosition().toPosition();
					targetAttackPos = firstAssemblyArea;
				} else if (!MyBotModule.Broodwar.isExplored(mapTactic.Scout3rdPosition())) {
					state = Common.attack;
					firstAssemblyArea = mapTactic.Scout3rdPosition().toPosition();
					targetAttackPos = firstAssemblyArea;
				} else if (!MyBotModule.Broodwar.isExplored(mapTactic.Scout1stPosition())) {
					state = Common.attack;
					firstAssemblyArea = mapTactic.Scout1stPosition().toPosition();
					targetAttackPos = firstAssemblyArea;
				}
			}
		} else {
			firstAssemblyArea = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer).getCenter();
			targetAttackPos = enemyBaseLocation.getPosition();
		}
		
		int attack_marine_cnt = 2;
		//if (terranInfo.enmey_listPhoton.size() > 0) attack_marine_cnt = 7 * terranInfo.enmey_listPhoton.size();
		
		int enemyPhotonCnt = 0, enemyConstPhotonCnt=0, enemyNexusCnt = 0, enemyConstNext = 0;;
		for (UnitInfo ebdg : terranInfo.enemy_listBuildings) {
			if (ebdg.getUnit().getType()==Protoss_Photon_Cannon) {
				if (ebdg.getUnit().isCompleted()) {enemyPhotonCnt++;}
				else {enemyConstPhotonCnt++;}
//			} else if (ebdg.getUnit().getType()==Protoss_Nexus) {
				
			}
		}
		
		Position movePosition = null;
		
		if (enemyPhotonCnt > 0) {
			attack_marine_cnt = 7 * enemyPhotonCnt + enemyConstPhotonCnt;
			firstAssemblyArea = MapGrid.GetTileFromPool(64, 64).toPosition();
		} else {
			if (enemyConstPhotonCnt == 0) {
				if (InformationManager.Instance().enemy_first_expand) {
					if (enemyBaseLocation != null) movePosition = enemyBaseLocation.getPosition();
				}
			}
		}
		

		int range = 20 * 32;
		
		int cntAssembled = 0;
//		
		for (UnitInfo marine : terranInfo.self_marines) {
			if (Common.getDistanceSquared(firstAssemblyArea, marine.getUnit().getPosition()) < 64*64) cntAssembled++;
		}
		
		if (cntAssembled >= attack_marine_cnt) {
			state = Common.attack;
			frameAttackStarted = MyBotModule.Broodwar.getFrameCount();
		} else {
			state = Common.initState;
		}
		
		
//		if (state == Common.initState) {
//			int cntAssembled = 0;
//			
//			for (UnitInfo marine : terranInfo.self_marines) {
//				if (Common.getDistanceSquared(firstAssemblyArea, marine.getUnit().getPosition()) < 64*64) cntAssembled++;
//				if (cntAssembled >= attack_marine_cnt) {
//					state = Common.attack;
//					frameAttackStarted = MyBotModule.Broodwar.getFrameCount();
//					break;
//				}
//			}
//		}

		if (state == Common.initState || state == Common.attack) {
			
			if (movePosition != null) {
				for (UnitInfo offense : terranInfo.self_units) {

					if (!(offense.getType().equals(Terran_SCV) && (offense.getUnit().equals(conScv1) || offense.getUnit().equals(conScv2)))
							&& !offense.getType().equals(Terran_Marine) && !offense.getType().equals(Terran_Medic)) continue;	//  || !offense.getType().equals(Terran_SCV)) continue;
					
					CommandUtil.MOVE(offense.getUnit(), movePosition);
					
				}
			}

			if (unitControl(firstAssemblyArea, targetAttackPos, range)) return;

		}

		//__Util.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> " + (terranInfo.supplyProvidedCnt + terranInfo.supplyProvidedConstCnt - (terranInfo.supplyOccupiedCnt + terranInfo.supplyOccupiedConstCnt)));

		if (STRATEGY_MODE == 1 && BionicStrategy()) return;
	}

	protected boolean BionicStrategy() {
		if (terranInfo.scvCnt >= 20) Config.WorkersPerRefinery = 3;
		else Config.WorkersPerRefinery = 1;


		if (terranInfo.barracksCnt == 2) {
			//if (getScvCount() + terranInfo.marineCnt + terranInfo.ma)
			if (supplyRemaining() <= 2*2) {
				if (MyBotModule.Broodwar.self().minerals() >= 100 && Common.CountInBuildQ(Terran_Supply_Depot) == 0 && Common.CountInConstQ(Terran_Supply_Depot) == 0 && terranInfo.supplyDepotConstCnt == 0) {
					BuildManager.Instance().buildQueue.queueAsHighestPriority(Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
					return true;
				}
			}
		} else if (terranInfo.barracksCnt == 3) {
			if (supplyRemaining() <= 2*3) {
				if (MyBotModule.Broodwar.self().minerals() >= 100 && Common.CountInBuildQ(Terran_Supply_Depot) == 0 && Common.CountInConstQ(Terran_Supply_Depot) == 0 && terranInfo.supplyDepotConstCnt == 0) {
					BuildManager.Instance().buildQueue.queueAsHighestPriority(Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
					return true;
				}
			}
		} else if (terranInfo.barracksCnt == 4) {
			if (supplyRemaining() <= 2*7) {
				if (MyBotModule.Broodwar.self().minerals() >= 100 && Common.CountInBuildQ(Terran_Supply_Depot) == 0 && Common.CountInConstQ(Terran_Supply_Depot) == 0 && terranInfo.supplyDepotConstCnt == 0) {
					BuildManager.Instance().buildQueue.queueAsHighestPriority(Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
					return true;
				}
			}
		} else if (terranInfo.barracksCnt >= 5) {
			if (supplyRemaining() <= 2*8) {
				if (MyBotModule.Broodwar.self().minerals() >= 100 && Common.CountInBuildQ(Terran_Supply_Depot) == 0 && Common.CountInConstQ(Terran_Supply_Depot) == 0 && terranInfo.supplyDepotConstCnt == 0) {
					BuildManager.Instance().buildQueue.queueAsHighestPriority(Terran_Supply_Depot, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
					return true;
				}
			}
		}

		if (terranInfo.barracksCnt >= 2 && terranInfo.supplyDepotCnt + terranInfo.supplyDepotConstCnt >= 2) {
			if (terranInfo.supplyProvidedCnt + terranInfo.supplyProvidedConstCnt >= 16 * 2) {
				if (MyBotModule.Broodwar.self().minerals() >= 150 && Common.CountInBuildQ(Terran_Academy) == 0 && Common.CountInConstQ(Terran_Academy) == 0 && terranInfo.academyCnt + terranInfo.academyConstCnt == 0) {
					BuildManager.Instance().buildQueue.queueAsHighestPriority(Terran_Academy, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
					return true;
				}
			}

			if (terranInfo.supplyProvidedCnt + terranInfo.supplyProvidedConstCnt >= 16 * 2 && terranInfo.academyCnt + terranInfo.academyConstCnt > 0) {
				if (MyBotModule.Broodwar.self().minerals() >= 100 && Common.CountInBuildQ(Terran_Refinery) == 0 && Common.CountInConstQ(Terran_Refinery) == 0 && terranInfo.refineryCnt + terranInfo.refineryConstCnt == 0) {
					BuildManager.Instance().buildQueue.queueAsHighestPriority(Terran_Refinery, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
					return true;
				}
			}


			if (terranInfo.barracksCnt < 5) {
				if (terranInfo.marineConstCnt == terranInfo.barracksCnt && terranInfo.barracksConstCnt == 0 && Common.CountInBuildQ(Terran_Barracks) == 0 && Common.CountInConstQ(Terran_Barracks) == 0) {
					if (MyBotModule.Broodwar.self().minerals() >= 250) {
						BuildManager.Instance().buildQueue.queueAsHighestPriority(Terran_Barracks, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
						return true;
					}
				}
			} else if (terranInfo.barracksCnt < 7) {
				if (terranInfo.marineConstCnt == terranInfo.barracksCnt && terranInfo.barracksConstCnt == 0 && Common.CountInBuildQ(Terran_Barracks) == 0 && Common.CountInConstQ(Terran_Barracks) == 0) {
					if (MyBotModule.Broodwar.self().minerals() >= 400) {
						BuildManager.Instance().buildQueue.queueAsHighestPriority(Terran_Barracks, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
						return true;
					}
				}
			} else if (terranInfo.barracksCnt < 9) {
				if (terranInfo.marineConstCnt == terranInfo.barracksCnt && terranInfo.barracksConstCnt == 0 && Common.CountInBuildQ(Terran_Barracks) == 0 && Common.CountInConstQ(Terran_Barracks) == 0) {
					if (MyBotModule.Broodwar.self().minerals() >= 600) {
						BuildManager.Instance().buildQueue.queueAsHighestPriority(Terran_Barracks, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
						return true;
					}
				}
			}

			if (terranInfo.academyCnt > 0) {
				if (!MyBotModule.Broodwar.self().hasResearched(TechType.Stim_Packs) && MyBotModule.Broodwar.self().isResearching(TechType.Stim_Packs)) {
					if (MyBotModule.Broodwar.self().isResearchAvailable(TechType.Stim_Packs) && BuildManager.Instance().buildQueue.getItemCount(TechType.Stim_Packs) == 0) {
						if (MyBotModule.Broodwar.self().minerals() >= TechType.Stim_Packs.mineralPrice() && MyBotModule.Broodwar.self().gas() >= TechType.Stim_Packs.gasPrice()) {
							BuildManager.Instance().buildQueue.queueAsHighestPriority(TechType.Stim_Packs, false);
							return true;
						}
					}
				}

				if (tryToUpgrade(terranInfo.academy.getUnit(), UpgradeType.U_238_Shells)) return true;
			}
		}
		return false;
	}



	protected int supplyRemaining() {
		return terranInfo.supplyProvidedCnt + terranInfo.supplyProvidedConstCnt - (terranInfo.supplyOccupiedCnt + terranInfo.supplyOccupiedConstCnt);
	}

	protected boolean tryToUpgrade(Unit upgrader, UpgradeType upgType) {
		Player self = MyBotModule.Broodwar.self();
		int maxLvl = self.getMaxUpgradeLevel(upgType);
		int currentLvl = self.getUpgradeLevel(upgType);
		if ( !self.isUpgrading(upgType) && currentLvl < maxLvl && self.completedUnitCount(upgType.whatsRequired(currentLvl+1)) > 0 && self.completedUnitCount(upgType.whatUpgrades()) > 0 )
		{
			if (BuildManager.Instance().buildQueue.getItemCount(upgType) == 0) {
				return upgrader.upgrade(upgType);
			}
		}
		return false;
	}


	protected void controlCombatScouterScv(Unit scv, Position firstAssemblyArea, Unit targetUnit) {
		if (state == Common.initState) CommandUtil.MOVE(scv, firstAssemblyArea);
		else if (state == Common.attack) {
			UnitInfo closestMarine = null;
			int closestDist = -1;
			for (UnitInfo marine : terranInfo.self_marines) {
				int distMarine = scv.getDistance(marine.getUnit());
				if (closestDist < 0 || closestDist > distMarine) {
					closestMarine = marine;
					closestDist = distMarine;
				}
			}
			if (closestMarine == null) CommandUtil.MOVE(scv, firstAssemblyArea);
			else {
				if (closestDist <= CommandUtil.GET_ATTACK_RANGE(closestMarine.getUnit(), scv) * .75) {
					CommandUtil.ATTACK_MOVE(scv, targetUnit.getPosition());
				} else {
					CommandUtil.MOVE(scv, closestMarine.getUnit().getPosition());
				}
			}
		}
	}

	
	protected abstract boolean unitControl(Position firstAssemblyArea, Position targetAttackPos, int range);



	@Override
	public void handleNuclearAttack(Position target) {
		// TODO Auto-generated method stub

	}

}
