import bwapi.TilePosition;

public class TerranCircuit7 implements MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[4][113];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[11][115];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[1][115];
	}

	@Override
	public TilePosition Bunker1Position() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[7][115];
	}
}
