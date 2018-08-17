import bwapi.TilePosition;

public class _TerranCircuit5 implements _TerranMapTactic {

	@Override
	public TilePosition StartingPosition() {
		return MapGrid.GetTileFromPool(117, 118);
	}

	@Override
	public TilePosition Scout1stPosition() {
		return _StrategySelector.TERRAN_CIRCUIT_1.StartingPosition();
	}

	@Override
	public TilePosition Scout2ndPosition() {
		return _StrategySelector.TERRAN_CIRCUIT_7.StartingPosition();
	}

	@Override
	public TilePosition Scout3rdPosition() {
		return _StrategySelector.TERRAN_CIRCUIT_11.StartingPosition();
	}

	@Override
	public TilePosition SupplyDepot1Position() {
		return MapGrid.GetTileFromPool(121, 113);
	}

	@Override
	public TilePosition SupplyDepot2Position() {
		return MapGrid.GetTileFromPool(114, 118);
	}

	@Override
	public TilePosition SupplyDepot3Position() {
		return MapGrid.GetTileFromPool(114, 113);
	}

	@Override
	public TilePosition Barrack1Position() {
		return MapGrid.GetTileFromPool(114, 115);
	}

}
