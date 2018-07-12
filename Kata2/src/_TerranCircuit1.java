import bwapi.TilePosition;

public class _TerranCircuit1 implements _TerranMapTactic {

	@Override
	public TilePosition StartingPosition() {
		return MapGrid.GetTileFromPool(117, 9);
	}

	@Override
	public TilePosition SupplyDepot1Position() {
		return MapGrid.GetTileFromPool(120, 15);
	}

	@Override
	public TilePosition SupplyDepot2Position() {
		return MapGrid.GetTileFromPool(114, 12);
	}

	@Override
	public TilePosition SupplyDepot3Position() {
		return MapGrid.GetTileFromPool(125, 4);
	}

	@Override
	public TilePosition Barrack1Position() {
		return MapGrid.GetTileFromPool(117, 14);
	}

}
