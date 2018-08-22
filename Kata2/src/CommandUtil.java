import java.util.List;
import java.util.Map;

import bwapi.*;

public class CommandUtil {

	private static CommandUtil commandUtil = new CommandUtil();

	public static void ATTACK_UNIT(Unit attacker, Unit target){
		commandUtil.attackUnit(attacker, target);
	}

	public static void ATTACK_MOVE(Unit attacker, final Position targetPosition)
	{
		commandUtil.attackMove(attacker, targetPosition);
	}

	public static void MOVE(Unit unit, Position targetPosition) {
		commandUtil.move(unit, targetPosition);
	}

	public static void MOVE_BACK_다시(Unit movingUnit, Unit from, List<Unit> unitsNear) {
		Unit nearestMarine = null;
		int nearestMarineDist = 1000000;
		for (Unit unit : unitsNear) {
			if (IS_VALID_UNIT(unit)) {
				int curFromDist = unit.getDistance(from);
				if (unit.getPlayer().equals(InformationManager.Instance().selfPlayer)) {
					if (unit.getType().equals(UnitType.Terran_Marine)) {
						if (nearestMarineDist > curFromDist) {
							nearestMarine = unit;
							nearestMarineDist = curFromDist;
						}
					}
				}
			}
		}
		if (nearestMarine != null) {

		}
	}

	public static void MOVE_BACK(Unit movingUnit, Unit from, List<Unit> unitsNear) {
		Position newPosition = backwardPosition(movingUnit.getPosition(), from.getPosition());
		if (MyBotModule.Broodwar.self().getRace().equals(Race.Terran)) {
			newPosition = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getPosition();
		} else {

			if (newPosition.getX() <= 0 * 32 || newPosition.getX() >= 127 * 32 || newPosition.getY() <= 0 * 32 || newPosition.getY() >= 127 * 32 || MyBotModule.Broodwar.isWalkable(newPosition.getX() / 32, newPosition.getY() / 32)) {
				//commandUtil.move(movingUnit, );
				newPosition = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getPosition();
			} else {
//			if (unitsNear != null) {
//				for(Unit unit : unitsNear) {
//					if (unit.getPosition().toTilePosition().equals(newPosition.toTilePosition())) {
//						newPosition = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getPosition();
//						break;
//					}
//				}
//			}
//			Map<Integer, UnitInfo> map = InformationManager.Instance().getUnitAndUnitInfoMap(InformationManager.Instance().selfPlayer);
//			if (map.get(movingUnit.getID()).getLastPosition().equals(movingUnit.getPosition())) {
//				newPosition = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getPosition();
//			}
			}
		}
		commandUtil.move(movingUnit, newPosition);
	}
	
	// a 에서 b 를 볼때 a 가 b 뒤로 움직임
	public static Position backwardPosition(Position a, Position b) {
		int factor = 2;
		if (MyBotModule.Broodwar.self().getRace().equals(Race.Protoss)) factor = 1;
		int psx = a.getX(), psy = a.getY();
		int pex = b.getX(), pey = b.getY();
		int vectorX = pex - psx, vectorY = pey - psy;
		return new Position(psx-vectorX/factor, psy-vectorY/factor);
	}

	public static Position backwardPosition(Position a, Position b, int factor) {
		int psx = a.getX(), psy = a.getY();
		int pex = b.getX(), pey = b.getY();
		double magnitude = Math.sqrt((psx-pex)*(psx-pex) + (psy-pey)*(psy-pey));
		double vectorX = (pex - psx)/magnitude, vectorY = (pey - psy)/magnitude;
		return new Position(psx-(int)(vectorX*factor), psy-(int)(vectorY*factor));
	}
	
