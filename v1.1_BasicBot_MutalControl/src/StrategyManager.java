import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager {

	private static StrategyManager instance = new StrategyManager();

	private CommandUtil commandUtil = new CommandUtil();

	private boolean isFullScaleAttackStarted;
	private boolean isInitialBuildOrderFinished;
		
	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가를 위한 변수 및 메소드 선언

	/// 한 게임에 대한 기록을 저장하는 자료구조
	private class GameRecord {
		String mapName;
		String enemyName;
		String enemyRace;
		String enemyRealRace;
		String myName;
		String myRace;
		int gameFrameCount = 0;
		int myWinCount = 0;
		int myLoseCount = 0;
	}

	/// 과거 전체 게임들의 기록을 저장하는 자료구조
	ArrayList<GameRecord> gameRecordList = new ArrayList<GameRecord>();

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////

	/// static singleton 객체를 리턴합니다
	public static StrategyManager Instance() {
		return instance;
	}

	public StrategyManager() {
		isFullScaleAttackStarted = false;
		isInitialBuildOrderFinished = false;
	}

	/// 경기가 시작될 때 일회적으로 전략 초기 세팅 관련 로직을 실행합니다
	public void onStart() {
		
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		
		// 과거 게임 기록을 로딩합니다
		loadGameRecordList();

		// BasicBot 1.1 Patch End //////////////////////////////////////////////////

		setInitialBuildOrder();
	}

	///  경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {
		
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		
		// 과거 게임 기록 + 이번 게임 기록을 저장합니다
		saveGameRecordList(isWinner);
		
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////		
	}

	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다
	public void update() {
		if (BuildManager.Instance().buildQueue.isEmpty()) {
			isInitialBuildOrderFinished = true;
		}

		executeWorkerTraining();

		//executeSupplyManagement();

		//executeBasicCombatUnitTraining();
		
		//executeCombat();
		executeMutalControl();
		

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

		// 이번 게임의 로그를 남깁니다
		//saveGameLog();
		
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	}
	
	
	int frameStartHold = -1;
	int state = 0;
	TilePosition targetPos;
	Unit targetUnit;
	
	static final double offset = 32.0;
	static final int stdFrame = 60;
	
	
	public void executeMutalControl() {
		
		if (MyBotModule.Broodwar.getFrameCount() % 15 != 0) return;
			
		Player self = MyBotModule.Broodwar.self();
		Player enemy = MyBotModule.Broodwar.enemy();
		//if (enemy.getStartLocation() == null) return;
		if (InformationManager.Instance().getMainBaseLocation(enemy) == null) return;
		
		BaseLocation enemyBase = InformationManager.Instance().getMainBaseLocation(enemy);
//		
//		if (enemyBase == null) return;
		
		//TilePosition targetPos = MapInfo.Instance().getPositionBehindMinerals();
		
		//List<Unit> enemyMinerals = InformationManager.Instance().getMainBaseLocation(enemy).getMinerals();
		//TilePosition targetPos = enemyMinerals.get(enemyMinerals.size()/2)
		
		
		List<Unit> mutals = new ArrayList<>();
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit != null && unit.exists() && unit.getHitPoints() > 0) {
				if (unit.getType() == UnitType.Zerg_Mutalisk) {
					mutals.add(unit);
				}
			}
		}
		
		String targetUnitStr = " / targetUnit : null";
		if (targetUnit != null) targetUnitStr = " / targetUnit : " + targetUnit.getID() + ":" + targetUnit.getType().toString();
		System.out.println("state : " + state + " / mutal cnt : " + mutals.size() + " / targetPos : " + targetPos + targetUnitStr);
		//System.out.println("  enemy : " + enemyBase.toString() + " / self : " + self.getStartLocation());
		
		if (mutals.size() == 0) return;
		
		if (state == 0) {	// 초기 상태 : 중간에 모은다.
			targetPos = new TilePosition((enemyBase.getTilePosition().getX() + self.getStartLocation().getX()) /2, (enemyBase.getTilePosition().getY() + self.getStartLocation().getY()) /2);
			
			if (mutals.size() >= 9) {
				if (state == 0) {
					int inTargetCnt = 0;
					for (Unit mutal : mutals) {
						if (mutal == null || !mutal.exists() || mutal.getHitPoints() <= 0) continue;
						mutal.rightClick(targetPos.toPosition());
						if (mutal.getPosition().getDistance(targetPos.toPosition()) < offset) {
							inTargetCnt++;
						}
					}
					
					if (inTargetCnt >= 9) {
						state = 10;
					}
				}
			}
		} else if (state == 10) { // 상대 본진 미네랄 쪽으로 간다.
			//List<Unit> enemyMinerals = InformationManager.Instance().getMainBaseLocation(enemy).getMinerals();
			//targetPos = enemyMinerals.get(enemyMinerals.size()/2).getTilePosition();
			targetPos = MapInfo.Instance().getPositionBehindMinerals();
			int inTargetCnt = 0;
			for (Unit mutal : mutals) {
				if (mutal == null || !mutal.exists() || mutal.getHitPoints() <= 0) continue;
				mutal.rightClick(targetPos.toPosition());
				if (mutal.getPosition().getDistance(targetPos.toPosition()) < offset) {
					inTargetCnt++;
				}
			}
			if (inTargetCnt >= 9) {
				state = 20;
			}
		} else if (state == 20) { // 미네랄 중간에서 위헙적인 유닛을 잡아 낸다.
//			Unit leadMutal = null;
//			for (Unit mutal : mutals) {
//				double distFromEnemy = targetPos.toPosition().getDistance(mutal.getPosition());
//				if (leadMutal == null || leadMutal.getPosition().getDistance(targetPos.toPosition()) > distFromEnemy) {
//					leadMutal = mutal;
//				}
//			}
			
			for (Unit unit : enemy.getUnits()) {
				
				if (unit != null && unit.exists() && unit.getHitPoints() > 0) {
					if (enemy.getRace() == Race.Zerg) {
						
					} else if (enemy.getRace() == Race.Protoss) {
						
					} else if (enemy.getRace() == Race.Terran) {
						
						if (unit.getType() == UnitType.Terran_SCV && unit.isConstructing()) {
							//if (unit.isConstructing()) targetUnit = unit;
							targetUnit = unit;
							break;
						}
						
						if (unit.getType() == UnitType.Terran_Missile_Turret && unit.isAttacking()) {
							targetUnit = unit;
							break;
						}
						
						if (unit.getType() == UnitType.Terran_SCV) {
							targetUnit = unit;
							break;
						}
						
//						// 위와 같은 우선순위로 targetUnit 을 정하고 다 잡으면 아무 unit 이나 공격
//						targetUnit = unit;
//						break;
						
					}
				}
			}
			
			
			if (targetUnit != null && targetUnit.exists() && targetUnit.getHitPoints() > 0) {
				for (Unit mutal : mutals) {
					if (mutal == null || !mutal.exists() || mutal.getHitPoints() <= 0) continue;
					mutal.move(targetUnit.getPosition());
				}
				state = 30;
			}
			
			
			
		} else if (state == 30) {	//  move 하다가 hold and attack
			
//			if (mutals.get(0) != null && mutals.get(0).exists() && mutals.get(0).getHitPoints() > 0) {
//				if (mutals.get(0).getDistance(targetUnit) <= 32) {
//					for (Unit mutal : mutals) {
//						if (mutal == null || !mutal.exists() || mutal.getHitPoints() <= 0) continue;
//						
//					}
//				}
//			}
			
			
			
			if (frameStartHold < 0 || MyBotModule.Broodwar.getFrameCount() - frameStartHold < stdFrame) {
				if ((targetUnit == null || !targetUnit.exists() || targetUnit.getHitPoints() <=0) ) {
					
				} else {	
					boolean flag = true;
					for (Unit mutal : mutals) {
						if (mutal == null || !mutal.exists() || mutal.getHitPoints() <= 0) continue;
						if (mutal.getDistance(targetUnit) > 32) flag = false;
					}
					if (flag) {
						for (Unit mutal : mutals) {
							if (mutal == null || !mutal.exists() || mutal.getHitPoints() <= 0) continue;
							mutal.holdPosition();
						}
						frameStartHold = MyBotModule.Broodwar.getFrameCount();
						System.out.println(" >>> hold at " + frameStartHold);
					} else {// hold 할때가 아니면, 15프레임에 한번씩 move 한다.
						for (Unit mutal : mutals) {
							if (mutal == null || !mutal.exists() || mutal.getHitPoints() <= 0) continue;
							//mutal.move(targetUnit.getPosition());
							commandUtil.move(mutal, targetUnit.getPosition());
						}
					}
				}
				
			} else if (MyBotModule.Broodwar.getFrameCount() - frameStartHold >= stdFrame) {
				if (targetUnit == null || !targetUnit.exists() || targetUnit.getHitPoints() <=0) {
					state = 20;	// 다른 유닛을 잡아라
				} else {
					state = 40; // 뒤로 이동
				}
				
			}
		} else if (state == 40) {	// hold and attack 후 뒤로 도망
			
			if (targetUnit != null && targetUnit.exists() && targetUnit.getHitPoints() > 0) {
				
				
				
				Unit any = null;
				for (int i = 0; i < mutals.size(); i++) {
					if (mutals.get(i) != null && mutals.get(i).exists() && mutals.get(i).getHitPoints() > 0) {
						any = mutals.get(i);
						break;
					}
				}
				
				if (any == null) {
					state = 0;
				} else {
					int x1 = targetUnit.getPosition().getX();
					int y1 = targetUnit.getPosition().getY();
					int x2 = any.getPosition().getX();
					int y2 = any.getPosition().getY();
					
					double dx = x1 - x2;
					double dy = y1 - y2;
					double dz = Math.sqrt((double)(dx*dx + dy*dy));
					
					int tx = (int)(x2 - (160*dx / dz))/32;
					int ty = (int)(y2 - (160*dy / dz))/32;
					
					
					if (tx <= 0) tx = 0;
					else if (tx >= 128) tx = 128;
					
					if (ty <= 0) ty = 0;
					else if (ty >= 128) ty = 128;
					
					targetPos = new TilePosition(tx, ty);
					
					//targetPos = new TilePosition((int)tx/32, (int)ty/32);
					
					boolean flag = true;
					for (Unit mutal : mutals) {
						if (mutal == null || !mutal.exists() || mutal.getHitPoints() <= 0) continue;
						if (mutal.getDistance(targetPos.toPosition()) <= 96 || mutal.getTilePosition().getX() >= 128 || mutal.getTilePosition().getY() >= 128) flag = false;
					}
					if (flag) {
						for (Unit mutal : mutals) {
							if (mutal == null || !mutal.exists() || mutal.getHitPoints() <= 0) continue;
							mutal.rightClick(targetPos.toPosition());
						}
						state = 50; // 뒤로 이동하는 곳에 다 도달 할때 까지 이동하라.
					} else {
						state = 30;
					}
					
				}
				
				
				
				
			} else { // target unit 이 죽거나 했을때
				state = 20;
			}
		} else if (state == 50) {
		
			int inTargetCnt = 0;
			for (Unit mutal : mutals) {
				if (mutal == null || !mutal.exists() || mutal.getHitPoints() <= 0) continue;
				mutal.rightClick(targetPos.toPosition());
				if (mutal.getPosition().getDistance(targetPos.toPosition()) < offset || mutal.isIdle()) {
					inTargetCnt++;
				}
			}
			if (inTargetCnt >= 3) { // 여기서는 이정도 mutal 수만 되면 온것으로 보자..
				state = 30;
			}
			
		}
		
