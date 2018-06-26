package umojan;

import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;

public interface UmojanBotInterface {
	
	void onStart();
	void onEnd(boolean isWinner);
	void onFrame();
	void onSendText(String text);
	void onReceiveText(Player player, String text);
	void onPlayerLeft(Player player);
	void onNukeDetect(Position target);
	void onUnitDiscover(Unit unit);
	void onUnitEvade(Unit unit);
	void onUnitShow(Unit unit);
	void onUnitHide(Unit unit);
	void onUnitCreate(Unit unit);
	void onUnitDestroy(Unit unit);
	void onUnitMorph(Unit unit);
	void onUnitRenegade(Unit unit);
	void onSaveGame(String gameName);
	void onUnitComplete(Unit unit);

}
