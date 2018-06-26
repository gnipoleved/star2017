

import static bwapi.UnitType.*;

import bwapi.TilePosition;

public class _TerranBasicStrategy implements _Strategy {
	
	private static BuildOrderQueue bq = BuildManager.Instance().buildQueue; 

	@Override
	public void setupTerritoryInfo() {
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsHighestPriority(Terran_Barracks, new TilePosition(39,16), true);
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_SCV);		
		bq.queueAsLowestPriority(Terran_SCV);
		bq.queueAsLowestPriority(Terran_SCV);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateSelfUnitInfo(_UnitInfo ui) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateEnemyUnitInfo(_UnitInfo eui) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void build() {
		// TODO Auto-generated method stub
		
	} 

}
