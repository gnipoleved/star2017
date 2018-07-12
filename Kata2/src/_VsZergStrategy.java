import bwapi.Position;
import bwapi.UnitType;

public class _VsZergStrategy extends _TerranStrategy {

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute() {
		if (Common.WholeUnitCount(UnitType.Terran_Supply_Depot) < 1) {
			bq.queueAsFixedPosition(UnitType.Terran_Supply_Depot, mapTactic.SupplyDepot1Position(), true);
			return;
		}
		
		if (Common.WholeUnitCount(UnitType.Terran_Barracks) < 1) {
			bq.queueAsFixedPosition(UnitType.Terran_Supply_Depot, mapTactic.SupplyDepot2Position(), true);
			return;
		}
	}

	@Override
	public void handleNuclearAttack(Position target) {
		// TODO Auto-generated method stub

	}

}
