import bwapi.TilePosition;

public class TerranFightingSpirit11 implements MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[10][26];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[4][28];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[7][26];
	}

}
