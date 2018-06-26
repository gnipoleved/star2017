

import bwapi.Race;
import bwapi.TilePosition;

//Instance Management 기능을 같이 한다.
public class _StrategySelector {
	
	private static _Zerg5DroneStrategy zerg5DroneStrategy;
	private static _TerranBasicStrategy terranBasicStrategy;
	
	private static boolean isHere(int tx, int ty) {
		int offset = 10;
		TilePosition startLocation = _Common.SELF.getStartLocation();
		return (tx - offset <= startLocation.getX() && startLocation.getX() <= tx + offset && ty - offset <= startLocation.getY() && startLocation.getY() <= ty + offset);
	}

	public static _Strategy select() {
//		return getSampleStrategy();
		//return getTerranBasicStrategy();
		
		if (_Common.SELF.getRace() == Race.Terran) return getTerranBasicStrategyInstance();
		else return getSampleStrategyInstance();
	}

	public static _TerranBasicStrategy getTerranBasicStrategy() {
		return terranBasicStrategy;
	}
	
	private static _TerranBasicStrategy getTerranBasicStrategyInstance() {
		
		if (terranBasicStrategy == null) {
			//terranBasicStrategy = _TerranBasicStrategy.getInstance();
			terranBasicStrategy = new _TerranBasicStrategy();
			
			_MapStrategy mapStrategy = null;
			//(8)Hunters_KeSPA1.3.scx, Fighting_Spirit_1.3.scx, Lost_Temple_2.4_iCCup.scx
			
			__Util.println("_Common.Broodwar.mapFileName() : " + _Common.Broodwar.mapFileName());
			
			if (_Common.Broodwar.mapFileName().toUpperCase().indexOf("LOST") >= 0) {
				
				if (isHere(117, 27)) {	// 2시
					mapStrategy = new TerranLostTemple2();
				} else if (isHere(27, 118)) { // 7시
					mapStrategy = new TerranLostTemple7();
				} else if (isHere(7, 87)) {	// 8시
					mapStrategy = new TerranLostTemple8();
				} else { // 나머진 당연히 12시겠지
					mapStrategy = new TerranLostTemple12();
				}
			}
			
			if (mapStrategy == null) __Util.println("why null?");
			
			terranBasicStrategy.mapStrategy = mapStrategy;
			
		}
		return terranBasicStrategy;
	}

	public static _Zerg5DroneStrategy getSampleStrategy() {
		return zerg5DroneStrategy;
	}
	
	private static _Zerg5DroneStrategy getSampleStrategyInstance() {
		if (zerg5DroneStrategy == null) {
			zerg5DroneStrategy = new _Zerg5DroneStrategy();
		}
		return zerg5DroneStrategy;
	}

}
