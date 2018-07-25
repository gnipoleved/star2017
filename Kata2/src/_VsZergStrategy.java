import bwapi.Position;
import bwapi.UnitType;

public class _VsZergStrategy extends _TerranStrategy {

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}
	
	private boolean testFlag = false;
	private int testint = 0;
	
	private void showOut(UnitType unitType)
	{
		__Util.println("count whole: " + Common.WholeUnitCount(unitType) + "\tcount completed: " + Common.CountCompleted(unitType) + "\tcount Incompleted: " + Common.CountIncompleted(unitType)
				+ "\tcount in-buildQ: " + Common.CountInBuildQ(unitType));
		__Util.println("testint : " + testint);
	}

	@Override
	public void execute() {
		
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
			__Util.println("---");
			showOut(UnitType.Terran_SCV);
//			if (testFlag) return;
			bq.queueAsLowestPriority(UnitType.Terran_SCV);
			testint++;
			showOut(UnitType.Terran_SCV);
			__Util.println("---");
//			testFlag = true;
			return;
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
