import bwapi.TilePosition;

public class TerranHunters1 implements MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[96][21];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[98][23];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return null;
	}

}
