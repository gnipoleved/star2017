

import bwapi.Race;
import bwapi.TilePosition;

//Instance Management 기능을 같이 한다.
public class StrategySelector {
	
	private static Strategy strategyInstance;
	
	private static TerranStrategy terranStrategy;
	
	private static boolean isHere(int tx, int ty) {
		int offset = 10;
		TilePosition startLocation = Common.SELF.getStartLocation();
		return (tx - offset <= startLocation.getX() && startLocation.getX() <= tx + offset && ty - offset <= startLocation.getY() && startLocation.getY() <= ty + offset);
	}

	public static Strategy select() {
//		return getSampleStrategy();
		//return getTerranBasicStrategy();
		
		if (strategyInstance == null) {
		
			if (Common.SELF.getRace() == Race.Terran) strategyInstance = getTerranBasicStrategyInstance();
			else strategyInstance = null;
			
		}
		
		return strategyInstance;
		
		
	}

	public static TerranStrategy getTerranBasicStrategy() {
		return terranStrategy;
	}
	
	private static TerranStrategy getTerranBasicStrategyInstance() {
		
		if (terranStrategy == null) {
			//terranBasicStrategy = _TerranBasicStrategy.getInstance();
			terranStrategy = new TerranStrategy();
			
			MapStrategy mapStrategy = null;
			//(8)Hunters_KeSPA1.3.scx, Fighting_Spirit_1.3.scx, Lost_Temple_2.4_iCCup.scx
			
			if (Common.Broodwar.mapFileName().toUpperCase().indexOf("CIRCUIT") >= 0) {
				
				if (isHere(117, 7)) {	// 1
					mapStrategy = new TerranCircuit1();
				} else if (isHere(117, 117)) { //5
					mapStrategy = new TerranCircuit5();
				} else if (isHere(7, 116)) { //7
					mapStrategy = new TerranCircuit7();
				} else {	// 11
					mapStrategy = new TerranCircuit11();
				}
				
			} else if (Common.Broodwar.mapFileName().toUpperCase().indexOf("LOST") >= 0) {
				
				if (isHere(117, 27)) {	// 2시
					mapStrategy = new TerranLostTemple2();
				} else if (isHere(27, 118)) { // 7시
					mapStrategy = new TerranLostTemple7();
				} else if (isHere(7, 87)) {	// 8시
					mapStrategy = new TerranLostTemple8();
				} else { // 나머진 당연히 12시겠지
					mapStrategy = new TerranLostTemple12();
				}
			} else if (Common.Broodwar.mapFileName().toUpperCase().indexOf("FIGHT") >= 0) {
				
				if (isHere(117, 7)) {	// 1
					mapStrategy = new TerranFightingSpirit1();
				} else if (isHere(117, 117)) { //5
					mapStrategy = new TerranFightingSpirit5();
				} else if (isHere(7, 116)) { //7
					mapStrategy = new TerranFightingSpirit7();
				} else {	// 11
					mapStrategy = new TerranFightingSpirit11();
				}
				
			} else if (Common.Broodwar.mapFileName().toUpperCase().indexOf("HUNTER") >= 0) {
				
				if (isHere(113, 8)) {	// 1
					mapStrategy = new TerranHunters1();
				} else if (isHere(114, 80)) { //4
					mapStrategy = new TerranHunters4();
				} else if (isHere(114, 116)) { //5
					mapStrategy = new TerranHunters5();
				} else if (isHere(63, 117)){	// 6
					mapStrategy = new TerranHunters6();
				} else if (isHere(10, 115)) { //7
					mapStrategy = new TerranHunters7();
				} else if (isHere(8, 47)) { //9
					mapStrategy = new TerranHunters9();
				} else if (isHere(10, 6)) { //11
					mapStrategy = new TerranHunters11();
				} else {	// 12
					mapStrategy = new TerranHunters12();
				}
			}
			
			terranStrategy.mapStrategy = mapStrategy;
			
		}
		return terranStrategy;
	}


}
