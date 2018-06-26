import static bwapi.UnitType.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
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

	private static final int defradius = (8*Config.TILE_SIZE)*(8*Config.TILE_SIZE);
	private static final int defradius2 = (2*Config.TILE_SIZE)*(2*Config.TILE_SIZE);
	public static final int scanCoolTime = TerranStrategy.scanCoolTime;

	///////////////////////////////////////////////////////////////////
	/// 
	/// 제목 : Kata1
	///
	/// 목표 : 컴퓨터 정도는 이겨보자
	///
	/// 아이디어 : 방어를 튼튼히 갖추고 기다리고 있다가, 적군 유닛들이 쳐들어온 것을 방어 성공해낸 후 역습을 가서 승리한다
	///
	/// 제공 :
	///         1. 방어형 빌드오더
	///
	///         2. 일꾼 훈련, 방어 건물 건설, 업그레이드, 리서치, 공격 유닛 훈련, 아군 공격유닛 방어형 배치, 적 Eliminate 시키기 메소드 
	///
	/// 참가자 구현 과제 :
	///
	///         TODO 1. 적 기지로 공격을 갈 타이밍인지 판단하는 로직                  (예상 개발시간  5분)
	/// 
	///         TODO 2. 적 기지로 아군 공격 유닛들이 공격가게 하는 로직               (예상 개발시간 20분)
	///
	///         TODO 3. 적 Eliminate 시키기 모드로 전환할 때인지 판단하는 로직   (예상 개발시간  5분)
	///
	/// 성공 조건 : 컴퓨터와 1대1로 싸워 승리한다
	///
	/// 도전 과제 :
	///           "더 빠른 시간 내에" 승리를 달성하도록 노력해보세요.
	///
	///           방어 시 아군 공격유닛들의 피해를 최소화하고 
	///
	///           역습 타이밍을 가능한한 이른 시간으로 결정하고
	///
	///           아군 공격유닛들을 이동 시키되 가능한한 떼지어서 빠르게 이동시키고  
	///
	///           적군의 남은 건물들을 가능한한 빠른 시간내에 찾아 Eliminate시키면,
	///
	///           "더 빠른 시간 내에" 승리를 달성할 수 있을 것입니다.
	/// 
	///////////////////////////////////////////////////////////////////
	
	static TerranStrategy terran = StrategySelector.getTerranBasicStrategy();
	
	boolean doorOpened = false;
	boolean firstExpan = false;
	
	// 아군
	Player myPlayer;
	Race myRace;
	
	// 적군
	Player enemyPlayer;
	Race enemyRace;
	
	// 아군 공격 유닛 첫번째 타입
	UnitType myCombatUnitType1;					/// 질럿 마린 저글링

	// 아군 공격 유닛 두번째 타입
	UnitType myCombatUnitType2;			  		/// 드라군 메딕 히드라
	
	int[] buildOrderArrayOfMyCombatUnitType;		/// 아군 공격 유닛 첫번째 타입, 두번째 타입 생산 순서
	int nextTargetIndexOfBuildOrderArray;		/// buildOrderArrayMyCombatUnitType 에서 다음 생산대상 아군 공격 유닛
	
	// 아군의 공격유닛 숫자
	int necessaryNumberOfCombatUnitType1;		/// 공격을 시작하기위해 필요한 최소한의 유닛 숫자 
	int necessaryNumberOfCombatUnitType2;		/// 공격을 시작하기위해 필요한 최소한의 유닛 숫자 
	int numberOfCompletedCombatUnitType1;		/// 첫번째 유닛 타입의 현재 유닛 숫자
	int numberOfCompletedCombatUnitType2;		/// 두번째 유닛 타입의 현재 유닛 숫자
	int myKilledUnitCount1;	 					/// 첫번째 유닛 타입의 사망자 숫자 누적값
	int myKilledUnitCount2;	 					/// 두번째 유닛 타입의 사망자 숫자 누적값
	
	// 아군 공격 유닛 목록
	ArrayList<Unit> myCombatUnitType1List = new ArrayList<Unit>();       // 질럿   마린 저글링
	ArrayList<Unit> myCombatUnitType2List = new ArrayList<Unit>();       // 드라군 메딕 히드라

	// 아군 방어 건물 첫번째 타입
	UnitType myDefenseBuildingType1;			/// 파일런 벙커 크립콜로니
	
	// 아군 방어 건물 두번째 타입
	UnitType myDefenseBuildingType2;			/// 포톤  터렛  성큰콜로니

	// 아군 방어 건물 건설 숫자
	int necessaryNumberOfDefenseBuilding1;		/// 방어 건물 건설 갯수
	int necessaryNumberOfDefenseBuilding2;		/// 방어 건물 건설 갯수

	// 아군 방어 건물 건설 위치
	BuildOrderItem.SeedPositionStrategy seedPositionStrategyOfMyDefenseBuildingType;
	BuildOrderItem.SeedPositionStrategy seedPositionStrategyOfMyCombatUnitTrainingBuildingType;

	// 아군 방어 건물 목록 
	ArrayList<Unit> myDefenseBuildingType1List = new ArrayList<Unit>();  // 파일런 벙커 크립
	ArrayList<Unit> myDefenseBuildingType2List = new ArrayList<Unit>();  // 캐논   터렛 성큰

	// 업그레이드 / 리서치 할 것 
	UpgradeType 	necessaryUpgradeType1;		/// 드라군사정거리업 마린공격력업     히드라사정거리업
	UpgradeType 	necessaryUpgradeType2;		/// 질럿발업         마린사정거리업   히드라발업
	TechType 		necessaryTechType;			///            마린스팀팩

	
	
	// 적군 공격 유닛 숫자
	int numberOfCompletedEnemyCombatUnit;

	// 적군 공격 유닛 사망자 수 
	int enemyKilledUnitCount;					/// 적군 공격유닛 사망자 숫자 누적값

	
	
	// 아군 / 적군의 본진, 첫번째 길목, 두번째 길목
	BaseLocation myMainBaseLocation; 
	BaseLocation myFirstExpansionLocation; 
	Chokepoint myFirstChokePoint;
	Chokepoint mySecondChokePoint;
	BaseLocation enemyMainBaseLocation;
	BaseLocation enemyFirstExpansionLocation; 
	Chokepoint enemyFirstChokePoint;
	Chokepoint enemySecondChokePoint;
		
	boolean isInitialBuildOrderFinished;	/// setInitialBuildOrder 에서 입력한 빌드오더가 다 끝나서 빌드오더큐가 empty 되었는지 여부

	enum CombatState { 
		defenseMode,						// 아군 진지 방어
		attackStarted,						// 적 공격 시작
		attackMySecondChokepoint,			// 아군 두번째 길목까지 공격
		attackEnemySecondChokepoint,		// 적진 두번째 길목까지 공격
		attackEnemyFirstExpansionLocation,	// 적진 앞마당까지 공격
		attackEnemyMainBaseLocation,		// 적진 본진까지 공격
		eliminateEnemy						// 적 Eliminate 
	};
		
	CombatState combatState;				/// 전투 상황

	public StrategyManager() {
	}

	/// 경기가 시작될 때 일회적으로 전략 초기 세팅 관련 로직을 실행합니다
	public void onStart() {
		
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		
		// 과거 게임 기록을 로딩합니다
		//loadGameRecordList();
		
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////

		/// 변수 초기값을 설정합니다
		setVariables();

		/// 게임 초기에 사용할 빌드오더를 세팅합니다
		//setInitialBuildOrder();		
	}
	
	/// 변수 초기값을 설정합니다
	void setVariables(){
		
		// 참가자께서 자유롭게 초기값을 수정하셔도 됩니다 
		
		myPlayer = MyBotModule.Broodwar.self();
		myRace = MyBotModule.Broodwar.self().getRace();
		enemyPlayer = InformationManager.Instance().enemyPlayer;

		numberOfCompletedCombatUnitType1 = 0;
		numberOfCompletedCombatUnitType2 = 0;
		myKilledUnitCount1 = 0;
		myKilledUnitCount2 = 0;
		
		numberOfCompletedEnemyCombatUnit = 0;
		enemyKilledUnitCount = 0;
	
		isInitialBuildOrderFinished = false;
		combatState = CombatState.defenseMode;
		

		// 공격 유닛 종류 설정  
		myCombatUnitType1 = UnitType.Terran_Goliath;
		myCombatUnitType2 = UnitType.Terran_Medic;
		
		// 공격 모드로 전환하기 위해 필요한 최소한의 유닛 숫자 설정
		necessaryNumberOfCombatUnitType1 = 12;                      // 공격을 시작하기위해 필요한 최소한의 마린 유닛 숫자 
		necessaryNumberOfCombatUnitType2 = 2;                       // 공격을 시작하기위해 필요한 최소한의 메딕 유닛 숫자 

		// 공격 유닛 생산 순서 설정
		//buildOrderArrayOfMyCombatUnitType = new int[]{1,1,1,1,2}; 	// 마린 마린 마린 마린 메딕 ...
//		buildOrderArrayOfMyCombatUnitType = new int[]{1,1,1,1,2,2,3,1,2,3,1};
		buildOrderArrayOfMyCombatUnitType = new int[]{1,1,1,1,2,2,1,1,2,1}; 
		nextTargetIndexOfBuildOrderArray = 0; 			        	// 다음 생산 순서 index
		
		// 방어 건물 종류 및 건설 갯수 설정
		myDefenseBuildingType1 = UnitType.Terran_Bunker;
		necessaryNumberOfDefenseBuilding1 = 0; 						
		myDefenseBuildingType2 = UnitType.Terran_Missile_Turret;
		necessaryNumberOfDefenseBuilding2 = 5;						
		
		// 방어 건물 건설 위치 설정
		seedPositionStrategyOfMyDefenseBuildingType 
			= BuildOrderItem.SeedPositionStrategy.FirstChokePoint;	// 첫번째 길목
		//seedPositionStrategyOfMyCombatUnitTrainingBuildingType = BuildOrderItem.SeedPositionStrategy.FirstChokePoint;	// 첫번째 길목
		seedPositionStrategyOfMyCombatUnitTrainingBuildingType = BuildOrderItem.SeedPositionStrategy.MainBaseLocation;

		// 업그레이드 및 리서치 대상 설정
		necessaryUpgradeType1 = UpgradeType.Terran_Vehicle_Weapons;//UpgradeType.U_238_Shells;
		necessaryUpgradeType2 = UpgradeType.Terran_Infantry_Weapons;
		necessaryTechType = TechType.Stim_Packs;
		
	}


	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다
	public void update() {

		/// 변수 값을 업데이트 합니다
		updateVariables();

		/// 일꾼을 계속 추가 생산합니다
		executeWorkerTraining();

		/// Supply DeadLock 예방 및 SupplyProvider 가 부족해질 상황 에 대한 선제적 대응으로서 SupplyProvider를 추가 건설/생산합니다
		executeSupplyManagement();

		/// 방어건물 및 공격유닛 생산 건물을 건설합니다
		executeBuildingConstruction();

		/// 업그레이드 및 테크 리서치를 실행합니다
		executeUpgradeAndTechResearch();

		/// 공격유닛을 계속 추가 생산합니다
		executeCombatUnitTraining();

		/// 전반적인 전투 로직 을 갖고 전투를 수행합니다
		executeCombat();

		/// StrategyManager 의 수행상황을 표시합니다
		drawStrategyManagerStatus();

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

		// 이번 게임의 로그를 남깁니다
		//saveGameLog();
		
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	}
	
	/// 전반적인 전투 로직 을 갖고 전투를 수행합니다
	public void executeCombat() {

		// 공격을 시작할만한 상황이 되기 전까지는 방어를 합니다
		if (combatState == CombatState.defenseMode) {
			/// 아군 공격유닛 들에게 방어를 지시합니다
			commandMyCombatUnitToDefense();

			/// 공격 모드로 전환할 때인지 여부를 판단합니다			
			if (isTimeToStartAttack() ) {
				combatState = CombatState.attackStarted;
			}
		}
		// 공격을 시작한 후에는 공격을 계속 실행하다가, 거의 적군 기지를 파괴하면 Eliminate 시키기를 합니다 
		else {
			if (combatState != CombatState.eliminateEnemy) {
				/// 아군 공격 유닛들에게 공격을 지시합니다 
				commandMyCombatUnitToAttack();

				/// 적군을 Eliminate 시키는 모드로 전환할지 여부를 판단합니다 
				if (isTimeToStartElimination() ) {
					combatState = CombatState.eliminateEnemy;
				}
			}
			else {
				/// 적군을 Eliminate 시키도록 아군 공격 유닛들에게 지시합니다
				commandMyCombatUnitToEliminate();	
			}
		}
	}
	
	/// 아군 공격유닛 들에게 방어를 지시합니다
	void commandMyCombatUnitToDefense(){

		// 아군 방어 건물이 세워져있는 위치
		Position myDefenseBuildingPosition = null;
		switch (seedPositionStrategyOfMyDefenseBuildingType) {
			case MainBaseLocation: myDefenseBuildingPosition = myMainBaseLocation.getPosition(); break;
			case FirstChokePoint: myDefenseBuildingPosition = myFirstChokePoint.getCenter(); break;
			case FirstExpansionLocation: myDefenseBuildingPosition = myFirstExpansionLocation.getPosition(); break;
			case SecondChokePoint: myDefenseBuildingPosition = mySecondChokePoint.getCenter(); break;
			default: myDefenseBuildingPosition = myMainBaseLocation.getPosition(); break;
		}

		// 아군 공격유닛을 방어 건물이 세워져있는 위치로 배치시킵니다
		// 아군 공격유닛을 아군 방어 건물 뒤쪽에 배치시켰다가 적들이 방어 건물을 공격하기 시작했을 때 다함께 싸우게하면 더 좋을 것입니다
		
		Position defLocation = null;
		int radius = 0;
		if (firstExpan) {
			defLocation = mySecondChokePoint.getPoint();
			radius = defradius2;
		} else {
			defLocation = terran.tilesPool[terran.mapStrategy.getFirstBarrackstTilePosition().getX()+1][terran.mapStrategy.getFirstBarrackstTilePosition().getY()+1].toPosition();
			radius = defradius;
		}
		
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit.isIdle()) {
				if (unit.getType() == UnitType.Terran_Marine) {

					boolean hasCommanded = false;

					// 테란 종족의 경우 마린을 벙커안에 집어넣기
//					if (unit.getType() == UnitType.Terran_Marine) {
						for(Unit bunker : myDefenseBuildingType1List) {
							if (bunker.getLoadedUnits().size() < 4 && bunker.canLoad(unit)) {
								commandUtil.rightClick(unit, bunker);
								hasCommanded = true;
							}
						}
//					}
										
					// 명령 내린 적이 없으면, 방어 건물 위치로 이동
					if (hasCommanded == false) {
						commandUtil.attackMove(unit, myDefenseBuildingPosition);
					}
				} else if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
					
					if (Common.getDistanceSquared(unit.getPosition(), defLocation) > radius) {
						commandUtil.move(unit, terran.mapStrategy.getFirstBarrackstTilePosition().toPosition());
					} else {
						if (!unit.isSieged()) unit.siege();
					}
					
				} else if (unit.getType() == UnitType.Terran_Vulture) {
					
					if (terran.underAttackBuildingCnt > 0) {
						commandUtil.attackMove(unit, terran.underAttackBuilding.getPosition());
					} else {
						if (terran.underAttackUnitCnt > 0) {
							commandUtil.attackMove(unit, terran.underAttackUnit.getPosition());
						} else {
							if (Common.getDistanceSquared(unit.getPosition(), defLocation) > radius) {
								commandUtil.move(unit, terran.mapStrategy.getFirstBarrackstTilePosition().toPosition());
							}
						}
					}
					
					
				} else if (unit.getType() == UnitType.Terran_Goliath) {
					if (terran.underAttackBuildingCnt > 0) {
						commandUtil.attackMove(unit, terran.underAttackBuilding.getPosition());
					} else {
						if (terran.underAttackUnitCnt > 0) {
							commandUtil.attackMove(unit, terran.underAttackUnit.getPosition());
						} else {
							if (Common.getDistanceSquared(unit.getPosition(), defLocation) > radius) {
								commandUtil.move(unit, terran.mapStrategy.getFirstBarrackstTilePosition().toPosition());
							}
						}
					}
				}
			}
		}	
	
	}
	
	/// 공격 모드로 전환할 때인지 여부를 리턴합니다
	boolean isTimeToStartAttack(){

		///////////////////////////////////////////////////////////////////
		///////////////////////// 아래의 코드를 수정해보세요 ///////////////////////
		//
		// TODO 1. 적군 기지로 역습 (공격)을 갈 타이밍인지 판단하는 로직            (예상 개발시간  5분)
		//
		// 목표 : 현재는 단순히 아군 공격 유닛 숫자가 최소 숫자를 넘으면 공격하도록 되어있습니다.
		//      그러나, 아군 공격 유닛 숫자가 최소 숫자를 넘었더라도 
		//      먼저 적군이 공격해올때까지 기다렸다가 적군의 공격을 방어한 후 역습을 가는 것이 훨씬 나은 전략입니다.
		//
		//      적군 공격 유닛 사망자 수 enemyKilledUnitCount 를 이용하여 
		//		방어모드에서 공격모드로 전환할지 여부를 리턴하도록 수정해보세요
		//      return false = 방어모드를 유지
		//      return true = 공격모드로 전환
		// 
		// Hint : 적군 공격 유닛 사망자 수가 대략 8 이상이면 충분히 역습갈만한 타이밍이라고 볼 수 있지 않을까요?
		//
		///////////////////////////////////////////////////////////////////

//		if (myPlayer.completedUnitCount(myCombatUnitType1) >= necessaryNumberOfCombatUnitType1
//				&& myPlayer.completedUnitCount(myCombatUnitType2) >= necessaryNumberOfCombatUnitType2) 
//		{
//			return true;
//		}
//
//		if (myPlayer.completedUnitCount(myCombatUnitType1) 
//				+ myPlayer.completedUnitCount(myCombatUnitType2) >= 40) 
//		{
//			return true;
//		}
		
		if (terran.tankCnt > 12) return true;
		
		
		return false;
	}

	/// 아군 공격 유닛들에게 공격을 지시합니다 
	void commandMyCombatUnitToAttack(){
		
		///////////////////////////////////////////////////////////////////
		///////////////////////// 아래의 코드를 수정해보세요 ///////////////////////
		//
		// TODO 2. 적군 기지로 아군 공격 유닛들이 공격가게 하는 로직                 (예상 개발시간 20분)
		//
		// 목표 : 현재는 단순히 아군 공격 유닛들을 적군의 Main BaseLocation 을 향해 바로 공격하러 가도록 되어있습니다.
		//      이렇게하면, 한줄로 줄서서 이동하기 때문에 효과적인 공격이 되기 어려운데요
		//      아군 공격 유닛들이 떼지어서 군집이동을 하도록 수정해보세요
		// 
		// Hint : 군집이동을 하게 하는 방법은 수많은 방법이 있는데요, 
		//
		//        가장 간단한 방법은 중간 집결지를 두는 것입니다.
		//
		//        1단계로, 적군의 두번째 길목 (enemySecondChokePoint.center()) 을 향해 공격가게 한 후,
		// 
		//        적군의 두번째 길목 길목 주위에 병력의 상당수가 도착했을 때 (unit.distance() 메소드로 판단)
		//
		//        이제 2단계로 전환하여, 적군 기지 (enemyMainBaseLocation.getPosition()) 로 공격하게 하는 것이지요.
		//
		///////////////////////////////////////////////////////////////////

		// 최종 타겟은 적군의 Main BaseLocation 
		BaseLocation targetEnemyBaseLocation = enemyMainBaseLocation;
		Position targetPosition = null;
		
		if (targetEnemyBaseLocation != null) 
		{
			// 먼저, 테란 종족의 경우, 벙커 안에 있는 유닛은 밖으로 빼낸다
			if (myRace == Race.Terran) {
				for(Unit bunker : myDefenseBuildingType1List) {
					if (bunker.getLoadedUnits().size() > 0) {
						
						boolean isThereSomeEnemyUnit = false;
						for(Unit someUnit : MyBotModule.Broodwar.getUnitsInRadius(bunker.getPosition(), 6 * Config.TILE_SIZE)) {
							if (someUnit.getPlayer() == enemyPlayer) {
								isThereSomeEnemyUnit = true;
								break;
							}
						}
						if (isThereSomeEnemyUnit == false) {
							bunker.unloadAll();
						}
					}
				}
			}

			// targetPosition 을 설정한다
			targetPosition = targetEnemyBaseLocation.getPosition();
			
			// 모든 아군 공격유닛들로 하여금 targetPosition 을 향해 attackMove 로 공격하게 한다
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
					unit.unsiege();
				}
				if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || unit.getType() == UnitType.Terran_Goliath || unit.getType() == UnitType.Terran_Vulture) {					
					if (unit.isIdle()) {						
						commandUtil.attackMove(unit, targetPosition);
					}
				}
				if (unit.getType() == UnitType.Terran_Barracks) {
					unit.lift();
					doorOpened = true;
				}
			}
			
		}	
	}

	/// 적군을 Eliminate 시키는 모드로 전환할지 여부를 리턴합니다 
	boolean isTimeToStartElimination(){

		///////////////////////////////////////////////////////////////////
		///////////////////////// 아래의 코드를 수정해보세요 ///////////////////////
		//
		// TODO 3. 적군을 Eliminate 시키는 모드로 전환할 때인지 판단하는 로직   (예상 개발시간  5분)
		//
		// 목표 : 공격이 성공하여 적군의 BaseLocation 의 많은 건물들과 유닛들을 파괴했지만, 
		//      지도 곳곳에 적군의 건물들이 몇개씩 남아서 게임이 끝나지 않는 상태가 될 수 있습니다.
		// 
		//      적군을 Eliminate 시키는 모드로 전환을 하면, 모든 공격유닛들을 지도 곳곳으로 보내게 되는데요
		// 
		//      현재는 적군의 BaseLocation 에 도착했는지만 갖고 판단하여 모드를 전환하므로,
		//      아직 적군 공격유닛이 다수 남아있는 상태에서도 모드를 전환하게 될 수 있습니다.
		//
		//      이를 더욱 잘 판단하는 코드로 수정해보세요
		//
		//      return false : 적군을 Eliminate 시키는 모드로 전환할 때가 아님
		//      return true  : 적군을 Eliminate 시키는 모드로 전환할 때임
		// 
		// Hint : 적군 공격 유닛 숫자  numberOfEnemyCombatUnit,
		//        적군 공격 유닛 사망자 수 numEnemyCombatUnitKilled,
		//        아군 공격 유닛 숫자 numberOfCompletedCombatUnitType1, numberOfCompletedCombatUnitType2 
		//        등을 조건문에 추가하면 더 적절한 시점을 판단할 수 있지 않을까요?
		// 
		///////////////////////////////////////////////////////////////////

		// 적군의 Main BaseLocation
		BaseLocation enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(enemyPlayer);
		
		if (enemyMainBaseLocation != null) 
		{
			// 적군의 Main BaseLocation 에 아군 공격 유닛이 도착하였는가
			boolean isMyCombatUnitArrivedAtEnemyBaseLocation = false;
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
						|| unit.getType() == UnitType.Terran_Vulture || unit.getType() == UnitType.Terran_Goliath) {
					
					if (unit.getDistance(enemyMainBaseLocation.getPosition()) < 4 * Config.TILE_SIZE ) {
						isMyCombatUnitArrivedAtEnemyBaseLocation  = true;
						break;
					}
				} 
			}
					
			// 적군의 Main BaseLocation 에 도착했으면 이제 Eliminate 모드로 전환할 때이다
			if (isMyCombatUnitArrivedAtEnemyBaseLocation == true) {
				return true;
			}
		}

		
		return false;
	}
	
	/// 적군을 Eliminate 시키도록 아군 공격 유닛들에게 지시합니다
	void commandMyCombatUnitToEliminate(){

		if (enemyPlayer == null || enemyRace == Race.Unknown) 
		{
			return;
		}
		
		Random random = new Random();
		int mapHeight = MyBotModule.Broodwar.mapHeight();	// 128
		int mapWidth = MyBotModule.Broodwar.mapWidth();		// 128
		
		// 맵 전체의 BaseLocation들 목록
		List<bwta.BaseLocation> baselocationList = BWTA.getBaseLocations();

		// 적군의 남은 건물 정보
		UnitInfo remainingBuildingUnitInfo = null;
		double minDistance = 1000000000;
		double tempDistance = 0;		
		for(Map.Entry<Integer, UnitInfo> unitInfoEntry : InformationManager.Instance().getUnitAndUnitInfoMap(enemyPlayer).entrySet()) {
			if (unitInfoEntry.getValue().getLastHealth() > 0) 
			{
				tempDistance = myMainBaseLocation.getDistance(unitInfoEntry.getValue().getLastPosition());
				if (minDistance > tempDistance) {
					minDistance = tempDistance;
					remainingBuildingUnitInfo = unitInfoEntry.getValue();
				}
			}
		}			
		
		// 아군 공격 유닛들로 하여금 적군의 남은 건물을 알고 있으면 그것을 공격하게 하고, 그렇지 않으면 맵 전체를 랜덤하게 돌아다니도록 합니다 
		for(Unit unit : myPlayer.getUnits()) {
			if (unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode || unit.getType() == UnitType.Terran_Goliath || unit.getType() == UnitType.Terran_Vulture) {
				
				if (unit.isIdle()) {
					
					Position targetPosition = null;
					if (remainingBuildingUnitInfo != null) {
						targetPosition = remainingBuildingUnitInfo.getLastPosition();
					}
					else {
						targetPosition = new Position(random.nextInt(mapWidth * Config.TILE_SIZE), random.nextInt(mapHeight * Config.TILE_SIZE));
					}
					
					if (unit.canAttack()) {
						commandUtil.attackMove(unit, targetPosition);
					}
					else {
						commandUtil.move(unit, targetPosition);
					}
				}
			}
		}
	}
	
	/// StrategyManager 의 수행상황을 표시합니다
	private void drawStrategyManagerStatus() {
		
		// 아군 공격유닛 숫자 및 적군 공격유닛 숫자
		MyBotModule.Broodwar.drawTextScreen(200, 250, "My " + myCombatUnitType1.toString());
		MyBotModule.Broodwar.drawTextScreen(300, 250, "alive " + numberOfCompletedCombatUnitType1);
		MyBotModule.Broodwar.drawTextScreen(350, 250, "killed " + myKilledUnitCount1);
		MyBotModule.Broodwar.drawTextScreen(200, 260, "My " + myCombatUnitType2.toString());
		MyBotModule.Broodwar.drawTextScreen(300, 260, "alive " + numberOfCompletedCombatUnitType2);
		MyBotModule.Broodwar.drawTextScreen(350, 260, "killed " + myKilledUnitCount2);
		MyBotModule.Broodwar.drawTextScreen(200, 270, "Enemy CombatUnit");
		MyBotModule.Broodwar.drawTextScreen(300, 270, "alive " + numberOfCompletedEnemyCombatUnit);
		MyBotModule.Broodwar.drawTextScreen(350, 270, "killed " + enemyKilledUnitCount);

		// setInitialBuildOrder 에서 입력한 빌드오더가 다 끝나서 빌드오더큐가 empty 되었는지 여부
		MyBotModule.Broodwar.drawTextScreen(200, 280, "isInitialBuildOrderFinished " + isInitialBuildOrderFinished);
		// 전투 상황
		MyBotModule.Broodwar.drawTextScreen(200, 290, "combatState " + combatState.ordinal());
	}


	
	
	
	
	
	
	private static StrategyManager instance = new StrategyManager();

	/// static singleton 객체를 리턴합니다
	public static StrategyManager Instance() {
		return instance;
	}

	private CommandUtil commandUtil = new CommandUtil();
		
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


	///  경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {
		
		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가
		
		// 과거 게임 기록 + 이번 게임 기록을 저장합니다
		saveGameRecordList(isWinner);
		
		// BasicBot 1.1 Patch End //////////////////////////////////////////////////		
	}

	/// 변수 값을 업데이트 합니다 
	void updateVariables(){

		enemyRace = InformationManager.Instance().enemyRace;
		
		if (BuildManager.Instance().buildQueue.isEmpty()) {
			isInitialBuildOrderFinished = true;
		}
		
		// 아군의 공격유닛 숫자
		numberOfCompletedCombatUnitType1 = myPlayer.completedUnitCount(myCombatUnitType1);
		numberOfCompletedCombatUnitType2 = myPlayer.completedUnitCount(myCombatUnitType2);

		// 적군의 공격유닛 숫자
		numberOfCompletedEnemyCombatUnit = 0;
		for(Map.Entry<Integer, UnitInfo> unitInfoEntry : InformationManager.Instance().getUnitAndUnitInfoMap(enemyPlayer).entrySet()) {
			UnitInfo enemyUnitInfo = unitInfoEntry.getValue(); 
			if (enemyUnitInfo.getType().isWorker() == false && enemyUnitInfo.getType().isBuilding() == false) {
				numberOfCompletedEnemyCombatUnit ++; 
			}
		}
		

		// 아군 / 적군의 본진, 첫번째 길목, 두번째 길목
		myMainBaseLocation = InformationManager.Instance().getMainBaseLocation(myPlayer); 
		myFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(myPlayer); 
		myFirstChokePoint = InformationManager.Instance().getFirstChokePoint(myPlayer);
		mySecondChokePoint = InformationManager.Instance().getSecondChokePoint(myPlayer);
		enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(enemyPlayer);
		enemyFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(enemyPlayer); 
		enemyFirstChokePoint = InformationManager.Instance().getFirstChokePoint(enemyPlayer);
		enemySecondChokePoint = InformationManager.Instance().getSecondChokePoint(enemyPlayer);
		
		// 아군 방어 건물 목록, 공격 유닛 목록
//		myDefenseBuildingType1List.clear();
//		myDefenseBuildingType2List.clear();
//		myCombatUnitType1List.clear();
//		myCombatUnitType2List.clear();
//		for(Unit unit : myPlayer.getUnits()) {			
//			if (unit.getType() == myCombatUnitType1) { myCombatUnitType1List.add(unit); }
//			else if (unit.getType() == myCombatUnitType2) { myCombatUnitType2List.add(unit); }
//			else if (unit.getType() == myDefenseBuildingType1) { myDefenseBuildingType1List.add(unit); }
//			else if (unit.getType() == myDefenseBuildingType2) { myDefenseBuildingType2List.add(unit); }			
//		}
	}

	/// 아군 / 적군 공격 유닛 사망 유닛 숫자 누적값을 업데이트 합니다
	public void onUnitDestroy(Unit unit) {
		if (unit.getType().isNeutral()) {
			return;
		}
		
		if (unit.getPlayer() == myPlayer) {
			// 아군 공격 유닛 첫번째 타입의 사망 유닛 숫자 누적값
			if (unit.getType() == myCombatUnitType1) {
				myKilledUnitCount1 ++;				
			}
			// 아군 공격 유닛 두번째 타입의 사망 유닛 숫자 누적값
			else if (unit.getType() == myCombatUnitType2) {
				myKilledUnitCount2 ++;		
			} 
		}
		else if (unit.getPlayer() == enemyPlayer) {
			/// 적군 공격 유닛타입의 사망 유닛 숫자 누적값
			if (unit.getType().isWorker() == false && unit.getType().isBuilding() == false) {
				enemyKilledUnitCount ++;
			}			
		} 
	}

	/// 일꾼을 계속 추가 생산합니다
	public void executeWorkerTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			// workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType());
			
			int eggWorkerCount = 0;

			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType().isResourceDepot()) {
					if (unit.isTraining()) {
						workerCount += unit.getTrainingQueue().size();
					}
				}
			}

			// 최적의 일꾼 수 = 미네랄 * (1~1.5) + 가스 * 3
			int optimalWorkerCount = 0;
			for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(myPlayer)) {
				optimalWorkerCount += baseLocation.getMinerals().size() * 1.5;
				optimalWorkerCount += baseLocation.getGeysers().size() * 3;
			}
			
			if (workerCount < optimalWorkerCount) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining() == false || unit.getLarva().size() > 0) {
							// 빌드큐에 일꾼 생산이 1개는 있도록 한다
							if (BuildManager.Instance().buildQueue
									.getItemCount(InformationManager.Instance().getWorkerType(), null) == 0 && eggWorkerCount == 0) {
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

	/// Supply DeadLock 예방 및 SupplyProvider 가 부족해질 상황 에 대한 선제적 대응으로서<br>
	/// SupplyProvider를 추가 건설/생산합니다
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
		// InitialBuildOrder 진행중이라도 supplyUsed 가 supplyTotal 보다 커져버리면 실행하도록 합니다
//		if (isInitialBuildOrderFinished == false && MyBotModule.Broodwar.self().supplyUsed() <= MyBotModule.Broodwar.self().supplyTotal()  ) {
//			return;
//		}
		
		if (isInitialBuildOrderFinished == false) {
			if (terran.supplyProvidedCnt + terran.supplyProvidedConstCnt <= terran.supplyOccupiedCnt + terran.supplyOccupiedConstCnt) {
			} else {
				return;
			}
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
				System.out.println("------ currentSupplyShortage : " + currentSupplyShortage + " onBuildingSupplyCount : " + onBuildingSupplyCount);

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
						BuildManager.Instance().buildQueue.queueAsHighestPriority(
								new MetaType(InformationManager.Instance().getBasicSupplyProviderUnitType()), true);
					}
				}
			}
		}

		// BasicBot 1.1 Patch End ////////////////////////////////////////////////		
	}

	/// 방어건물 및 공격유닛 생산 건물을 건설합니다
	void executeBuildingConstruction() {
		
		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}
		
		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		
		int engbayCnt = terran.engbayCnt + terran.engbayConstCnt;
		engbayCnt += BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay);
		engbayCnt += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Engineering_Bay, null);
		
		if (engbayCnt == 0) {
			
			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Engineering_Bay) == 0 ) {
				if (BuildManager.Instance().getAvailableMinerals() >= UnitType.Terran_Engineering_Bay.mineralPrice()) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Engineering_Bay, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				}			
			}
			
		}
		
		
		int academyCnt = terran.academyCnt + terran.academyConstCnt; //myPlayer.allUnitCount(UnitType.Terran_Academy);
		academyCnt += BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy);
		academyCnt += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Academy, null);
		
		if (academyCnt == 0) {
			
			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Academy) == 0 ) {
				if (BuildManager.Instance().getAvailableMinerals() >= UnitType.Terran_Academy.mineralPrice()) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Academy, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				}			
			}
			
		}
		
		if (terran.academyCnt > 0) {
			if (terran.cmdCenterCnt > terran.comsatStationCnt) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Comsat_Station) == 0 ) {
					if (BuildManager.Instance().getAvailableMinerals() >= UnitType.Terran_Comsat_Station.mineralPrice()) {
						BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Comsat_Station);
					}			
				}
			}
		}

		boolean			isPossibleToConstructDefenseBuildingType1 = false;
		boolean			isPossibleToConstructDefenseBuildingType2 = false;	
		boolean			isPossibleToConstructCombatUnitTrainingBuildingType = false;
		
		// 방어 건물 증설을 우선적으로 실시한다
		
		// 현재 방어 건물 갯수
		int numberOfMyDefenseBuildingType1 = 0; 
		int numberOfMyDefenseBuildingType2 = 0;
		
		numberOfMyDefenseBuildingType1 += myPlayer.allUnitCount(myDefenseBuildingType1);
		numberOfMyDefenseBuildingType1 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType1);
		numberOfMyDefenseBuildingType1 += ConstructionManager.Instance().getConstructionQueueItemCount(myDefenseBuildingType1, null);
		numberOfMyDefenseBuildingType2 += myPlayer.allUnitCount(myDefenseBuildingType2);
		numberOfMyDefenseBuildingType2 += BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType2);
		numberOfMyDefenseBuildingType2 += ConstructionManager.Instance().getConstructionQueueItemCount(myDefenseBuildingType2, null);
		
