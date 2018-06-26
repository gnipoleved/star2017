import bwapi.TilePosition;

public class TerranFightingSpirit7 implements _MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[23][118];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[24][120];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return _StrategySelector.getTerranBasicStrategy().tilesPool[28][121];
	}
}
