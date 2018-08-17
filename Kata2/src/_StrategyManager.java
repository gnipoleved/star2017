import bwapi.Position;
import bwapi.Unit;

public class _StrategyManager {
	
	private final static _StrategyManager instance = new _StrategyManager();
	
	public static _StrategyManager Instance() {
		return instance;
	}
	
	
	private _Strategy strategy;
	
	public void onStart()
	{
		// TODO: 과거 게임 기록을 로딩하는 것. PRIORITY-3
		// loadGameRecordList();
		instance.strategy = _StrategySelector.select();
		
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
