import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;

import java.util.ArrayList;
import java.util.List;

import static bwapi.UnitType.*;

public class _VsProtossStrategy extends _VsCommonStrategy {
	
	@Override 
	protected boolean unitControl(Position firstAssemblyArea, Position targetAttackPos, int range) {
		
		if ((MyBotModule.Broodwar.getFrameCount() - frameAttackStarted) % 2 != 0) return false;

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

				if (closestMarine != null && !targetMarines.contains(closestMarine)) {
					targetMarines.add(closestMarine);
//					MyBotModule.Broodwar.drawTextScreen(closestMarine.getLastPosition(), "");
//					MyBotModule.Broodwar.drawTextScreen(closestMarine.getUnit().getPosition(), "V");
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
//			else if (index == terranInfo.self_marines.size()) offense = conScv1;
//			else offense = conScv2;
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
//						if (minDist < CommandUtil.GET_ATTACK_RANGE(offense, closerUnit) * .5) {
//							CommandUtil.MOVE_BACK_CON(offense, closerUnit, unitsNear);
//						} else {
//							CommandUtil.ATTACK_UNIT_CON(offense, closerUnit);
//						}
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
//				if (targetPhoton != null) {
//					if (targetPhoton.isBeingConstructed()) {
//						CommandUtil.ATTACK_UNIT(offense, targetPhoton);
//					} else {
//						if (targetDragoon != null) {
//							controlCombatScouterScv(offense, firstAssemblyArea, targetDragoon);
//						} else {
//							if (targetZealot != null) {
//								controlCombatScouterScv(offense, firstAssemblyArea, targetZealot);
//							}
//						}
//					}
//				} else {
//					if (targetDragoon != null) {
//						controlCombatScouterScv(offense, firstAssemblyArea, targetDragoon);
//					} else {
//						if (targetZealot != null) {
//							controlCombatScouterScv(offense, firstAssemblyArea, targetZealot);
//						}
//					}
//				}
			}
		}
		
		return false;
	}

}
