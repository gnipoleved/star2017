import bwapi.TilePosition;

public class TerranLostTemple7 implements MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
//		return _TerranBasicStrategy.getInstance().tilesPool[57][105];
		return StrategySelector.getTerranBasicStrategy().tilesPool[57][105];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
//		return _TerranBasicStrategy.getInstance().tilesPool[52][108];
		return StrategySelector.getTerranBasicStrategy().tilesPool[52][108];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
//		return _TerranBasicStrategy.getInstance().tilesPool[54][106];
		return StrategySelector.getTerranBasicStrategy().tilesPool[54][106];
	}

}