//		
//		
//		
//		
//		

		
		
		
	}
	
	
	
	

	public void setInitialBuildOrder() {
		
		if (MyBotModule.Broodwar.self().getRace() == Race.Terran) {
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Barracks);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Refinery);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);	// 14
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Supply_Depot);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_SCV);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Missile_Turret, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			
			
			
			
		} else if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spawning_Pool);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//12
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//16
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lair);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);			
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	
			
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	// 20
			
			
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//20
			
				
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//21
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//21
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spire);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);		
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony);	// 15
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);	//16
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Drone);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Flyer_Attacks);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Overlord);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk);
			

			/*
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
					BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery,
					BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(
					InformationManager.Instance().getBasicSupplyProviderUnitType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(),
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);

			// 가스 익스트랙터
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Extractor,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			// 성큰 콜로니
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Sunken_Colony,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			BuildManager.Instance().buildQueue
					.queueAsLowestPriority(InformationManager.Instance().getRefineryBuildingType());

			// 저글링 이동속도 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metabolic_Boost);

			// 에볼루션 챔버
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Evolution_Chamber);
			// 에볼루션 챔버 . 지상유닛 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Melee_Attacks, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Missile_Attacks, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Carapace, false);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType());
			
			// 스포어 코로니
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Creep_Colony,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spore_Colony,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			// 히드라
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk_Den);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);

			// 레어
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lair);

			// 오버로드 운반가능
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Ventral_Sacs);
			// 오버로드 시야 증가
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Antennae);
			// 오버로드 속도 증가
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Pneumatized_Carapace);

			// 히드라 이동속도 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Muscular_Augments, false);
			// 히드라 공격 사정거리 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Grooved_Spines, false);

			// 럴커
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Lurker_Aspect);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hydralisk);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Lurker);

			// 스파이어
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spire, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Scourge, true);

			// 스파이어 . 공중유닛 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Flyer_Attacks, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Zerg_Flyer_Carapace, false);

			// 퀸
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Queens_Nest);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Queen);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Spawn_Broodlings, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Ensnare, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Gamete_Meiosis, false);

			// 하이브
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hive);
			// 저글링 공격 속도 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Adrenal_Glands, false);

			// 스파이어 . 그레이트 스파이어
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Greater_Spire, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Guardian, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Mutalisk, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Devourer, true);

			// 울트라리스크
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Ultralisk_Cavern);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Ultralisk);
			// 울트라리스크 이동속도 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Anabolic_Synthesis, false);
			// 울트라리스크 방어력 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Chitinous_Plating, false);

			// 디파일러
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Defiler_Mound);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Defiler);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Consume, false);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Plague, false);
			// 디파일러 에너지 업
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Metasynaptic_Node, false);

			// 나이더스 캐널
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Nydus_Canal);

			// 참고로, Zerg_Nydus_Canal 건물로부터 Nydus Canal Exit를 만드는 방법은 다음과 같습니다
			//if (MyBotModule.Broodwar.self().completedUnitCount(UnitType.Zerg_Nydus_Canal) > 0) {
			//	for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			//		if (unit.getType() == UnitType.Zerg_Nydus_Canal) {
			//			TilePosition targetTilePosition = new TilePosition(unit.getTilePosition().getX() + 6, unit.getTilePosition().getY()); // Creep 이 있는 곳이어야 한다
			//			unit.build(UnitType.Zerg_Nydus_Canal, targetTilePosition);
			//		}
			//	}
			//}

			// 퀸 - 인페스티드 테란 : 테란 Terran_Command_Center 건물의 HitPoint가 낮을 때, 퀸을 들여보내서 Zerg_Infested_Command_Center 로 바꾸면, 그 건물에서 실행 됨
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Infested_Terran);
			*/
		}
	}

	// 일꾼 계속 추가 생산
	public void executeWorkerTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			// workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType());

			if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType() == UnitType.Zerg_Egg) {
						// Zerg_Egg 에게 morph 명령을 내리면 isMorphing = true,
						// isBeingConstructed = true, isConstructing = true 가 된다
						// Zerg_Egg 가 다른 유닛으로 바뀌면서 새로 만들어진 유닛은 잠시
						// isBeingConstructed = true, isConstructing = true 가
						// 되었다가,
						if (unit.isMorphing() && unit.getBuildType() == UnitType.Zerg_Drone) {
							workerCount++;
						}
					}
				}
			} else {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining()) {
							workerCount += unit.getTrainingQueue().size();
						}
					}
				}
			}

			if (workerCount < 30) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining() == false || unit.getLarva().size() > 0) {
							// 빌드큐에 일꾼 생산이 1개는 있도록 한다
							if (BuildManager.Instance().buildQueue
									.getItemCount(InformationManager.Instance().getWorkerType(), null) == 0) {
								// std.cout + "worker enqueue" + std.endl;
								BuildManager.Instance().buildQueue.queueAsLowestPriority(
										new MetaType(InformationManager.Instance().getWorkerType()), false);
							}
						}
					}
				}
			}
		}
	}

	// Supply DeadLock 예방 및 SupplyProvider 가 부족해질 상황 에 대한 선제적 대응으로서<br>
	// SupplyProvider를 추가 건설/생산한다
	public void executeSupplyManagement() {

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 가이드 추가 및 콘솔 출력 명령 주석 처리

		// InitialBuildOrder 진행중 혹은 그후라도 서플라이 건물이 파괴되어 데드락이 발생할 수 있는데, 이 상황에 대한 해결은 참가자께서 해주셔야 합니다.
		// 오버로드가 학살당하거나, 서플라이 건물이 집중 파괴되는 상황에 대해  무조건적으로 서플라이 빌드 추가를 실행하기 보다 먼저 전략적 대책 판단이 필요할 것입니다

		// BWAPI::Broodwar->self()->supplyUsed() > BWAPI::Broodwar->self()->supplyTotal()  인 상황이거나
		// BWAPI::Broodwar->self()->supplyUsed() + 빌드매니저 최상단 훈련 대상 유닛의 unit->getType().supplyRequired() > BWAPI::Broodwar->self()->supplyTotal() 인 경우
		// 서플라이 추가를 하지 않으면 더이상 유닛 훈련이 안되기 때문에 deadlock 상황이라고 볼 수도 있습니다.
		// 저그 종족의 경우 일꾼을 건물로 Morph 시킬 수 있기 때문에 고의적으로 이런 상황을 만들기도 하고, 
		// 전투에 의해 유닛이 많이 죽을 것으로 예상되는 상황에서는 고의적으로 서플라이 추가를 하지 않을수도 있기 때문에
		// 참가자께서 잘 판단하셔서 개발하시기 바랍니다.
		
		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}

		// 게임에서는 서플라이 값이 200까지 있지만, BWAPI 에서는 서플라이 값이 400까지 있다
		// 저글링 1마리가 게임에서는 서플라이를 0.5 차지하지만, BWAPI 에서는 서플라이를 1 차지한다
		if (MyBotModule.Broodwar.self().supplyTotal() <= 400) {

			// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
			// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
			int supplyMargin = 12;

			// currentSupplyShortage 를 계산한다
			int currentSupplyShortage = MyBotModule.Broodwar.self().supplyUsed() + supplyMargin - MyBotModule.Broodwar.self().supplyTotal();

			if (currentSupplyShortage > 0) {
				
				// 생산/건설 중인 Supply를 센다
				int onBuildingSupplyCount = 0;

				// 저그 종족인 경우, 생산중인 Zerg_Overlord (Zerg_Egg) 를 센다. Hatchery 등 건물은 세지 않는다
				if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == UnitType.Zerg_Overlord) {
							onBuildingSupplyCount += UnitType.Zerg_Overlord.supplyProvided();
						}
						// 갓태어난 Overlord 는 아직 SupplyTotal 에 반영안되어서, 추가 카운트를 해줘야함
						if (unit.getType() == UnitType.Zerg_Overlord && unit.isConstructing()) {
							onBuildingSupplyCount += UnitType.Zerg_Overlord.supplyProvided();
						}
					}
				}
				// 저그 종족이 아닌 경우, 건설중인 Protoss_Pylon, Terran_Supply_Depot 를 센다. Nexus, Command Center 등 건물은 세지 않는다
				else {
					onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(
							InformationManager.Instance().getBasicSupplyProviderUnitType(), null)
							* InformationManager.Instance().getBasicSupplyProviderUnitType().supplyProvided();
				}

				// 주석처리
				//System.out.println("currentSupplyShortage : " + currentSupplyShortage + " onBuildingSupplyCount : " + onBuildingSupplyCount);

				if (currentSupplyShortage > onBuildingSupplyCount) {
					
					// BuildQueue 최상단에 SupplyProvider 가 있지 않으면 enqueue 한다
					boolean isToEnqueue = true;
					if (!BuildManager.Instance().buildQueue.isEmpty()) {
						BuildOrderItem currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
						if (currentItem.metaType.isUnit() 
							&& currentItem.metaType.getUnitType() == InformationManager.Instance().getBasicSupplyProviderUnitType()) 
						{
							isToEnqueue = false;
						}
					}
					if (isToEnqueue) {
						// 주석처리
						//System.out.println("enqueue supply provider "
						//		+ InformationManager.Instance().getBasicSupplyProviderUnitType());
						BuildManager.Instance().buildQueue.queueAsHighestPriority(
								new MetaType(InformationManager.Instance().getBasicSupplyProviderUnitType()), true);
					}
				}
			}
		}

		// BasicBot 1.1 Patch End ////////////////////////////////////////////////		
	}

	public void executeBasicCombatUnitTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// 기본 병력 추가 훈련
		if (MyBotModule.Broodwar.self().minerals() >= 200 && MyBotModule.Broodwar.self().supplyUsed() < 390) {
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == InformationManager.Instance().getBasicCombatBuildingType()) {
					if (unit.isTraining() == false || unit.getLarva().size() > 0) {
						if (BuildManager.Instance().buildQueue
								.getItemCount(InformationManager.Instance().getBasicCombatUnitType(), null) == 0) {
							BuildManager.Instance().buildQueue.queueAsLowestPriority(
									InformationManager.Instance().getBasicCombatUnitType(),
									BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
		}
	}

	public void executeCombat() {

		// 공격 모드가 아닐 때에는 전투유닛들을 아군 진영 길목에 집결시켜서 방어
		if (isFullScaleAttackStarted == false) {
			Chokepoint firstChokePoint = BWTA.getNearestChokepoint(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition());

			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == InformationManager.Instance().getBasicCombatUnitType() && unit.isIdle()) {
					commandUtil.attackMove(unit, firstChokePoint.getCenter());
				}
			}

			// 전투 유닛이 2개 이상 생산되었고, 적군 위치가 파악되었으면 총공격 모드로 전환
			if (MyBotModule.Broodwar.self().completedUnitCount(InformationManager.Instance().getBasicCombatUnitType()) > 2) {
				if (InformationManager.Instance().enemyPlayer != null
					&& InformationManager.Instance().enemyRace != Race.Unknown  
					&& InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0) {				
					isFullScaleAttackStarted = true;
				}
			}
		}
		// 공격 모드가 되면, 모든 전투유닛들을 적군 Main BaseLocation 로 공격 가도록 합니다
		else {
			//std.cout + "enemy OccupiedBaseLocations : " + InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance()._enemy).size() + std.endl;
			
			if (InformationManager.Instance().enemyPlayer != null
					&& InformationManager.Instance().enemyRace != Race.Unknown 
					&& InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0) 
			{					
				// 공격 대상 지역 결정
				BaseLocation targetBaseLocation = null;
				double closestDistance = 100000000;

				for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer)) {
					double distance = BWTA.getGroundDistance(
						InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition(), 
						baseLocation.getTilePosition());

					if (distance < closestDistance) {
						closestDistance = distance;
						targetBaseLocation = baseLocation;
					}
				}

				if (targetBaseLocation != null) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						// 건물은 제외
						if (unit.getType().isBuilding()) {
							continue;
						}
						// 모든 일꾼은 제외
						if (unit.getType().isWorker()) {
							continue;
						}
											
						// canAttack 유닛은 attackMove Command 로 공격을 보냅니다
						if (unit.canAttack()) {
							
							if (unit.isIdle()) {
								commandUtil.attackMove(unit, targetBaseLocation.getPosition());
							}
						} 
					}
				}
			}
		}
	}
	
	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

	/// 과거 전체 게임 기록을 로딩합니다
	void loadGameRecordList() {
	
		// 과거의 게임에서 bwapi-data\write 폴더에 기록했던 파일은 대회 서버가 bwapi-data\read 폴더로 옮겨놓습니다
		// 따라서, 파일 로딩은 bwapi-data\read 폴더로부터 하시면 됩니다

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameRecordFileName = "bwapi-data\\read\\NoNameBot_GameRecord.dat";
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(gameRecordFileName));

			System.out.println("loadGameRecord from file: " + gameRecordFileName);

			String currentLine;
			StringTokenizer st;  
			GameRecord tempGameRecord;
			while ((currentLine = br.readLine()) != null) {
				
				st = new StringTokenizer(currentLine, " ");
				tempGameRecord = new GameRecord();
				if (st.hasMoreTokens()) { tempGameRecord.mapName = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.myName = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.myRace = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.myWinCount = Integer.parseInt(st.nextToken());}
				if (st.hasMoreTokens()) { tempGameRecord.myLoseCount = Integer.parseInt(st.nextToken());}
				if (st.hasMoreTokens()) { tempGameRecord.enemyName = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.enemyRace = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.enemyRealRace = st.nextToken();}
				if (st.hasMoreTokens()) { tempGameRecord.gameFrameCount = Integer.parseInt(st.nextToken());}
			
				gameRecordList.add(tempGameRecord);
			}
		} catch (FileNotFoundException e) {
			System.out.println("loadGameRecord failed. Could not open file :" + gameRecordFileName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}		
	}

	/// 과거 전체 게임 기록 + 이번 게임 기록을 저장합니다
	void saveGameRecordList(boolean isWinner) {

		// 이번 게임의 파일 저장은 bwapi-data\write 폴더에 하시면 됩니다.
		// bwapi-data\write 폴더에 저장된 파일은 대회 서버가 다음 경기 때 bwapi-data\read 폴더로 옮겨놓습니다

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameRecordFileName = "bwapi-data\\write\\NoNameBot_GameRecord.dat";

		System.out.println("saveGameRecord to file: " + gameRecordFileName);

		String mapName = MyBotModule.Broodwar.mapFileName();
		mapName = mapName.replace(' ', '_');
		String enemyName = MyBotModule.Broodwar.enemy().getName();
		enemyName = enemyName.replace(' ', '_');
		String myName = MyBotModule.Broodwar.self().getName();
		myName = myName.replace(' ', '_');

		/// 이번 게임에 대한 기록
		GameRecord thisGameRecord = new GameRecord();
		thisGameRecord.mapName = mapName;
		thisGameRecord.myName = myName;
		thisGameRecord.myRace = MyBotModule.Broodwar.self().getRace().toString();
		thisGameRecord.enemyName = enemyName;
		thisGameRecord.enemyRace = MyBotModule.Broodwar.enemy().getRace().toString();
		thisGameRecord.enemyRealRace = InformationManager.Instance().enemyRace.toString();
		thisGameRecord.gameFrameCount = MyBotModule.Broodwar.getFrameCount();
		if (isWinner) {
			thisGameRecord.myWinCount = 1;
			thisGameRecord.myLoseCount = 0;
		}
		else {
			thisGameRecord.myWinCount = 0;
			thisGameRecord.myLoseCount = 1;
		}
		// 이번 게임 기록을 전체 게임 기록에 추가
		gameRecordList.add(thisGameRecord);

		// 전체 게임 기록 write
		StringBuilder ss = new StringBuilder();
		for (GameRecord gameRecord : gameRecordList) {
			ss.append(gameRecord.mapName + " ");
			ss.append(gameRecord.myName + " ");
			ss.append(gameRecord.myRace + " ");
			ss.append(gameRecord.myWinCount + " ");
			ss.append(gameRecord.myLoseCount + " ");
			ss.append(gameRecord.enemyName + " ");
			ss.append(gameRecord.enemyRace + " ");
			ss.append(gameRecord.enemyRealRace + " ");
			ss.append(gameRecord.gameFrameCount + "\n");
		}
		
		Common.overwriteToFile(gameRecordFileName, ss.toString());
	}

	/// 이번 게임 중간에 상시적으로 로그를 저장합니다
	void saveGameLog() {
		
		// 100 프레임 (5초) 마다 1번씩 로그를 기록합니다
		// 참가팀 당 용량 제한이 있고, 타임아웃도 있기 때문에 자주 하지 않는 것이 좋습니다
		// 로그는 봇 개발 시 디버깅 용도로 사용하시는 것이 좋습니다
		if (MyBotModule.Broodwar.getFrameCount() % 100 != 0) {
			return;
		}

		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
		String gameLogFileName = "bwapi-data\\write\\NoNameBot_LastGameLog.dat";

		String mapName = MyBotModule.Broodwar.mapFileName();
		mapName = mapName.replace(' ', '_');
		String enemyName = MyBotModule.Broodwar.enemy().getName();
		enemyName = enemyName.replace(' ', '_');
		String myName = MyBotModule.Broodwar.self().getName();
		myName = myName.replace(' ', '_');

		StringBuilder ss = new StringBuilder();
		ss.append(mapName + " ");
		ss.append(myName + " ");
		ss.append(MyBotModule.Broodwar.self().getRace().toString() + " ");
		ss.append(enemyName + " ");
		ss.append(InformationManager.Instance().enemyRace.toString() + " ");
		ss.append(MyBotModule.Broodwar.getFrameCount() + " ");
		ss.append(MyBotModule.Broodwar.self().supplyUsed() + " ");
		ss.append(MyBotModule.Broodwar.self().supplyTotal() + "\n");

		Common.appendTextToFile(gameLogFileName, ss.toString());
	}

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	
}