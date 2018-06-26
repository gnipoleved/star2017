import bwapi.TilePosition;

public class TerranFightingSpirit5 implements _MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[118][98];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[114][101];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[118][100];
	}

}
