import bwapi.Position;

public class _StrategyManager {
	
	private final static _StrategyManager instance = new _StrategyManager();
	
	
//	private static boolean isHere(int tx, int ty)
//	{
//		int offset = 10;
//		TilePosition startLocation = MyBotModule.Broodwar.self().getStartLocation();
//		return (tx - offset <= startLocation.getX() && startLocation.getX() <= tx + offset && ty - offset <= startLocation.getY() && startLocation.getY() <= ty + offset);
//	}
	
	public static _StrategyManager Instance() {
//		_TerranMapTactic mapTactic = null;
//		if (MyBotModule.Broodwar.mapFileName().toUpperCase().contains("CIRCUIT")) {
//			_TerranMapTactic _figure = new _TerranCircuit1();
//			if (isHere(_figure.StartingPosition().getX(), _figure.StartingPosition().getY())) mapTactic = _figure;
//			else {
//				_figure = new _TerranCircuit5();
//				if (isHere(_figure.StartingPosition().getX(), _figure.StartingPosition().getY())) mapTactic = _figure;
//				else {
//					_figure = new _TerranCircuit7();
//					if (isHere(_figure.StartingPosition().getX(), _figure.StartingPosition().getY())) mapTactic = _figure;
//					else {
//						mapTactic = new _TerranCircuit11();
//					}
//				}
//			}
//		}
//		
//		_TerranStrategy terranStrategy = null;
//		if (InformationManager.Instance().enemyRace == Race.Random) {
//			terranStrategy = new _VsRandomStrategy();
//		} else if (InformationManager.Instance().enemyRace == Race.Terran) {
//			terranStrategy = new _VsTerranStrategy();
//		} else if (InformationManager.Instance().enemyRace == Race.Protoss) {
//			terranStrategy = new _VsProtossStrategy();
//		} else if (InformationManager.Instance().enemyRace == Race.Zerg) {
//			terranStrategy = new _VsZergStrategy();
//		}
//		
//		terranStrategy.mapTactic = mapTactic;
//		instance.strategy = terranStrategy;
		
		instance.strategy = _StrategySelector.select();
		
		return instance;
	}
	
	
	private _Strategy strategy;
	
	public void onStart()
	{
		// TODO: 과거 게임 기록을 로딩하는 것. PRIORITY-3
		// loadGameRecordList();
		strategy.start();
	}
	
	public void onEnd(boolean isWinner)
	{
		// TODO
	}

	public void update()
	{
		// TODO
		strategy.execute();
	}
	
	public void onNukeDetect(Position target) {
		// TODO
		strategy.handleNuclearAttack(target);
	}
}
