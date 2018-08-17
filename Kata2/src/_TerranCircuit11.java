import bwapi.TilePosition;

public class _TerranCircuit11 implements _TerranMapTactic {

	@Override
	public TilePosition StartingPosition() {
		return MapGrid.GetTileFromPool(7, 9);
	}

	@Override
	public TilePosition Scout1stPosition() {
		return _StrategySelector.TERRAN_CIRCUIT_7.StartingPosition();
	}

	@Override
	public TilePosition Scout2ndPosition() {
		return _StrategySelector.TERRAN_CIRCUIT_1.StartingPosition();
	}

	@Override
	public TilePosition Scout3rdPosition() {
		return _StrategySelector.TERRAN_CIRCUIT_5.StartingPosition();
	}

	@Override
	public TilePosition SupplyDepot1Position() {
		return MapGrid.GetTileFromPool(10, 12);
	}

	@Override
	public TilePosition SupplyDepot2Position() {
		return MapGrid.GetTileFromPool(11, 7);
	}

	@Override
	public TilePosition SupplyDepot3Position() {
		return MapGrid.GetTileFromPool(11, 5);
	}

	@Override
	public TilePosition Barrack1Position() {
		return MapGrid.GetTileFromPool(8, 14);
	}

}
