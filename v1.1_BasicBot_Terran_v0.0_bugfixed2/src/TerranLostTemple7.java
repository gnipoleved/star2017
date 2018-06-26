import bwapi.TilePosition;

public class TerranLostTemple7 implements _MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
//		return _TerranBasicStrategy.getInstance().tilesPool[57][105];
		return _StrategySelector.getTerranBasicStrategy().tilesPool[57][105];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
//		return _TerranBasicStrategy.getInstance().tilesPool[52][108];
		return _StrategySelector.getTerranBasicStrategy().tilesPool[52][108];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
//		return _TerranBasicStrategy.getInstance().tilesPool[54][106];
		return _StrategySelector.getTerranBasicStrategy().tilesPool[54][106];
	}

}
