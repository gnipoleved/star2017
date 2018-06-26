package umojan;

import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import mybot.BuildManager;
import mybot.ConstructionManager;
import mybot.MyBotModule;
import mybot.ScoutManager;
import mybot.WorkerManager;

public class UmojanBot implements UmojanBotInterface {

	//private static InformationManager info = InformationManager.Instance();
	//private static MapGrid grid = MapGrid.Instance();
	
	private Strategy strategy;
	
	@Override
	public void onStart() {
		strategy = StrategySelector.select();
	}

	@Override
	public void onEnd(boolean isWinner) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFrame() {
//		// 아군 베이스 위치. 적군 베이스 위치. 각 유닛들의 상태정보 등을 Map 자료구조에 저장/업데이트
//		InformationManager.Instance().update();
//	
//		// 각 유닛의 위치를 자체 MapGrid 자료구조에 저장
//		MapGrid.Instance().update();
		
		
		strategy.init();
		
		// 만약 InformationManager 에 있는 RemoveBadUnits 가 실제로 아무일 안하거나 별 상관이 없는 경우는 아래 주석을 풀고 위를 주석 처리 해도 된다...
		MapGrid.Instance().clearGrid();
		
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			//InformationManager.Instance().updateUnitInfo(unit);
			InformationManager.Instance().updateUnitInfo(unit);
			
			if (unit.getHitPoints() > 0)
			{
				MapGrid.Instance().getCell(unit).oppUnits.add(unit);
				MapGrid.Instance().getCell(unit).timeLastOpponentSeen = MyBotModule.Broodwar.getFrameCount();
			}
			
			
		}
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			strategy.update(InformationManager.Instance().updateUnitInfo(unit));
			
			MapGrid.Instance().getCell(unit).ourUnits.add(unit);
			MapGrid.Instance().getCell(unit).timeLastVisited = MyBotModule.Broodwar.getFrameCount();
		}
		//InformationManager.Instance().removeBadUnits();
		
		if (MyBotModule.Broodwar.getFrameCount() % 120 == 0) {
			InformationManager.Instance().updateBaseLocationInfo();
		}

		// 일꾼 유닛에 대한 명령 (자원 채취, 이동 정도) 지시 및 정리
		WorkerManager.Instance().update();

		// 빌드오더큐를 관리하며, 빌드오더에 따라 실제 실행(유닛 훈련, 테크 업그레이드 등)을 지시한다.
		BuildManager.Instance().update();

		// 빌드오더 중 건물 빌드에 대해서는, 일꾼유닛 선정, 위치선정, 건설 실시, 중단된 건물 빌드 재개를 지시한다
		ConstructionManager.Instance().update();

		// 게임 초기 정찰 유닛 지정 및 정찰 유닛 컨트롤을 실행한다
		ScoutManager.Instance().update();
		
		strategy.build();

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
		InformationManager.Instance().onUnitShow(unit);
		
		// ResourceDepot 및 Worker 에 대한 처리
		//WorkerManager.Instance().onUnitShow(unit);
	}

	@Override
	public void onUnitHide(Unit unit) {
		InformationManager.Instance().onUnitHide(unit); 
	}

	@Override
	public void onUnitCreate(Unit unit) {
		InformationManager.Instance().onUnitCreate(unit);
	}

	@Override
	public void onUnitDestroy(Unit unit) {
		// ResourceDepot 및 Worker 에 대한 처리
		WorkerManager.Instance().onUnitDestroy(unit);

		InformationManager.Instance().onUnitDestroy(unit); 
	}

	@Override
	public void onUnitMorph(Unit unit) {
		InformationManager.Instance().onUnitMorph(unit);

		// Zerg 종족 Worker 의 Morph 에 대한 처리
		WorkerManager.Instance().onUnitMorph(unit);
	}

	@Override
	public void onUnitRenegade(Unit unit) {
		// Vespene_Geyser (가스 광산) 에 누군가가 건설을 했을 경우
		//MyBotModule.Broodwar.sendText("A %s [%p] has renegaded. It is now owned by %s", unit.getType().c_str(), unit, unit.getPlayer().getName().c_str());

		InformationManager.Instance().onUnitRenegade(unit);
	}

	@Override
	public void onSaveGame(String gameName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnitComplete(Unit unit) {
		InformationManager.Instance().onUnitComplete(unit);

		// ResourceDepot 및 Worker 에 대한 처리
		WorkerManager.Instance().onUnitComplete(unit);
	}

}
