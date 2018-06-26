import bwapi.TilePosition;

public class TerranLostTemple8 implements MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
//		return _TerranBasicStrategy.getInstance().tilesPool[19][62];
		return StrategySelector.getTerranBasicStrategy().tilesPool[19][62];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
//		return _TerranBasicStrategy.getInstance().tilesPool[16][64];
		return StrategySelector.getTerranBasicStrategy().tilesPool[16][64];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return null;
	}

}
