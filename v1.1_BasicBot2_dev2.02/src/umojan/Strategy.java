package umojan;

public interface Strategy {

	
	void setupTerritoryInfo();

	void init();

	void update(UnitInfo ui);
	
	void build();

	

}
