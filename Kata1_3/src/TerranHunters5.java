import bwapi.TilePosition;

public class TerranHunters5 implements MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[93][95];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[90][97];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return null;
	}

}
