import bwapi.TilePosition;

public class TerranHunters11 implements _MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[29][19];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[26][21];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return null;
	}

}
