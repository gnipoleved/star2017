import bwapi.TilePosition;

public class TerranCircuit11 implements MapStrategy {

	@Override
	public TilePosition getFirstSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[10][12];
	}

	@Override
	public TilePosition getFirstBarrackstTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[8][14];
	}

	@Override
	public TilePosition getSecondSupplyDepotTilePosition() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[11][7];
	}
	
	@Override
	public TilePosition Bunker1Position() {
		return StrategySelector.getTerranBasicStrategy().tilesPool[7][6];
	}

}
