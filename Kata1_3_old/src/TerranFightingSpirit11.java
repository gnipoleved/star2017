import bwapi.TilePosition;

public class TerranFightingSpirit11 implements _MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[10][26];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[4][28];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[7][26];
	}

}
