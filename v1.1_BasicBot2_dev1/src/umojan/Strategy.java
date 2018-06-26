package umojan;

import mybot.UnitInfo;

public interface Strategy {

	

	void init();

	void update(UnitInfo ui);
	
	void build();

}