	public static void MOVE_BACK_CON(UnitInfo movingUnit, Unit enemyUnit) {
		UnitControlData ucd = movingUnit.getUnitControlData();
		Position backwardPosition = backwardPosition(movingUnit.getUnit().getPosition(), enemyUnit.getPosition(), 118);
		if (ucd == null) {
			ucd = new UnitControlData();
			ucd.setMoveControlData(backwardPosition);
			movingUnit.setUnitControlData(ucd);
			commandUtil.move(movingUnit.getUnit(), ucd.targetPosition);
		} else {
			if (ucd.actionType == ActionType.MOVE) {
				if (ucd.targetPosition.equals(backwardPosition)) {
					if (movingUnit.getUnit().getDistance(ucd.targetPosition) <= 5 && ucd.actionGivenFrame - MyBotModule.Broodwar.getFrameCount() > 15){
						movingUnit.getUnit().holdPosition();
						ucd.clearControlData();
					}
				} else {
					ucd.setMoveControlData(backwardPosition);
					commandUtil.move(movingUnit.getUnit(), ucd.targetPosition);
				}
			} else {
				ucd.setMoveControlData(backwardPosition);
				commandUtil.move(movingUnit.getUnit(), ucd.targetPosition);
			}
		}
	}

	public static void ATTACK_UNIT_CON(UnitInfo attackingUnit, Unit enemyUnit) {
		UnitControlData ucd = attackingUnit.getUnitControlData();
		if (ucd == null) {
			ucd = new UnitControlData();
			ucd.setAttackUnitControlData(enemyUnit);
			attackingUnit.setUnitControlData(ucd);
			commandUtil.attackUnit(attackingUnit.getUnit(), ucd.targetUnit);
		} else {
			if (ucd.actionType == ActionType.ATTACK_UNIT) {
				if (ucd.targetUnit.equals(enemyUnit) == false) {
					ucd.setAttackUnitControlData(enemyUnit);
					commandUtil.attackUnit(attackingUnit.getUnit(), ucd.targetUnit);
				}
			} else if (ucd.actionType == ActionType.MOVE) {
				if (ucd.targetPosition.getDistance(attackingUnit.getUnit().getPosition()) <= 13 || MyBotModule.Broodwar.getFrameCount() - ucd.actionGivenFrame >= 25) {
					ucd.setAttackUnitControlData(enemyUnit);
					commandUtil.attackUnit(attackingUnit.getUnit(), ucd.targetUnit);
				}
			} else {
				ucd.setAttackUnitControlData(enemyUnit);
				commandUtil.attackUnit(attackingUnit.getUnit(), ucd.targetUnit);
			}
		}
	}


	public static void RIGHT_CLICK(Unit unit, Unit target){
		commandUtil.rightClick(unit, target);
	}

	public static void REPAIR(Unit unit, Unit target) {
		commandUtil.repair(unit, target);
	}

	public static boolean IS_COMBAT_UNIT(Unit unit) {
		return commandUtil.IsCombatUnit(unit);
	}

	public static boolean IS_VALID_UNIT(Unit unit) {
		return commandUtil.IsValidUnit(unit);
	}

	public static boolean CAN_ATTACK(Unit attacker, Unit target)
	{
		return commandUtil.CanAttack(attacker, target);
	}

	public static boolean CAN_ATTACK_AIR(Unit unit)
	{
		return commandUtil.CanAttackAir(unit);
	}

	public static boolean CAN_ATTACK_GROUND(Unit unit)
	{
		return commandUtil.CanAttackGround(unit);
	}

	public static int GET_ALL_UNIT_COUNT(UnitType type) {
		return commandUtil.GetAllUnitCount(type);
	}

	public static WeaponType GET_WEAPON(Unit attacker, Unit target) {
		return commandUtil.GetWeapon(attacker, target);
	}

	public static WeaponType GET_WEAPON(UnitType attacker, UnitType target) {
		return commandUtil.GetWeapon(attacker, target);
	}

	public static int GET_ATTACK_RANGE(Unit attacker, Unit target) {
		return commandUtil.GetAttackRange(attacker, target);
	}

	public static int GET_ATTACK_RANGE(UnitType attacker, UnitType target) {
		return commandUtil.GetAttackRange(attacker, target);
	}




