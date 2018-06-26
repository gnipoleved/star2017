

public interface _Strategy {

	// only once at start
	void setupTerritoryInfo();
	
	// on every frame
	void init();
	void updateSelfUnitInfo(_UnitInfo ui);
	void updateEnemyUnitInfo(_UnitInfo eui);
	void build();

	// kind of getter
//	_MapStrategy getMapStrategy();

	

	

}
