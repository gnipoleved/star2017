import static bwapi.UnitType.*;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

public class _VsTerranStrategy extends _TerranStrategy {


	protected int conScvCnt = 0;
	protected Unit conScv1, conScv2;

	protected Common.State state;
	protected int frameAttackStarted;

	@Override
	public void start() {
		terranInfo.limit_cmdCenter = 4;
		terranInfo.limit_supplyDepot = 25;
		terranInfo.limit_barracks = 8;

		state = Common.initState;
		frameAttackStarted = 0;

		_ScoutManager_8Brracks.Instance().base1st = mapTactic.Scout1stPosition();
		_ScoutManager_8Brracks.Instance().base2nd = mapTactic.Scout2ndPosition();
		_ScoutManager_8Brracks.Instance().base3rd = mapTactic.Scout3rdPosition();
	}
	
	private boolean testFlag = false;
	private int testint = 0;
	
	private void showOut(UnitType unitType)
	{
		__Util.println("count whole: " + Common.WholeUnitCount(unitType) + "\tcount completed: " + Common.CountCompleted(unitType) + "\tcount Incompleted: " + Common.CountIncompleted(unitType)
				+ "\tcount in-buildQ: " + Common.CountInBuildQ(unitType));
		__Util.println("testint : " + testint);
	}

	private void buildScvInHQ() {
		terranInfo.list_cmdCenter.get(0).getUnit().train(Terran_SCV);
	}

	private int getScvCount() {
		return terranInfo.scvCnt + terranInfo.scvConstCnt;// + terranInfo.list_cmdCenter.get(0).getUnit().getTrainingQueue().size();
	}

	private int getSupplyDepotCount() {
		return terranInfo.supplyDepotCnt + terranInfo.supplyDepotConstCnt + Common.CountInBuildQ(Terran_Supply_Depot) + Common.CountInConstQ(Terran_Supply_Depot);
	}

