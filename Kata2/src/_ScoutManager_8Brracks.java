import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwta.BaseLocation;

public class _ScoutManager_8Brracks {

    //private static WorkerManager workerManager = WorkerManager.Instance();

    private static _ScoutManager_8Brracks instance = new _ScoutManager_8Brracks();

    /// static singleton 객체를 리턴합니다
    public static _ScoutManager_8Brracks Instance() {
        return instance;
    }


    public TilePosition base1st, base2nd, base3rd;
    private Unit scouter1, scouter2;
    private Position targetScv1, targetScv2;


    public void update() {

        // 1초에 4번만 실행합니다
        if (MyBotModule.Broodwar.getFrameCount() % 6 != 0) return;

        BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().enemyPlayer);

        if (enemyBaseLocation == null) {
            if (Common.IsAlive(scouter1)) {
                if (MyBotModule.Broodwar.isExplored(base1st)) {
                    targetScv1 = base3rd.toPosition();  //3rd 가 대각선
                    if (MyBotModule.Broodwar.isExplored((base3rd))) {   // 3rd 로 정찰가려고 했는데 이미 탐색이 되었다면
                        WorkerManager.Instance().setCombatWorker(scouter1);
                    } else {
                        CommandUtil.MOVE(scouter1, base3rd.toPosition());
                    }
                } else {
                    targetScv1 = base1st.toPosition();
                    if (MyBotModule.Broodwar.isExplored(base1st)) { // 1st 를 정찰하려고 하는데 이미 탐색이 되었다면
                        WorkerManager.Instance().setCombatWorker(scouter1);
                    } else {
                        CommandUtil.MOVE(scouter1, base1st.toPosition());
                    }
                }

            }
            if (Common.IsAlive(scouter2)) {
                targetScv2 = base2nd.toPosition();
                if (MyBotModule.Broodwar.isExplored(base2nd)) {
                    WorkerManager.Instance().setCombatWorker(scouter2);
                } else {
                    CommandUtil.MOVE(scouter2, base2nd.toPosition());
                }
            }
        }
        else {
            if (Common.IsAlive(scouter1)) {
                WorkerManager.Instance().setCombatWorker(scouter1);
            }
            if (Common.IsAlive(scouter2)) {
                WorkerManager.Instance().setCombatWorker(scouter2);
            }
        }


    }

    public void setScouter1(Unit scv1) {
        scouter1 = scv1;
        WorkerManager.Instance().setScoutWorker(scouter1);
    }

    public void setScouter2(Unit scv2) {
        scouter2 = scv2;
        WorkerManager.Instance().setScoutWorker(scouter2);
    }
}
