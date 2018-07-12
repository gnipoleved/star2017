import bwapi.Position;
import bwapi.Unit;

public class __EventLogger implements __IEventLogger {
	
	private boolean logging = true;
	
	private int maxUnitId = 0;
	private int showCnt = 0;
	private int discoverCnt = 0;
	
	private int hideCnt = 0;
	private int evadeCnt = 0;

	@Override
	public void logOnStart() {
		if (logging) {
			__Util.println("~~~~~~~ Game started.");
			__Util.println("~~~~ unit count : " + MyBotModule.Broodwar.getAllUnits().size());
		}
	}
	
	@Override
	public void logOnEnd(boolean isWinner) {
		if (logging) {
			__Util.println(" >> unit count : " + MyBotModule.Broodwar.getAllUnits().size() + " at " + MyBotModule.Broodwar.getFrameCount());
			
			__Util.println("   -> showCnt : " + showCnt);
			__Util.println("   -> discoverCnt : " + discoverCnt);
			__Util.println("   -> evadeCnt : " + evadeCnt);
			__Util.println("   -> hideCnt : " + hideCnt);
			
			__Util.println("   -> maxUnitId : " + maxUnitId);
			
			__Util.println("~~~~~ Game ended.");
		}
	}
	
	@Override
	public void logOnFrame() {
		if (logging) {
			if (MyBotModule.Broodwar.getFrameCount() % 500 == 0) {
				__Util.println(" >> unit count : " + MyBotModule.Broodwar.getAllUnits().size() + " at " + MyBotModule.Broodwar.getFrameCount());
			}
		}
	}
	
	@Override
	public void logOnUnitCreate(Unit unit) {
		if (logging) {
			updateMaxUnitId(unit.getID());
			__Util.println("~~~~~~ Unit Created : " + unit.getType() + "(" + unit.getID() + ")");
		}
	}
	
	@Override
	public void logOnUnitDestroy(Unit unit) {
		if (logging) {
			updateMaxUnitId(unit.getID());
			__Util.println("~~~~~~ Unit Destroyed : " + unit.getType() + "(" + unit.getID() + ")");
		}
	}
	
	@Override
	public void logOnUnitMorph(Unit unit) {
		if (logging) {
			updateMaxUnitId(unit.getID());
			__Util.println("~~~~~~ Unit morphing : " + unit.getType() + "(" + unit.getID() + ")"); 
		}
	}
	
	@Override
	public void logOnUnitRenegade(Unit unit) {
		if (logging) {
			updateMaxUnitId(unit.getID());
			__Util.println("~~~~~~~ Unit Renegaded : " + unit.getType() + "(" + unit.getID() + ")");
		}
	}
	
	@Override
	public void logOnUnitComplete(Unit unit) {
		if (logging) {
			updateMaxUnitId(unit.getID());
			__Util.println("~~~~~~~ Unit Completed : " + unit.getType() + "(" + unit.getID() + ")");
		}
	}
	
	@Override
	public void logOnUnitDiscover(Unit unit) {
		if (logging) {
			updateMaxUnitId(unit.getID());
			discoverCnt++;
			__Util.println("~~~~~~ Unit Discovered : " + unit.getType() + "(" + unit.getID() + ")");
		}
	}
	
	@Override
	public void logOnUnitEvade(Unit unit) {
		if (logging) {
			updateMaxUnitId(unit.getID());
			evadeCnt++;
			__Util.println("~~~~~~ Unit Evaded : " + unit.getType() + "(" + unit.getID() + ")");
		}
	}
	
	@Override
	public void logOnUnitShow(Unit unit) {
		if (logging) {
			updateMaxUnitId(unit.getID());
			showCnt++;
			__Util.println("~~~~~~ Unit Show : " + unit.getType() + "(" + unit.getID() + ")");
		}
	}
	
	@Override
	public void logOnUnitHide(Unit unit) {
		if (logging) {
			updateMaxUnitId(unit.getID());
			hideCnt++;
			__Util.println("~~~~~~ Unit Hide : " + unit.getType() + "(" + unit.getID() + ")");
		}
	}
	
	@Override
	public void logOnNukeDetect(Position target) {
		if (logging) {
			__Util.println(" !!!!! Nuclear Missile Ready !!! : " + target.toString());
		}
	}
	
	private void updateMaxUnitId(int unitId) {
		if (maxUnitId < unitId) maxUnitId = unitId;
	}
	
}


interface __IEventLogger
{
	void logOnStart();
	void logOnEnd(boolean isWinner);
	void logOnFrame();
	void logOnUnitCreate(Unit unit);
	void logOnUnitDestroy(Unit unit);
	void logOnUnitMorph(Unit unit);
	void logOnUnitRenegade(Unit unit);
	void logOnUnitComplete(Unit unit);
	void logOnUnitDiscover(Unit unit);
	void logOnUnitEvade(Unit unit);
	void logOnUnitShow(Unit unit);
	void logOnUnitHide(Unit unit);
	void logOnNukeDetect(Position target);
}