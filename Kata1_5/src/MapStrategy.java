import bwapi.TilePosition;

public interface MapStrategy {

	TilePosition getFirstSupplyDepotTilePosition();
	TilePosition getFirstBarrackstTilePosition();
	TilePosition getSecondSupplyDepotTilePosition();
	TilePosition Bunker1Position();

}
