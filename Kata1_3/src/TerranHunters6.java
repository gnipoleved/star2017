import bwapi.TilePosition;

public class TerranHunters6 implements MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[55][94];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[52][96];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return null;
	}

}
