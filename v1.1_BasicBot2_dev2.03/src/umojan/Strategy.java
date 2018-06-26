package umojan;

public interface Strategy {

	
	void setupTerritoryInfo();

	void init();

	void updateSelfUnitInfo(UnitInfo ui);
	
	void updateEnemyUnitInfo(UnitInfo eui);
	
	void build();

	

	

}
