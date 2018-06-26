import bwapi.TilePosition;

public class TerranFightingSpirit1 implements _MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[100][7];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[102][9];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[97][5];
	}

}