	public void attackUnit(Unit attacker, Unit target)
	{
		if (attacker == null || target == null)
		{
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (attacker.getLastCommandFrame() >= MyBotModule.Broodwar.getFrameCount() || attacker.isAttackFrame())
		{
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = attacker.getLastCommand();

		// if we've already told this unit to attack this target, ignore this command
		if (currentCommand.getUnitCommandType() == UnitCommandType.Attack_Unit &&	currentCommand.getTarget() == target)
		{
			return;
		}

		// if nothing prevents it, attack the target
		attacker.attack(target);
	}

	public void attackMove(Unit attacker, final Position targetPosition)
	{
		// Position 객체에 대해서는 == 가 아니라 equals() 로 비교해야 합니다		
		if (attacker == null || !targetPosition.isValid())
		{
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (attacker.getLastCommandFrame() >= MyBotModule.Broodwar.getFrameCount() || attacker.isAttackFrame())
		{
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = attacker.getLastCommand();

		// if we've already told this unit to attack this target, ignore this command
		if (currentCommand.getUnitCommandType() == UnitCommandType.Attack_Move &&	currentCommand.getTargetPosition().equals(targetPosition))
		{
			return;
		}

		// if nothing prevents it, attack the target
		attacker.attack(targetPosition);
	}

	public void move(Unit attacker, final Position targetPosition)
	{
		if (attacker == null || !targetPosition.isValid())
		{
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (attacker.getLastCommandFrame() >= MyBotModule.Broodwar.getFrameCount() || attacker.isAttackFrame())
		{
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = attacker.getLastCommand();

		// if we've already told this unit to move to this position, ignore this command
		if ((currentCommand.getUnitCommandType() == UnitCommandType.Move) && (currentCommand.getTargetPosition().equals(targetPosition)) && attacker.isMoving())
		{
			return;
		}

		// if nothing prevents it, attack the target
		attacker.move(targetPosition);
	}

	public void rightClick(Unit unit, Unit target)
	{
		if (unit == null || target == null)
		{
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (unit.getLastCommandFrame() >= MyBotModule.Broodwar.getFrameCount() || unit.isAttackFrame())
		{
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = unit.getLastCommand();

		// if we've already told this unit to move to this position, ignore this command
		if ((currentCommand.getUnitCommandType() == UnitCommandType.Right_Click_Unit) && (target.getPosition().equals(currentCommand.getTargetPosition())))
		{
			return;
		}

		// if nothing prevents it, attack the target
		unit.rightClick(target);
	}

	public void repair(Unit unit, Unit target)
	{
		if (unit == null || target == null)
		{
			return;
		}

		// if we have issued a command to this unit already this frame, ignore this one
		if (unit.getLastCommandFrame() >= MyBotModule.Broodwar.getFrameCount() || unit.isAttackFrame())
		{
			return;
		}

		// get the unit's current command
		UnitCommand currentCommand = unit.getLastCommand();

		// if we've already told this unit to move to this position, ignore this command
		if ((currentCommand.getUnitCommandType() == UnitCommandType.Repair) && (currentCommand.getTarget() == target))
		{
			return;
		}

		// if nothing prevents it, attack the target
		unit.repair(target);
	}

	public boolean IsCombatUnit(Unit unit)
	{
		if (unit == null)
		{
			return false;
		}

		// no workers or buildings allowed
		if (unit != null && unit.getType().isWorker() || unit.getType().isBuilding())
		{
			return false;
		}

		// check for various types of combat units
		if (unit.getType().canAttack() ||
			unit.getType() == UnitType.Terran_Medic ||
			unit.getType() == UnitType.Protoss_High_Templar ||
			unit.getType() == UnitType.Protoss_Observer ||
			unit.isFlying() && unit.getType().spaceProvided() > 0)
		{
			return true;
		}

		return false;
	}

	public boolean IsValidUnit(Unit unit)
	{
		if (unit == null)
		{
			return false;
		}

		if (unit.isCompleted()
			&& unit.getHitPoints() > 0
			&& unit.exists()
			&& unit.getType() != UnitType.Unknown
			&& unit.getPosition().isValid())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// 미사용
//	public double GetDistanceBetweenTwoRectangles(Rect rect1, Rect rect2)
//	{
//		Rect & mostLeft = rect1.x < rect2.x ? rect1 : rect2;
//		Rect & mostRight = rect2.x < rect1.x ? rect1 : rect2;
//		Rect & upper = rect1.y < rect2.y ? rect1 : rect2;
//		Rect & lower = rect2.y < rect1.y ? rect1 : rect2;
//
//		int diffX = std::max(0, mostLeft.x == mostRight.x ? 0 : mostRight.x - (mostLeft.x + mostLeft.width));
//		int diffY = std::max(0, upper.y == lower.y ? 0 : lower.y - (upper.y + upper.height));
//
//		return std::sqrtf(static_cast<float>(diffX*diffX + diffY*diffY));
//	}

	public boolean CanAttack(Unit attacker, Unit target)
	{
		return GetWeapon(attacker, target) != WeaponType.None;
	}

	public boolean CanAttackAir(Unit unit)
	{
		return unit.getType().airWeapon() != WeaponType.None;
	}

	public boolean CanAttackGround(Unit unit)
	{
		return unit.getType().groundWeapon() != WeaponType.None;
	}

	public double CalculateLTD(Unit attacker, Unit target)
	{
		WeaponType weapon = GetWeapon(attacker, target);

		if (weapon == WeaponType.None)
		{
			return 0;
		}

		return 0; // C++ : static_cast<double>(weapon.damageAmount()) / weapon.damageCooldown();
	}

	public WeaponType GetWeapon(Unit attacker, Unit target)
	{
		return target.isFlying() ? attacker.getType().airWeapon() : attacker.getType().groundWeapon();
	}

	public WeaponType GetWeapon(UnitType attacker, UnitType target)
	{
		return target.isFlyer() ? attacker.airWeapon() : attacker.groundWeapon();
	}

	public int GetAttackRange(Unit attacker, Unit target)
	{
		WeaponType weapon = GetWeapon(attacker, target);

		if (weapon == WeaponType.None)
		{
			return 0;
		}

		int range = weapon.maxRange();

		if ((attacker.getType() == UnitType.Protoss_Dragoon)
			&& (attacker.getPlayer() == MyBotModule.Broodwar.self())
			&& MyBotModule.Broodwar.self().getUpgradeLevel(UpgradeType.Singularity_Charge) > 0)
		{
			range = 6 * 32;
		}

		return range;
	}

	public int GetAttackRange(UnitType attacker, UnitType target)
	{
		WeaponType weapon = GetWeapon(attacker, target);

		if (weapon == WeaponType.None)
		{
			return 0;
		}

		return weapon.maxRange();
	}

	public int GetAllUnitCount(UnitType type)
	{
		int count = 0;
		for (final Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			// trivial case: unit which exists matches the type
			if (unit.getType() == type)
			{
				count++;
			}

			// case where a zerg egg contains the unit type
			if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == type)
			{
				count += type.isTwoUnitsInOneEgg() ? 2 : 1;
			}

			// case where a building has started constructing a unit but it doesn't yet have a unit associated with it
			if (unit.getRemainingTrainTime() > 0)
			{
				UnitType trainType = unit.getLastCommand().getUnit().getType();

				if (trainType == type && unit.getRemainingTrainTime() == trainType.buildTime())
				{
					count++;
				}
			}
		}

		return count;
	}

	// 전체 순차탐색을 하기 때문에 느리다
	public Unit GetClosestUnitTypeToTarget(UnitType type, Position target)
	{
		Unit closestUnit = null;
		double closestDist = 100000000;

		for (Unit unit : MyBotModule.Broodwar.self().getUnits())
		{
			if (unit.getType() == type)
			{
				double dist = unit.getDistance(target);
				if (closestUnit == null || dist < closestDist)
				{
					closestUnit = unit;
					closestDist = dist;
				}
			}
		}

		return closestUnit;
	}

	
}