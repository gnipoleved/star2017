import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

import java.util.ArrayList;
import java.util.List;

import static bwapi.UnitType.*;

public class _VsProtossStrategy extends _TerranStrategy {

	protected int conScvCnt = 0;
	protected Unit conScv1, conScv2;

	protected List<Unit> defenseWorkers;

	protected Common.State state;
	protected int frameAttackStarted;

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

		int range = 20 * 32;
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

		}

		if (state == Common.initState || state == Common.attack) {

			if ((MyBotModule.Broodwar.getFrameCount() - frameAttackStarted) % 2 != 0) return;

			List<UnitInfo> targetMarines = new ArrayList<>();

			for (UnitInfo zealot : terranInfo.enemy_listZealot) {
				if (CommandUtil.IS_VALID_UNIT(zealot.getUnit())) {
					UnitInfo closestMarine = null, closestScv = null, closestVulture = null;
					int closestMarineDist = 1000000, closestScvDist = 1000000, closestVultureDist = 1000000;
					for (UnitInfo marine : terranInfo.self_marines) {
						int curMarineDist = marine.getUnit().getDistance(zealot.getUnit());
						if (closestMarineDist > curMarineDist && curMarineDist < range) {
							closestMarine = marine;
							closestMarineDist = curMarineDist;
						}
					}

					if (closestMarine != null) {
						targetMarines.add(closestMarine);
						MyBotModule.Broodwar.drawTextScreen(closestMarine.getLastPosition(), "");
						MyBotModule.Broodwar.drawTextScreen(closestMarine.getUnit().getPosition(), "V");
						if (closestMarineDist < CommandUtil.GET_ATTACK_RANGE(Terran_Marine, Protoss_Zealot) * 1.5) {
							//CommandUtil.MOVE_BACK(closestMarine.getUnit(), zealot.getUnit(), null);
							CommandUtil.MOVE_BACK_CON(closestMarine, zealot.getUnit());
						}
					}
				}

			}

			for (int index = 0; index < terranInfo.self_marines.size() + 2; ++index) {

				Unit targetProbe = null, targetZealot = null, targetDragoon = null, targetPhoton = null;
				int targetProbeDist = 1000000, targetZealotDist = 1000000, targetDragoonDist = 1000000, targetPhotonDist = 1000000;

				UnitInfo offense = null;
				if (index < terranInfo.self_marines.size()) {
					if (targetMarines.contains(terranInfo.self_marines.get(index))) continue;
					offense = terranInfo.self_marines.get(index);
				}
//				else if (index == terranInfo.self_marines.size()) offense = conScv1;
//				else offense = conScv2;
				else offense = null;
				if (offense == null) continue;


				if (CommandUtil.IS_VALID_UNIT(offense.getUnit()) == false) continue;

				List<Unit> unitsNear = MyBotModule.Broodwar.getUnitsInRadius(offense.getUnit().getPosition(), range);
				for (Unit unit : unitsNear) {
					if (CommandUtil.IS_VALID_UNIT(unit) && unit.getPlayer().equals(InformationManager.Instance().enemyPlayer)) {
						// photon 인 경우
						// probe 가 공격중인 경우
						// zealot, dragoon 인 경우
						int curDist = offense.getUnit().getDistance(unit);
						int flexibleDist = range;
						if (curDist < flexibleDist) {
							if (unit.getType().equals(Protoss_Probe)) {
								if (targetProbe == null || targetProbeDist > curDist) {
									targetProbe = unit;
									targetProbeDist = curDist;
								}
							} else if (unit.getType().equals(Protoss_Zealot)) {
								if (targetZealot == null || targetZealotDist > curDist) {
									targetZealot = unit;
									targetZealotDist = curDist;
								}
							} else if (unit.getType().equals(Protoss_Dragoon)) {
								if (targetDragoon == null || targetDragoonDist > curDist) {
									targetDragoon = unit;
									targetDragoonDist = curDist;
								}
							} else if (unit.getType().equals(Protoss_Photon_Cannon)) {
								if (targetPhoton == null || targetPhotonDist > curDist) {
									targetPhoton = unit;
									targetPhotonDist = curDist;
								}
							}
						}
					}
				}

				//if (index <= terranInfo.self_marines.size()) {	// marine 인 경우
				if (offense.getType().equals(Terran_Marine)) { // marine 인 경우
					if (targetPhoton != null) {
						CommandUtil.ATTACK_UNIT_CON(offense, targetPhoton);
					} else {
						if (targetZealot != null || targetProbe != null) {
							Unit closerUnit = null;
							int minDist = -1;
							if (targetZealotDist <= targetProbeDist) {
								closerUnit = targetZealot;
								minDist = targetZealotDist;
							} else {
								closerUnit = targetProbe;
								minDist = targetProbeDist;
							}
//							if (minDist < CommandUtil.GET_ATTACK_RANGE(offense, closerUnit) * .5) {
//								CommandUtil.MOVE_BACK_CON(offense, closerUnit, unitsNear);
//							} else {
//								CommandUtil.ATTACK_UNIT_CON(offense, closerUnit);
//							}
							CommandUtil.ATTACK_UNIT_CON(offense, closerUnit);
						} else {
							if (targetDragoon != null) {
								CommandUtil.ATTACK_UNIT_CON(offense, targetDragoon);
							} else {
								if (state == Common.initState) CommandUtil.MOVE(offense.getUnit(), firstAssemblyArea);
								else if (state == Common.attack) CommandUtil.ATTACK_MOVE(offense.getUnit(), targetAttackPos);
							}
						}
					}
				} else { //conScv1, conScv2 인 경우
//					if (targetPhoton != null) {
//						if (targetPhoton.isBeingConstructed()) {
//							CommandUtil.ATTACK_UNIT(offense, targetPhoton);
//						} else {
//							if (targetDragoon != null) {
//								controlCombatScouterScv(offense, firstAssemblyArea, targetDragoon);
//							} else {
//								if (targetZealot != null) {
//									controlCombatScouterScv(offense, firstAssemblyArea, targetZealot);
//								}
//							}
//						}
//					} else {
//						if (targetDragoon != null) {
//							controlCombatScouterScv(offense, firstAssemblyArea, targetDragoon);
//						} else {
//							if (targetZealot != null) {
//								controlCombatScouterScv(offense, firstAssemblyArea, targetZealot);
//							}
//						}
//					}
				}
			}



		} else {

			try {

				if ((MyBotModule.Broodwar.getFrameCount() - frameAttackStarted) % 6 != 0) return;

				//for (UnitInfo marine : terranInfo.self_marines) {
				for (int index = 0; index < terranInfo.self_marines.size() + 2; ++index) {

					Unit targetProbe = null, targetZealot = null, targetDragoon = null, targetPhoton = null;
					int targetProbeDist = 1000000, targetZealotDist = 1000000, targetDragoonDist = 1000000, targetPhotonDist = 1000000;

					Unit offense = null;
					if (index < terranInfo.self_marines.size()) offense = terranInfo.self_marines.get(index).getUnit();
					else if (index == terranInfo.self_marines.size()) offense = conScv1;
					else offense = conScv2;

					if (CommandUtil.IS_VALID_UNIT(offense)==false) continue;

					List<Unit> unitsNear = MyBotModule.Broodwar.getUnitsInRadius(offense.getPosition(), 7*32);
					for (Unit unit : unitsNear) {
						if (CommandUtil.IS_VALID_UNIT(unit) && unit.getPlayer().equals(InformationManager.Instance().enemyPlayer)) {
							// photon 인 경우
							// probe 가 공격중인 경우
							// zealot, dragoon 인 경우
							int curDist = offense.getDistance(unit);
							int flexibleDist = 7*32;
							if (curDist < flexibleDist) {
								if (unit.getType().equals(Protoss_Probe)) {
									if (targetProbe == null || targetProbeDist > curDist) {
										targetProbe = unit;
										targetProbeDist = curDist;
									}
								} else if (unit.getType().equals(Protoss_Zealot)) {
									if (targetZealot == null || targetZealotDist > curDist) {
										targetZealot = unit;
										targetZealotDist = curDist;
									}
								} else if (unit.getType().equals(Protoss_Dragoon)) {
									if (targetDragoon == null || targetDragoonDist > curDist) {
										targetDragoon = unit;
										targetDragoonDist = curDist;
									}
								} else if (unit.getType().equals(Protoss_Photon_Cannon)) {
									if (targetPhoton == null || targetPhotonDist > curDist) {
										targetPhoton = unit;
										targetPhotonDist = curDist;
									}
								}
							}
						}
					}

					//if (index <= terranInfo.self_marines.size()) {	// marine 인 경우
					if (offense.getType().equals(Terran_Marine)) { // marine 인 경우
						if (targetPhoton != null) {
							CommandUtil.ATTACK_UNIT(offense, targetPhoton);
						} else {
							if (targetZealot != null || targetProbe != null) {
								Unit closerUnit = null; int minDist = -1;
								if (targetZealotDist <= targetProbeDist) {closerUnit = targetZealot; minDist = targetZealotDist;}
								else {closerUnit = targetProbe; minDist = targetProbeDist;}
								if (minDist < CommandUtil.GET_ATTACK_RANGE(offense, closerUnit) *.87) {
									CommandUtil.MOVE_BACK(offense, closerUnit, unitsNear);
								} else {
									CommandUtil.ATTACK_UNIT(offense, closerUnit);
								}
							} else {
								if (targetDragoon != null) {
									CommandUtil.ATTACK_UNIT(offense, targetDragoon);
								} else {
									if (state == Common.initState) CommandUtil.MOVE(offense, firstAssemblyArea);
									else if (state == Common.attack) CommandUtil.ATTACK_MOVE(offense, targetAttackPos);
								}
							}
						}
					} else { //conScv1, conScv2 인 경우
						if (targetPhoton != null) {
							if (targetPhoton.isBeingConstructed()) {
								CommandUtil.ATTACK_UNIT(offense, targetPhoton);
							} else {
								if (targetDragoon != null) {
									controlCombatScouterScv(offense, firstAssemblyArea, targetDragoon);
								} else {
									if (targetZealot != null) {
										controlCombatScouterScv(offense, firstAssemblyArea, targetZealot);
									}
								}
							}
						} else {
							if (targetDragoon != null) {
								controlCombatScouterScv(offense, firstAssemblyArea, targetDragoon);
							} else {
								if (targetZealot != null) {
									controlCombatScouterScv(offense, firstAssemblyArea, targetZealot);
								}
							}
						}



//							if (targetMarine != null) {
//								if (terranInfo.enemy_attackedCntBunkerConstScv == 0) CommandUtil.ATTACK_UNIT(offense, targetScvConstBunker);
//								else {
//									controlCombatScouterScv(offense, firstAssemblyArea, targetMarine);
//								}
//							} else {
//								CommandUtil.ATTACK_UNIT(offense, targetScvConstBunker);
//							}
//						} else {
//							if (targetMarine != null) {
//								controlCombatScouterScv(offense, firstAssemblyArea, targetMarine);
//							} else {
//								// 정찰 좀 하다가 가스, FACTORY, BARRACK 순서대로 점검 흐 SCV 한마리 건드려 보는 로직 가능하면 구현
//								if (state == Common.initState) CommandUtil.MOVE(offense, firstAssemblyArea);
//								else if (state == Common.attack) CommandUtil.ATTACK_MOVE(offense, targetAttackPos);
//							}
//						}
					}
				}
			} catch (Exception ex) {
				__Util.println(ex);
			}

		}


	}


	private void controlCombatScouterScv(Unit scv, Position firstAssemblyArea, Unit targetUnit) {
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




	@Override
	public void handleNuclearAttack(Position target) {
		// TODO Auto-generated method stub

	}

}
