package umojan;

import java.util.LinkedList;
import java.util.List;

import bwapi.Race;
import bwapi.UnitType;

public class UnitController {
	
	private Race race;
	
	private UnitType unitType;
	
	//private List<UnitInfo> units;
	
	public UnitController(Race race, UnitType unitType) {
		this.race = race;
		this.unitType = unitType;
		//units = new LinkedList<>();
		//units = InformationManager.Instance().getUnitData(Common.SELF).getUnitTypeListMap().get(unitType.toString());
	}
	
	

}
