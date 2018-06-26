import bwapi.TilePosition;

public class TerranLostTemple2 implements _MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
//		return _TerranBasicStrategy.getInstance().tilesPool[113][51];
		return _StrategySelector.getTerranBasicStrategy().tilesPool[113][51];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
//		return _TerranBasicStrategy.getInstance().tilesPool[116][51];
		return _StrategySelector.getTerranBasicStrategy().tilesPool[116][51];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
//		return _TerranBasicStrategy.getInstance().tilesPool[118][53];
		return _StrategySelector.getTerranBasicStrategy().tilesPool[118][53];
	}

}
