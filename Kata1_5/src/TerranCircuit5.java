import bwapi.TilePosition;

public class TerranCircuit5 implements MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[120][15];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[114][115];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[114][118];
	}
	
	@Override
	public TilePosition Bunker1Position() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[117][115];
	}

}