//		if (myPlayer.completedUnitCount(UnitType.Terran_Barracks) > 0) {
//			isPossibleToConstructDefenseBuildingType1 = true;	
//		}
		if (myPlayer.completedUnitCount(UnitType.Terran_Engineering_Bay) > 0) {
			isPossibleToConstructDefenseBuildingType2 = true;	
		}
		isPossibleToConstructCombatUnitTrainingBuildingType = true;	
			

//		if (isPossibleToConstructDefenseBuildingType1 == true 
//			&& numberOfMyDefenseBuildingType1 < necessaryNumberOfDefenseBuilding1) {
//			if (BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType1) == 0 ) {
//				if (BuildManager.Instance().getAvailableMinerals() >= myDefenseBuildingType1.mineralPrice()) {
//					BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType1, 
//							seedPositionStrategyOfMyDefenseBuildingType, false);
//				}			
//			}
//		}
		if (isPossibleToConstructDefenseBuildingType2 == true && numberOfMyDefenseBuildingType2 < necessaryNumberOfDefenseBuilding2) {
			if (BuildManager.Instance().buildQueue.getItemCount(myDefenseBuildingType2) == 0 ) {
				if (BuildManager.Instance().getAvailableMinerals() >= myDefenseBuildingType2.mineralPrice()) {
					BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType2, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
				}
				
				if (firstExpan) {
					BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType2, BuildOrderItem.SeedPositionStrategy.SecondChokePoint, false);
					BuildManager.Instance().buildQueue.queueAsHighestPriority(myDefenseBuildingType2, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, false);
				}
			}
		}

		// 현재 공격 유닛 생산 건물 갯수
		int numberOfMyCombatUnitTrainingBuilding = myPlayer.allUnitCount(InformationManager.Instance().getBasicCombatBuildingType());
		numberOfMyCombatUnitTrainingBuilding += BuildManager.Instance().buildQueue.getItemCount(InformationManager.Instance().getBasicCombatBuildingType());
		numberOfMyCombatUnitTrainingBuilding += ConstructionManager.Instance().getConstructionQueueItemCount(InformationManager.Instance().getBasicCombatBuildingType(), null);

		int maxFactoryCnt = 0, maxStarportCnt = 0;
		if (terran.cmdCenterCnt == 1) {
			maxFactoryCnt = 2;
			maxStarportCnt = 1;
		} else if (terran.cmdCenterCnt == 2) {
			maxFactoryCnt = 5;
			maxStarportCnt = 1;
		} else if (terran.cmdCenterCnt >= 3) {
			maxFactoryCnt = terran.cmdCenterCnt*3;
			maxStarportCnt = 1;
		}
		
		// 공격 유닛 생산 건물 증설 : 돈이 남아돌면 실시. 최대 6개 까지만
		if (isPossibleToConstructCombatUnitTrainingBuildingType == true
			&& BuildManager.Instance().getAvailableMinerals() > 300 && BuildManager.Instance().getAvailableGas() > 200  
			&& numberOfMyCombatUnitTrainingBuilding < maxFactoryCnt) {
			// 게이트웨이 / 배럭 / 해처리 증설
			if (BuildManager.Instance().buildQueue.getItemCount(InformationManager.Instance().getBasicCombatBuildingType()) == 0 ) 
			{
				BuildManager.Instance().buildQueue.queueAsHighestPriority(InformationManager.Instance().getBasicCombatBuildingType(), 
						seedPositionStrategyOfMyCombatUnitTrainingBuildingType, false);
			}
		}
		
		if (terran.tankCnt >= 6) {
			int starportCnt = myPlayer.allUnitCount(UnitType.Terran_Starport);
			starportCnt += BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport);
			starportCnt += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Starport, null);
			if (starportCnt < maxStarportCnt && BuildManager.Instance().getAvailableMinerals() > 300 && BuildManager.Instance().getAvailableGas() > 200 ) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Starport) == 0 ) 
				{
					BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Starport, seedPositionStrategyOfMyCombatUnitTrainingBuildingType, false);
				}
			}
		}
		
		int machineShopCnt = myPlayer.allUnitCount(UnitType.Terran_Machine_Shop);
		machineShopCnt += BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop);
		machineShopCnt += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Machine_Shop, null);
		
		if (terran.factoryCnt > machineShopCnt) {
			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Machine_Shop) == 0) BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Machine_Shop);
		}
		
		int controlTowerCnt = terran.controlTowerCnt + terran.controlTowerConstCnt;
		controlTowerCnt += BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower);
		controlTowerCnt += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Control_Tower, null);
		
		if (terran.starportCnt > controlTowerCnt) {
			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Control_Tower) == 0) BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Control_Tower);
		}
		
		if (myPlayer.minerals() > 800) {
			int armoryCnt = terran.armoryCnt + terran.armoryConstCnt;
			armoryCnt += BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory);
			armoryCnt += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Armory, null);
			
			if (armoryCnt < 3) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Armory) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Armory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
				}
			}
		}
		
		
		if (myPlayer.minerals() > 1000) {
			if (doorOpened) {
				int cmdCenterCnt = myPlayer.allUnitCount(UnitType.Terran_Command_Center);
				cmdCenterCnt += BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center);
				cmdCenterCnt += ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Terran_Command_Center, null);
				
				if (cmdCenterCnt < 2) {
					if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Command_Center) == 0) {
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Command_Center, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Terran_Refinery, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
						firstExpan = true;
					}
				}
			}
		}
		
		
		
	}

	/// 업그레이드 및 테크 리서치를 실행합니다
	void executeUpgradeAndTechResearch() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}
		
		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		
		boolean			isTimeToStartUpgradeType1 = false;	/// 업그레이드할 타이밍인가
		boolean			isTimeToStartUpgradeType2 = false;	/// 업그레이드할 타이밍인가
		boolean			isTimeToStartResearchTech = false;	/// 리서치할 타이밍인가

		if (myPlayer.completedUnitCount(UnitType.Terran_Machine_Shop) > 0) {
			//TechType.Tank_Siege_Mode
			
			if (myPlayer.completedUnitCount(UnitType.Terran_Armory) > 0) {
				if (myPlayer.getUpgradeLevel(UpgradeType.Charon_Boosters) == 0 && myPlayer.isUpgrading(UpgradeType.Charon_Boosters) == false && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Charon_Boosters) == 0)
				{
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Charon_Boosters, false);
				}
			}
			
			if (myPlayer.hasResearched(TechType.Tank_Siege_Mode) == false && myPlayer.isResearching(TechType.Tank_Siege_Mode) == false && BuildManager.Instance().buildQueue.getItemCount(TechType.Tank_Siege_Mode) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Tank_Siege_Mode, false);
			}
			
			if (myPlayer.hasResearched(TechType.Spider_Mines) == false && myPlayer.isResearching(TechType.Spider_Mines) == false && BuildManager.Instance().buildQueue.getItemCount(TechType.Spider_Mines) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Spider_Mines, false);
			}
			
			if (myPlayer.getUpgradeLevel(UpgradeType.Ion_Thrusters) == 0 && myPlayer.isUpgrading(UpgradeType.Ion_Thrusters) == false && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Ion_Thrusters) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Ion_Thrusters, false);
			}
			
		}	
		
		// 업그레이드 / 리서치할 타이밍인지 판단
		if (myPlayer.completedUnitCount(UnitType.Terran_Armory) > 0) {
//			isTimeToStartUpgradeType1 = true;
			if (myPlayer.getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) == 0 && myPlayer.isUpgrading(UpgradeType.Terran_Vehicle_Weapons) == false && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
			}
			if (myPlayer.getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) == 0 && myPlayer.isUpgrading(UpgradeType.Terran_Vehicle_Plating) == false && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0)
			{
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
			}
			
			if (myPlayer.completedUnitCount(UnitType.Terran_Science_Facility) > 0) {
				if (myPlayer.getUpgradeLevel(UpgradeType.Terran_Vehicle_Weapons) < 3 && myPlayer.isUpgrading(UpgradeType.Terran_Vehicle_Weapons) == false && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Weapons) == 0)
				{
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Weapons, false);
				}
				if (myPlayer.getUpgradeLevel(UpgradeType.Terran_Vehicle_Plating) < 3 && myPlayer.isUpgrading(UpgradeType.Terran_Vehicle_Plating) == false && BuildManager.Instance().buildQueue.getItemCount(UpgradeType.Terran_Vehicle_Plating) == 0)
				{
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Terran_Vehicle_Plating, false);
				}
			}
		}
				
