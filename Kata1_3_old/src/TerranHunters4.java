import bwapi.TilePosition;

public class TerranHunters4 implements _MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[102][61];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[104][63];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return null;
	}

}
