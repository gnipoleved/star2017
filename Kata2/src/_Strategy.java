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
	protected static InformationManager info = InformationManager.Instance();
	protected static _TerranInfo terranInfo = info.terranInfo;

	protected _TerranMapTactic mapTactic;
	protected BuildOrderQueue bq = BuildManager.Instance().buildQueue;

	public void setTerranMapTactic(_TerranMapTactic mapTactic)
	{
		this.mapTactic = mapTactic;
	}
}




class _StrategySelector
{
	public final static _TerranMapTactic TERRAN_CIRCUIT_1 = new _TerranCircuit1();
	public final static _TerranMapTactic TERRAN_CIRCUIT_5 = new _TerranCircuit5();
	public final static _TerranMapTactic TERRAN_CIRCUIT_7 = new _TerranCircuit7();
	public final static _TerranMapTactic TERRAN_CIRCUIT_11 = new _TerranCircuit11();

	public static _Strategy select() {
		_TerranMapTactic mapTactic = null;
		if (MyBotModule.Broodwar.mapFileName().toUpperCase().contains("CIRCUIT")) {
			_TerranMapTactic _figure = TERRAN_CIRCUIT_1;
			if (isHere(_figure.StartingPosition().getX(), _figure.StartingPosition().getY())) mapTactic = _figure;
			else {
				_figure = TERRAN_CIRCUIT_5;
				if (isHere(_figure.StartingPosition().getX(), _figure.StartingPosition().getY())) mapTactic = _figure;
				else {
					_figure = TERRAN_CIRCUIT_7;
					if (isHere(_figure.StartingPosition().getX(), _figure.StartingPosition().getY())) mapTactic = _figure;
					else {
						mapTactic = TERRAN_CIRCUIT_11;
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
			//terranStrategy = new _VsProtossStrategy();
			terranStrategy = new _VsTerranStrategy();
		} else if (InformationManager.Instance().enemyRace == Race.Zerg) {
			terranStrategy = new _VsZergStrategy();
		}
		
		
		__Util.println(" >>>> MapTactic Type : " + mapTactic.getClass().getSimpleName());
		
		//terranStrategy.mapTactic = mapTactic;
		terranStrategy.setTerranMapTactic((mapTactic));
		return terranStrategy;
		
	}
	
	private static boolean isHere(int tx, int ty)
	{
		int offset = 10;
		TilePosition startLocation = MyBotModule.Broodwar.self().getStartLocation();
		return (tx - offset <= startLocation.getX() && startLocation.getX() <= tx + offset && ty - offset <= startLocation.getY() && startLocation.getY() <= ty + offset);
	}
}