	@Override
	public void execute() {
		if (getScvCount() < 8)
		{
			if (MyBotModule.Broodwar.self().minerals() >= 50) {
				if (terranInfo.list_cmdCenter.get(0).getUnit().getTrainingQueue().isEmpty()) {
					buildScvInHQ();
					return;
				}
			}

			if (conScvCnt == 0 && getScvCount() == 7 && terranInfo.list_cmdCenter.get(0).getUnit().getRemainingTrainTime() < 180) {
				for (Unit scv : WorkerManager.Instance().getWorkerData().getWorkers()) {
					if (WorkerManager.Instance().isMineralWorker(scv) && !scv.isCarryingMinerals()) {
						conScv1 = scv;
						conScvCnt = 1;
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
					WorkerManager.Instance().moveWorkerTo(conScv2, MapGrid.GetTileFromPool(64, 64));
					break;
				}
			}
			if (conScvCnt >= 2) return;
		}

		// barracks 짓던 scv 가 4드론에 당해 죽었을 경우 어떻게 할건지 생각
		if (conScvCnt == 2 && terranInfo.scvCnt == 8) {
			if (terranInfo.barracksCnt + terranInfo.barracksConstCnt < 1) {
				conScv1.build(Terran_Barracks, MapGrid.GetTileFromPool(60, 62));
				return;
			}
			if (terranInfo.barracksCnt + terranInfo.barracksConstCnt == 1 ) {
				conScv2.build(Terran_Barracks, MapGrid.GetTileFromPool(64, 62));
				return;
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
		if (terranInfo.barracksCnt >= 1) {
			Unit barrack1 = terranInfo.list_barracks.get(0).getUnit();
			if (MyBotModule.Broodwar.self().minerals() >= 50 && barrack1.getTrainingQueue().isEmpty()) {
				barrack1.train(Terran_Marine);
			}
			if (terranInfo.barracksCnt == 2) {
				Unit barrack2 = terranInfo.list_barracks.get(1).getUnit();
				if (MyBotModule.Broodwar.self().minerals() >= 50 && barrack2.getTrainingQueue().isEmpty()) {
					barrack2.train(Terran_Marine);
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
		if (enemyBaseLocation == null) return;	// 정찰이 되지 않은 경우 이니 다음 frame 까지 기다리기 위해 return
		Position firstAssemblyArea = InformationManager.Instance().getSecondChokePoint(InformationManager.Instance().enemyPlayer).getCenter();
		Position targetAttackPos = enemyBaseLocation.getPosition();

		if (state == Common.initState) {
			int cntAssembled = 0;
			final int attack_marine_cnt = 2;
			for (UnitInfo marine : terranInfo.self_marines) {
				if (Common.getDistanceSquared(firstAssemblyArea, marine.getUnit().getPosition()) < 64*64) cntAssembled++;
				if (cntAssembled >= attack_marine_cnt) {
					state = Common.attack;
					frameAttackStarted = MyBotModule.Broodwar.getFrameCount();
					break;
				}
			}

			if (WorkerManager.Instance().isCombatWorker(conScv1)) {
				CommandUtil.MOVE(conScv1, firstAssemblyArea);
			}
			if (WorkerManager.Instance().isCombatWorker(conScv2)) {
				CommandUtil.MOVE(conScv2, firstAssemblyArea);
			}
			
			for (UnitInfo marine : terranInfo.self_marines) {
				boolean attckUnitFlag = false;
				for (Unit enemyUnit : MyBotModule.Broodwar.enemy().getUnits()) {
					// Enemy 종족별 처리가 다르게
					if (CommandUtil.IS_VALID_UNIT(enemyUnit)) {
						// bunker 인 경우
						// scv 가 공격중인 경우
						// marine 인 경우
						if (enemyUnit.getType().equals(Terran_Marine) || enemyUnit.getType().equals(Terran_SCV)) {
							if (marine.getUnit().getDistance(enemyUnit) <= CommandUtil.GET_ATTACK_RANGE(marine.getUnit(), enemyUnit) / 2) {	// 2 로 하면 아래 else 만 탐
								//CommandUtil.MOVE_BACK_CON(marine, enemyUnit);
								CommandUtil.MOVE_BACK(marine.getUnit(), enemyUnit);
								attckUnitFlag = true;
							} else { 
								if (marine.getUnit().getDistance(enemyUnit) <= CommandUtil.GET_ATTACK_RANGE(marine.getUnit(), enemyUnit)) {
									CommandUtil.ATTACK_UNIT(marine.getUnit(), enemyUnit);
									attckUnitFlag = true;
								}
							}
						}
					}
					
					if (attckUnitFlag) break;
				}
				if (!attckUnitFlag) CommandUtil.ATTACK_MOVE(marine.getUnit(), firstAssemblyArea);
			}
		}

		if (state == Common.attack) {
			if ((MyBotModule.Broodwar.getFrameCount() - frameAttackStarted) % 15 != 0) return;

			if (WorkerManager.Instance().isCombatWorker(conScv1)) { // TODO : PRIORITY LOW - 주위에 마린이 있는지 한번 확인해 보는 것도 좋을듯...
				CommandUtil.ATTACK_MOVE(conScv1, targetAttackPos);
			}
			if (WorkerManager.Instance().isCombatWorker(conScv2)) {
				CommandUtil.ATTACK_MOVE(conScv2, targetAttackPos);
			}

			for (UnitInfo marine : terranInfo.self_marines) {
				Unit targetScv = null, targetMarine = null, targetVulture = null, targetBunker = null;
				int targetScvDist = -1, targetMarineDist = -1, targetVultureDist = -1, targetBunkerDist = -1;
				for (Unit enemyUnit : MyBotModule.Broodwar.enemy().getUnits()) {
					// Enemy 종족별 처리가 다르게
					if (CommandUtil.IS_VALID_UNIT(enemyUnit)) {
						// bunker 인 경우
						// scv 가 공격중인 경우
						// marine 인 경우
//						if (enemyUnit.getType().equals(Terran_Marine) || enemyUnit.getType().equals(Terran_SCV)) {
////							if (marine.getUnit().getDistance(enemyUnit) <= CommandUtil.GET_ATTACK_RANGE(marine.getUnit(), enemyUnit) / 2) { // 일단 나누기 2로 해서 else 만 타게 한다.
////								//CommandUtil.MOVE_BACK_CON(marine, enemyUnit);
////								CommandUtil.MOVE_BACK(marine.getUnit(), enemyUnit);
//////								attckUnitFlag = true;
////								if (targetUnit == null || targetUnit.getType().equals(Terran_SCV)) targetUnit = enemyUnit;
////							} else { 
//								if (marine.getUnit().getDistance(enemyUnit) <= CommandUtil.GET_ATTACK_RANGE(marine.getUnit(), enemyUnit)) {
//								//if (marine.getUnit().getDistance(enemyUnit) <= 32*7) {
////									CommandUtil.ATTACK_UNIT(marine.getUnit(), enemyUnit);
//									//attckUnitFlag = true;
//									if (targetUnit == null || targetUnit.getType().equals(Terran_SCV)) targetUnit = enemyUnit;
//								}
////							}
//						}
						int curDist = marine.getUnit().getDistance(enemyUnit);
						if (enemyUnit.getType().equals(Terran_SCV)) {
							if (targetScv == null || targetScvDist > curDist) {
								targetScv = enemyUnit;
								targetScvDist = curDist;
							}
						} else if (enemyUnit.getType().equals(Terran_Marine)) {
							if (targetMarine == null || targetMarineDist > curDist) {
								targetMarine = enemyUnit;
								targetMarineDist = curDist;
							}
						} else if (enemyUnit.getType().equals(Terran_Vulture)) {
							if (targetVulture == null || targetVultureDist > curDist) {
								targetVulture = enemyUnit;
								targetVultureDist = curDist;
							}
						} else if (enemyUnit.getType().equals(Terran_Bunker)) {
							if (targetBunker == null || targetBunkerDist > curDist) {
								targetBunker = enemyUnit;
								targetBunkerDist = curDist;
							}
						}
					}
					
					//if (attckUnitFlag) break;
				}
//				if (!attckUnitFlag) CommandUtil.ATTACK_MOVE(marine.getUnit(), targetAttackPos);
				//if (targetUnit == null) CommandUtil.ATTACK_MOVE(marine.getUnit(), targetAttackPos);
				//else CommandUtil.ATTACK_UNIT(marine.getUnit(), targetUnit);
				if (targetScv != null) {
					if (targetScvDist < CommandUtil.GET_ATTACK_RANGE(marine.getUnit(), targetScv) / 4.0) {
						CommandUtil.MOVE_BACK(marine.getUnit(), targetScv);
					} else {
						if (targetMarine != null) {
							// 주위 병력을 계산해서 하는 방법도 있겠다
							// 일단은 공격
							CommandUtil.ATTACK_UNIT(marine.getUnit(), targetMarine);
						} else {
							CommandUtil.ATTACK_UNIT(marine.getUnit(), targetScv);
						}
					}
				} else {
					if (targetMarine != null) {
						// 주위 병력을 계산해서 하는 방법도 있겠다
						// 일단은 공격
						CommandUtil.ATTACK_UNIT(marine.getUnit(), targetMarine);
					} else {
						CommandUtil.ATTACK_UNIT(marine.getUnit(), targetScv);
					}
				}
			}
		}

//		if (conScvCnt == 2 && Common.IsAlive(conScv1) && Common.IsAlive(conScv2)) {
//			BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);
//			if (enemyBaseLocation == null) return;	// 정찰이 되지 않은 경우 이니 다음 frame 까지 기다리기 위해 return
//			Position firstAssemblyArea = InformationManager.Instance().getFirstChokePoint(InformationManager.Instance().enemyPlayer).getCenter();
//			Position targetAttackPos = enemyBaseLocation.getPosition();

//			int cntAssembled = 0;
//			for (UnitInfo marine : terranInfo.self_marines) {
//
//				if (Common.getDistanceSquared(firstAssemblyArea, marine.getUnit().getPosition()) < 64*64) cntAssembled++;
//				if (state == Common.attack) {
//					if (targetAttackPos != null) CommandUtil.ATTACK_MOVE(marine.getUnit(), targetAttackPos);
//				} else {
//					CommandUtil.ATTACK_MOVE(marine.getUnit(), firstAssemblyArea);
//				}
//			}
//
//			if (state == Common.initState) {
//
//				if (WorkerManager.Instance().isCombatWorker(conScv1)) {
//					CommandUtil.MOVE(conScv1, firstAssemblyArea);
//				}
//				if (WorkerManager.Instance().isCombatWorker(conScv2)) {
//					CommandUtil.MOVE(conScv2, firstAssemblyArea);
//				}
//
//				if (cntAssembled >= 2) {
//					state = Common.attack;
//				}
//			}
//		}



	}

	@Deprecated
	public void execute_old() {

//		if (MyBotModule.Broodwar.getFrameCount() % 500 == 0) {
//			__Util.println("\r\n>>> SCV");
//			showOut(UnitType.Terran_SCV);
//			
//			__Util.println(">>> Supply");
//			showOut(UnitType.Terran_Supply_Depot);
//			__Util.println(">>>");
//		}
		
//		if (MyBotModule.Broodwar.getFrameCount() > 500) return;
		
//		if (testFlag) showOut(UnitType.Terran_SCV);
		
//		if (testFlag) return;
		
		if (Common.WholeUnitCount(UnitType.Terran_SCV) < 8) {
			//__Util.println("---");
			//showOut(UnitType.Terran_SCV);
//			if (testFlag) return;
			bq.queueAsLowestPriority(UnitType.Terran_SCV);
			testint++;
			//showOut(UnitType.Terran_SCV);
			//__Util.println("---");
//			testFlag = true;
			//return;
		}
		
//		if (Common.WholeUnitCount(UnitType.Terran_Supply_Depot) < 1) {
//			bq.queueAsFixedPosition(UnitType.Terran_Supply_Depot, mapTactic.SupplyDepot1Position(), true);
//			return;
//		}
//		
//		if (Common.WholeUnitCount(UnitType.Terran_Barracks) < 1) {
//			bq.queueAsFixedPosition(UnitType.Terran_Supply_Depot, mapTactic.SupplyDepot2Position(), true);
//			return;
//		}
//		
//		if (Common.WholeUnitCount(UnitType.Terran_SCV) < 11) {
//			bq.queueAsLowestPriority(UnitType.Terran_SCV);
//			return;
//		}
		
		

	}

	@Override
	public void handleNuclearAttack(Position target) {
		// TODO Auto-generated method stub

	}

}
















