

public interface Strategy {

	// only once at start
	void setupTerritoryInfo();
	
	// on every frame
	void init();
	void updateSelfUnitInfo(UnitInfo ui);
	void updateEnemyUnitInfo(UnitInfo eui);
	void build();

	// kind of getter
//	_MapStrategy getMapStrategy();

	

	

}
