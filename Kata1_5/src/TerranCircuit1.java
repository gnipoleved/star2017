import bwapi.TilePosition;

public class TerranCircuit1 implements MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[120][15];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[117][14];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[114][12];
	}
	
	@Override
	public TilePosition Bunker1Position() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[118][12];
	}

}