//		if (myPlayer.getUpgradeLevel(UpgradeType.U_238_Shells) > 0) {
//			isTimeToStartResearchTech = true;
//		}			
//
//		if (isTimeToStartUpgradeType1) 
//		{
//			if (myPlayer.getUpgradeLevel(necessaryUpgradeType1) == 0 
//				&& myPlayer.isUpgrading(necessaryUpgradeType1) == false
//				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType1) == 0)
//			{
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType1, false);
//			}
//		}
//		
//		if (isTimeToStartUpgradeType2) 
//		{
//			if (myPlayer.getUpgradeLevel(necessaryUpgradeType2) == 0 
//				&& myPlayer.isUpgrading(necessaryUpgradeType2) == false
//				&& BuildManager.Instance().buildQueue.getItemCount(necessaryUpgradeType2) == 0)
//			{
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryUpgradeType2, false);
//			}
//		}
//
//		if (isTimeToStartResearchTech) 
//		{
//			if (myPlayer.isResearching(necessaryTechType) == false
//				&& BuildManager.Instance().buildQueue.getItemCount(necessaryTechType) == 0)
//			{
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(necessaryTechType, false);
//			}
//		}	
	}


	/// 공격유닛을 계속 추가 생산합니다
	public void executeCombatUnitTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}
		
		if (myPlayer.supplyUsed() <= 390 ) 
		{
			for(Unit unit : myPlayer.getUnits()) {
				if (unit.getType() == UnitType.Terran_Factory) {
					if (unit.isTraining() == false || unit.getLarva().size() > 0) {

						UnitType nextUnitTypeToTrain = getNextCombatUnitTypeToTrain();
						
						
						//if (BuildManager.Instance().buildQueue.getItemCount(nextUnitTypeToTrain) == 0) {
							
						if (unit.train(nextUnitTypeToTrain)) { 
							
							//BuildManager.Instance().buildQueue.queueAsLowestPriority(nextUnitTypeToTrain, false);

							nextTargetIndexOfBuildOrderArray++;
							if (nextTargetIndexOfBuildOrderArray >= buildOrderArrayOfMyCombatUnitType.length) {
								nextTargetIndexOfBuildOrderArray = 0;
							}		
							
						}

						//}
					}
				} else if (unit.getType() == UnitType.Terran_Starport) { 
					if (unit.isTraining() == false || unit.getLarva().size() > 0) {
						
						if (myPlayer.allUnitCount(UnitType.Terran_Science_Vessel) < ((terran.tankCnt + terran.tankConstCnt)/12 + 1)
								&&BuildManager.Instance().buildQueue.getItemCount(UnitType.Terran_Science_Vessel) == 0) {	
							BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Terran_Science_Vessel, false);
						}
					}
				}
				
			}
			
		}
	}

	/// 다음에 생산할 공격유닛 UnitType 을 리턴합니다
	public UnitType getNextCombatUnitTypeToTrain() {
		
		UnitType nextUnitTypeToTrain = null;

		if (buildOrderArrayOfMyCombatUnitType[nextTargetIndexOfBuildOrderArray] == 1) {
			nextUnitTypeToTrain = UnitType.Terran_Siege_Tank_Tank_Mode;
		} 
		else if (buildOrderArrayOfMyCombatUnitType[nextTargetIndexOfBuildOrderArray] == 2) {
			nextUnitTypeToTrain = UnitType.Terran_Vulture;
		}else {
			nextUnitTypeToTrain = UnitType.Terran_Goliath;
		}
		
		return nextUnitTypeToTrain;	
	}

	
	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// 경기 결과 파일 Save / Load 및 로그파일 Save 예제 추가

	/// 과거 전체 게임 기록을 로딩합니다
	void loadGameRecordList() {
	
//		// 과거의 게임에서 bwapi-data\write 폴더에 기록했던 파일은 대회 서버가 bwapi-data\read 폴더로 옮겨놓습니다
//		// 따라서, 파일 로딩은 bwapi-data\read 폴더로부터 하시면 됩니다
//
//		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
//		String gameRecordFileName = "bwapi-data\\read\\NoNameBot_GameRecord.dat";
//		
//		BufferedReader br = null;
//		try {
//			br = new BufferedReader(new FileReader(gameRecordFileName));
//
//			System.out.println("loadGameRecord from file: " + gameRecordFileName);
//
//			String currentLine;
//			StringTokenizer st;  
//			GameRecord tempGameRecord;
//			while ((currentLine = br.readLine()) != null) {
//				
//				st = new StringTokenizer(currentLine, " ");
//				tempGameRecord = new GameRecord();
//				if (st.hasMoreTokens()) { tempGameRecord.mapName = st.nextToken();}
//				if (st.hasMoreTokens()) { tempGameRecord.myName = st.nextToken();}
//				if (st.hasMoreTokens()) { tempGameRecord.myRace = st.nextToken();}
//				if (st.hasMoreTokens()) { tempGameRecord.myWinCount = Integer.parseInt(st.nextToken());}
//				if (st.hasMoreTokens()) { tempGameRecord.myLoseCount = Integer.parseInt(st.nextToken());}
//				if (st.hasMoreTokens()) { tempGameRecord.enemyName = st.nextToken();}
//				if (st.hasMoreTokens()) { tempGameRecord.enemyRace = st.nextToken();}
//				if (st.hasMoreTokens()) { tempGameRecord.enemyRealRace = st.nextToken();}
//				if (st.hasMoreTokens()) { tempGameRecord.gameFrameCount = Integer.parseInt(st.nextToken());}
//			
//				gameRecordList.add(tempGameRecord);
//			}
//		} catch (FileNotFoundException e) {
//			System.out.println("loadGameRecord failed. Could not open file :" + gameRecordFileName);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (br != null) br.close();
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		}		
	}

	/// 과거 전체 게임 기록 + 이번 게임 기록을 저장합니다
	void saveGameRecordList(boolean isWinner) {

//		// 이번 게임의 파일 저장은 bwapi-data\write 폴더에 하시면 됩니다.
//		// bwapi-data\write 폴더에 저장된 파일은 대회 서버가 다음 경기 때 bwapi-data\read 폴더로 옮겨놓습니다
//
//		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
//		String gameRecordFileName = "bwapi-data\\write\\NoNameBot_GameRecord.dat";
//
//		System.out.println("saveGameRecord to file: " + gameRecordFileName);
//
//		String mapName = MyBotModule.Broodwar.mapFileName();
//		mapName = mapName.replace(' ', '_');
//		String enemyName = MyBotModule.Broodwar.enemy().getName();
//		enemyName = enemyName.replace(' ', '_');
//		String myName = MyBotModule.Broodwar.self().getName();
//		myName = myName.replace(' ', '_');
//
//		/// 이번 게임에 대한 기록
//		GameRecord thisGameRecord = new GameRecord();
//		thisGameRecord.mapName = mapName;
//		thisGameRecord.myName = myName;
//		thisGameRecord.myRace = MyBotModule.Broodwar.self().getRace().toString();
//		thisGameRecord.enemyName = enemyName;
//		thisGameRecord.enemyRace = MyBotModule.Broodwar.enemy().getRace().toString();
//		thisGameRecord.enemyRealRace = _InformationManager.Instance().enemyRace.toString();
//		thisGameRecord.gameFrameCount = MyBotModule.Broodwar.getFrameCount();
//		if (isWinner) {
//			thisGameRecord.myWinCount = 1;
//			thisGameRecord.myLoseCount = 0;
//		}
//		else {
//			thisGameRecord.myWinCount = 0;
//			thisGameRecord.myLoseCount = 1;
//		}
//		// 이번 게임 기록을 전체 게임 기록에 추가
//		gameRecordList.add(thisGameRecord);
//
//		// 전체 게임 기록 write
//		StringBuilder ss = new StringBuilder();
//		for (GameRecord gameRecord : gameRecordList) {
//			ss.append(gameRecord.mapName + " ");
//			ss.append(gameRecord.myName + " ");
//			ss.append(gameRecord.myRace + " ");
//			ss.append(gameRecord.myWinCount + " ");
//			ss.append(gameRecord.myLoseCount + " ");
//			ss.append(gameRecord.enemyName + " ");
//			ss.append(gameRecord.enemyRace + " ");
//			ss.append(gameRecord.enemyRealRace + " ");
//			ss.append(gameRecord.gameFrameCount + "\n");
//		}
//		
//		_Common.overwriteToFile(gameRecordFileName, ss.toString());
	}

	/// 이번 게임 중간에 상시적으로 로그를 저장합니다
	void saveGameLog() {
		
		// 100 프레임 (5초) 마다 1번씩 로그를 기록합니다
		// 참가팀 당 용량 제한이 있고, 타임아웃도 있기 때문에 자주 하지 않는 것이 좋습니다
		// 로그는 봇 개발 시 디버깅 용도로 사용하시는 것이 좋습니다
//		if (MyBotModule.Broodwar.getFrameCount() % 100 != 0) {
//			return;
//		}
//
//		// TODO : 파일명은 각자 봇 명에 맞게 수정하시기 바랍니다
//		String gameLogFileName = "bwapi-data\\write\\NoNameBot_LastGameLog.dat";
//
//		String mapName = MyBotModule.Broodwar.mapFileName();
//		mapName = mapName.replace(' ', '_');
//		String enemyName = MyBotModule.Broodwar.enemy().getName();
//		enemyName = enemyName.replace(' ', '_');
//		String myName = MyBotModule.Broodwar.self().getName();
//		myName = myName.replace(' ', '_');
//
//		StringBuilder ss = new StringBuilder();
//		ss.append(mapName + " ");
//		ss.append(myName + " ");
//		ss.append(MyBotModule.Broodwar.self().getRace().toString() + " ");
//		ss.append(enemyName + " ");
//		ss.append(_InformationManager.Instance().enemyRace.toString() + " ");
//		ss.append(MyBotModule.Broodwar.getFrameCount() + " ");
//		ss.append(MyBotModule.Broodwar.self().supplyUsed() + " ");
//		ss.append(MyBotModule.Broodwar.self().supplyTotal() + "\n");
//
//		_Common.appendTextToFile(gameLogFileName, ss.toString());
	}

	public void onUnitShow(Unit unit) {
//		if (unit == null || !unit.exists() || unit.getHitPoints() <= 0) return;
//		if (unit.getType() == Zerg_Lurker || unit.getType() == Protoss
		
		//TechType.Scanner_Sweep
		
		//unit.useTech(TechType,.)
		
	}

	public void canYouScanThis(Unit unit) {
		canYouScanHere(unit.getPosition());
	}
	
	public void canYouScanHere(Position pos) {
		int cur = Common.Broodwar.getFrameCount()/scanCoolTime;
		for (int acc = 1; acc < scanCoolTime; acc++) {
			int index = cur - acc;
			if (index < 0) index = scanCoolTime-1;
			if (terran.scanPos[index] != null && Common.getDistanceSquared(terran.scanPos[index], pos) <= (7*Config.TILE_SIZE)*(7*Config.TILE_SIZE)) {
				terran.scanPos[cur] = null;
				return;
			}
		}
		int maxEnergy = 0;
		int selected = -1;
		for (int index = 0; index < terran.comsatStations.size(); index++) {
			if (terran.comsatStations.get(index).getEnergy() >= maxEnergy && terran.comsatStations.get(index).getEnergy() >= 50) {
				selected = index;
			}
		}
		if (selected == -1) {
			terran.scanPos[cur] = null;
		} else {
			terran.scanPos[cur] = pos;
			terran.comsatStations.get(selected).useTech(TechType.Scanner_Sweep, pos);
		}
		
	}

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////
	
}