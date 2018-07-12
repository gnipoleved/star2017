import bwapi.TilePosition;

public class _TerranCircuit7 implements _TerranMapTactic {

	@Override
	public TilePosition StartingPosition() {
		return MapGrid.GetTileFromPool(7, 118);
	}

	@Override
	public TilePosition SupplyDepot1Position() {
		return MapGrid.GetTileFromPool(4, 113);
	}

	@Override
	public TilePosition SupplyDepot2Position() {
		return MapGrid.GetTileFromPool(1, 115);
	}

	@Override
	public TilePosition SupplyDepot3Position() {
		return MapGrid.GetTileFromPool(1, 113);
	}

	@Override
	public TilePosition Barrack1Position() {
		return MapGrid.GetTileFromPool(11, 115);
	}
	
}