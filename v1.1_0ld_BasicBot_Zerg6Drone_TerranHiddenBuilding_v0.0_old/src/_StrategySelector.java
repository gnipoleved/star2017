

import bwapi.Race;

public class _StrategySelector {
	
	private static _Zerg5DroneStrategy zerg5DroneStrategy;
	private static _TerranBasicStrategy terranBasicStrategy;

	public static _Strategy select() {
//		return getSampleStrategy();
		//return getTerranBasicStrategy();
		
		if (_Common.SELF.getRace() == Race.Terran) return getTerranBasicStrategy();
		else return getSampleStrategy();
	}

	private static _TerranBasicStrategy getTerranBasicStrategy() {
		if (terranBasicStrategy == null) {
			terranBasicStrategy = new _TerranBasicStrategy();
		}
		return terranBasicStrategy;
	}

	private static _Zerg5DroneStrategy getSampleStrategy() {
		if (zerg5DroneStrategy == null) {
			zerg5DroneStrategy = new _Zerg5DroneStrategy();
		}
		return zerg5DroneStrategy;
	}

}
