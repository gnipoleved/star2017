import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

import java.util.ArrayList;
import java.util.List;

import static bwapi.UnitType.*;

public class _VsProtossStrategy extends _VsCommonStrategy {
	
	@Override 
	protected boolean unitControl(Position firstAssemblyArea, Position targetAttackPos, int range) {
		
		//if ((MyBotModule.Broodwar.getFrameCount() - frameAttackStarted) % 2 != 0) return false;

		List<UnitInfo> targetMarines = new ArrayList<>();


		for (UnitInfo zealot : terranInfo.enemy_listZealot) {
			if (CommandUtil.IS_VALID_UNIT(zealot.getUnit())) {
				UnitInfo closestMarine = null, closestScv = null, closestVulture = null;
				int closestMarineDist = 1000000, closestScvDist = 1000000, closestVultureDist = 1000000;
//				__Util.println("------------ marine count : " + terranInfo.self_marines.size());
				for (UnitInfo marine : terranInfo.self_marines) {
					int curMarineDist = marine.getUnit().getDistance(zealot.getUnit());
//					__Util.println("current Marine distance (" + marine.getUnitID() + ")Marine - (" + zealot.getUnitID() + ")Zealot >> " + closestMarineDist + " --> " +curMarineDist);
					if (closestMarineDist > curMarineDist && curMarineDist < range) {
//						__Util.println("     --closest marine pickedup > (" + marine.getUnitID() + ")Marine - (" + zealot.getUnitID() + ")Zealot >> " + closestMarineDist + " --> " +curMarineDist);
						closestMarine = marine;
						closestMarineDist = curMarineDist;
					}
				}

				//if (closestMarine != null && !targetMarines.contains(closestMarine)) {
				if (closestMarine != null) {
					if (!targetMarines.contains(closestMarine)) targetMarines.add(closestMarine);
//					MyBotModule.Broodwar.drawTextScreen(closestMarine.getLastPosition(), "");
//					MyBotModule.Broodwar.drawTextScreen(closestMarine.getUnit().getPosition(), "V");
					//if (closestMarineDist < CommandUtil.GET_ATTACK_RANGE(Terran_Marine, Protoss_Zealot) * 1.5) {
					if (closestMarineDist < CommandUtil.GET_ATTACK_RANGE(Terran_Marine, Protoss_Zealot) * 1.2) {
						//CommandUtil.MOVE_BACK(closestMarine.getUnit(), zealot.getUnit(), null);
						CommandUtil.MOVE_BACK_CON(closestMarine, zealot.getUnit());
					}
				}
			}

		}

//		for (int index = 0; index < terranInfo.self_marines.size() + 2; ++index) {
//			UnitInfo offense = null;
//			if (index < terranInfo.self_marines.size()) {
//				//if (targetMarines.contains(terranInfo.self_marines.get(index))) continue;
//				offense = terranInfo.self_marines.get(index);
//			}
////			else if (index == terranInfo.self_marines.size()) offense = conScv1;
////			else offense = conScv2;
//			else offense = null;
//			if (offense == null) continue;
		for (UnitInfo offense : terranInfo.self_units) {

			if (!(offense.getType().equals(Terran_SCV) && (offense.getUnit().equals(conScv1) || offense.getUnit().equals(conScv2)))
					&& !offense.getType().equals(Terran_Marine) && !offense.getType().equals(Terran_Medic)) continue;	//  || !offense.getType().equals(Terran_SCV)) continue;

			Unit targetProbe = null, targetZealot = null, targetDragoon = null, targetPhoton = null;
			int targetProbeDist = 1000000, targetZealotDist = 1000000, targetDragoonDist = 1000000, targetPhotonDist = 1000000;

			Unit nearestSickMarine = null;
			int sickMarineDist = 1000000;

			if (CommandUtil.IS_VALID_UNIT(offense.getUnit()) == false) continue;

			List<Unit> unitsNear = MyBotModule.Broodwar.getUnitsInRadius(offense.getUnit().getPosition(), range);
			for (Unit unit : unitsNear) {
				if (!CommandUtil.IS_VALID_UNIT(unit)) continue;
				int curDist = offense.getUnit().getDistance(unit);
				int flexibleDist = range;
				if (unit.getPlayer().equals(InformationManager.Instance().enemyPlayer)) {
					// photon 인 경우
					// probe 가 공격중인 경우
					// zealot, dragoon 인 경우
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
				} else { // my unit 들인 경우
					if (unit.getType().equals(Terran_Marine) && unit.getHitPoints() < Terran_Marine.maxHitPoints() && curDist < flexibleDist) {
						if (sickMarineDist > curDist) {
							nearestSickMarine = unit;
							sickMarineDist = curDist;
						}
					}
				}

			}

			try {
				if (offense.getType().equals(Terran_Marine)  && targetMarines.contains(offense)) {
					if (targetZealot != null) {
						UnitControlData ucd = offense.getUnitControlData();
						if (ucd != null) {
							if (!ucd.targetUnit.equals(targetZealot)) {
								if (targetZealot.getDistance(offense.getUnit()) < 88) {
									if (offense.getUnit().getPosition().getDistance(targetAttackPos) >= 32*32 && offense.getUnit().getDistance(MapGrid.MostOurMarineExists.getCenter()) < 20*32) {
										CommandUtil.MOVE_CON(offense, MapGrid.MostOurMarineExists.getCenter());
									} else {
										CommandUtil.MOVE_BACK_CON(offense, targetZealot);
									}
								}
							}
						}
					}
					continue;
				}
			}catch (Exception e) {
//				__Util.println(e);
			}

			if (offense.getType().equals(Terran_Medic)) {
				if (nearestSickMarine != null) {
					CommandUtil.ATTACK_MOVE(offense.getUnit(), nearestSickMarine.getPosition());
				} else {
					CommandUtil.ATTACK_MOVE(offense.getUnit(), MapGrid.MostOurMarineExists.getCenter());
				}
			}

			//if (index <= terranInfo.self_marines.size()) {	// marine 인 경우
			if (offense.getType().equals(Terran_Marine)) { // marine 인 경우

				if (offense.getUnit().canResearch(TechType.Stim_Packs)) {
					if(offense.getUnit().getStimTimer() == 0) offense.getUnit().research(TechType.Stim_Packs);
				}

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
						//if (minDist < CommandUtil.GET_ATTACK_RANGE(offense, closerUnit) * .5) {
						if (minDist <= 2*32+5) {
							CommandUtil.MOVE_BACK_CON(offense, closerUnit);
						} else {
//							if (offense.getUnit().getPosition().getDistance(targetAttackPos) >= 32*32 && offense.getUnit().getDistance(MapGrid.MostOurMarineExists.getCenter()) < 20*32) {
//								CommandUtil.MOVE_CON(offense, MapGrid.MostOurMarineExists.getCenter());
//								__Util.println("most marine tile : " + MapGrid.MostOurMarineExists.getCenter()));
//							}
//							else {
								CommandUtil.ATTACK_UNIT_CON(offense, closerUnit);
//							}
						}
//						CommandUtil.ATTACK_UNIT_CON(offense, closerUnit);
					} else {
						if (targetDragoon != null) {
							CommandUtil.ATTACK_UNIT_CON(offense, targetDragoon);
						} else {
							if (state == Common.initState) CommandUtil.MOVE(offense.getUnit(), firstAssemblyArea);
							else if (state == Common.attack) CommandUtil.ATTACK_MOVE(offense.getUnit(), targetAttackPos);
						}
					}
				}
			} else if (offense.getUnit().getType().equals(Terran_SCV)){ //conScv1, conScv2 인 경우
				if (targetPhoton != null) {
					if (targetPhoton.isBeingConstructed()) {
						CommandUtil.ATTACK_UNIT(offense.getUnit(), targetPhoton);
					} else {
//						if (targetDragoon != null) {
//							controlCombatScouterScv(offense, firstAssemblyArea, targetDragoon);
//						} else {
//							if (targetZealot != null) {
//								controlCombatScouterScv(offense, firstAssemblyArea, targetZealot);
//							}
//						}
						if (state == Common.initState) CommandUtil.MOVE_BACK_CON(offense, targetPhoton);
						else CommandUtil.MOVE_BACK_CON(offense, targetPhoton);
					}
//				} else {
//					if (targetDragoon != null) {
//						controlCombatScouterScv(offense, firstAssemblyArea, targetDragoon);
//					} else {
//						if (targetZealot != null) {
//							controlCombatScouterScv(offense, firstAssemblyArea, targetZealot);
//						}
//					}
				}
			}
		}
		
		return false;
	}

}
