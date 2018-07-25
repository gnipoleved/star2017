import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;

public interface _Strategy {

	void start();
	
	void execute();
	
	void handleNuclearAttack(Position target);
}


abstract class _TerranStrategy implements _Strategy
{
	protected _TerranMapTactic mapTactic;
	protected BuildOrderQueue bq = BuildManager.Instance().buildQueue;
	
	public void setTerranMapTactic(_TerranMapTactic mapTactic)
	{
		this.mapTactic = mapTactic;
	}
}


class _StrategySelector
{
	public static _Strategy select() {
		_TerranMapTactic mapTactic = null;
		if (MyBotModule.Broodwar.mapFileName().toUpperCase().contains("CIRCUIT")) {
			_TerranMapTactic _figure = new _TerranCircuit1();
			if (isHere(_figure.StartingPosition().getX(), _figure.StartingPosition().getY())) mapTactic = _figure;
			else {
				_figure = new _TerranCircuit5();
				if (isHere(_figure.StartingPosition().getX(), _figure.StartingPosition().getY())) mapTactic = _figure;
				else {
					_figure = new _TerranCircuit7();
					if (isHere(_figure.StartingPosition().getX(), _figure.StartingPosition().getY())) mapTactic = _figure;
					else {
						mapTactic = new _TerranCircuit11();
					}
				}
			}
		}
		
		_TerranStrategy terranStrategy = null;
		if (InformationManager.Instance().enemyRace == Race.Random) {
			terranStrategy = new _VsRandomStrategy();
		} else if (InformationManager.Instance().enemyRace == Race.Terran) {
			terranStrategy = new _VsTerranStrategy();
		} else if (InformationManager.Instance().enemyRace == Race.Protoss) {
			terranStrategy = new _VsProtossStrategy();
		} else if (InformationManager.Instance().enemyRace == Race.Zerg) {
			terranStrategy = new _VsZergStrategy();
		}
		
		
		__Util.println(" >>>> MapTactic Type : " + mapTactic.getClass().getSimpleName());
		
		terranStrategy.mapTactic = mapTactic;
		return terranStrategy;
		
	}
	
	private static boolean isHere(int tx, int ty)
	{
		int offset = 10;
		TilePosition startLocation = MyBotModule.Broodwar.self().getStartLocation();
		return (tx - offset <= startLocation.getX() && startLocation.getX() <= tx + offset && ty - offset <= startLocation.getY() && startLocation.getY() <= ty + offset);
	}
}