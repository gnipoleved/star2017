import bwapi.Player;
import bwapi.TilePosition;

public abstract class MapInfo {

	private static MapInfo instance = null;
	public static MapInfo Instance() {
		if (instance == null) {
			
			Player enemy = MyBotModule.Broodwar.enemy();
			
			if (MyBotModule.Broodwar.mapFileName().toUpperCase().indexOf("FIGHT") >= 0) {
				if (isHere(enemy, 117, 7)) {	// 1
					instance = new MapInfoFightingSpirit1();
				} else if (isHere(enemy, 117, 117)) { //5
					instance = new MapInfoFightingSpirit5();
				} else if (isHere(enemy, 7, 116)) { //7
					instance = new MapInfoFightingSpirit7();
				} else {	// 11
					instance = new MapInfoFightingSpirit11();
				}
			}
			
		}
		
		return instance;
	}
	
	private static boolean isHere(Player player, int tx, int ty) {
		int offset = 10;
		//TilePosition startLocation = player.getStartLocation();
		TilePosition startLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy()).getTilePosition();
		return (tx - offset <= startLocation.getX() && startLocation.getX() <= tx + offset && ty - offset <= startLocation.getY() && startLocation.getY() <= ty + offset);
	}

	
	public static final TilePosition POS_125_9 = new TilePosition(125, 9);
	public static final TilePosition POS_2_9 = new TilePosition(2, 9);
	public static final TilePosition POS_2_118 = new TilePosition(2, 118);
	public static final TilePosition POS_125_118 = new TilePosition(125, 118);
	
	
	public abstract TilePosition getPositionBehindMinerals();
	
}



class MapInfoFightingSpirit1 extends MapInfo {

	@Override
	public TilePosition getPositionBehindMinerals() {
		return MapInfo.POS_125_9;
	}
	
}

class MapInfoFightingSpirit5 extends MapInfo {

	@Override
	public TilePosition getPositionBehindMinerals() {
		return MapInfo.POS_125_118;
	}
	
}

class MapInfoFightingSpirit7 extends MapInfo {

	@Override
	public TilePosition getPositionBehindMinerals() {
		return MapInfo.POS_2_118;
	}
	
}

class MapInfoFightingSpirit11 extends MapInfo {

	@Override
	public TilePosition getPositionBehindMinerals() {
		return MapInfo.POS_2_9;
	}
	
}