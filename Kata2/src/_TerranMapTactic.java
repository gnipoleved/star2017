import bwapi.TilePosition;

public interface _TerranMapTactic {

	TilePosition StartingPosition();

	TilePosition Scout1stPosition();
	TilePosition Scout2ndPosition();
	TilePosition Scout3rdPosition();
	
	TilePosition SupplyDepot1Position();
	TilePosition SupplyDepot2Position();
	TilePosition SupplyDepot3Position();
	
	TilePosition Barrack1Position();
	
}
