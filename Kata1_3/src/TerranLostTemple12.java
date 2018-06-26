import bwapi.TilePosition;

public class TerranLostTemple12 implements MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		//return _TerranBasicStrategy.getInstance().tilesPool[81][5];
		return StrategySelector.getTerranBasicStrategy().tilesPool[81][5];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		//return _TerranBasicStrategy.getInstance().tilesPool[76][8];
		return StrategySelector.getTerranBasicStrategy().tilesPool[76][8];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		//return _TerranBasicStrategy.getInstance().tilesPool[78][6];
		return StrategySelector.getTerranBasicStrategy().tilesPool[78][6];
	}

}
