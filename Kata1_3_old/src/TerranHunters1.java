import bwapi.TilePosition;

public class TerranHunters1 implements _MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[96][21];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[98][23];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return null;
	}

}
