import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;

public class _LogBot implements _UmojanBotInterface {

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnd(boolean isWinner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSendText(String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveText(Player player, String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerLeft(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNukeDetect(Position target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitDiscover(Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitEvade(Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitShow(Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitHide(Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitCreate(Unit unit) {
		__Util.println(" On created. : " + unit.getType().toString() + "(" + unit.getID() + ")");		
	}

	@Override
	public void onUnitDestroy(Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitMorph(Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitRenegade(Unit unit) {
		__Util.println(" On Renegaded. : " + unit.getType().toString() + "(" + unit.getID() + ")");		
	}

	@Override
	public void onSaveGame(String gameName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnitComplete(Unit unit) {
		__Util.println(" On Completed. : " + unit.getType().toString() + "(" + unit.getID() + ")");		
	}

}
