import bwapi.Position;
import bwapi.Unit;

public class UnitControlData {

//    public Position orginalPosition;
	public int actionGivenFrame;
    public ActionType actionType;
    public Position targetPosition;
    public Unit targetUnit;

    public void setMoveControlData(Position targetPosition, Unit targetUnit) {
//        this.orginalPosition =
        this.actionGivenFrame = MyBotModule.Broodwar.getFrameCount();
        this.actionType = ActionType.MOVE;
        this.targetPosition = targetPosition;
        this.targetUnit = targetUnit;
    }

	public void setMoveControlData(Position targetPosition) {
		setMoveControlData(targetPosition, null);
	}

	public void setAttackUnitControlData(Unit target) {
	    this.actionGivenFrame = MyBotModule.Broodwar.getFrameCount();
	    this.actionType = ActionType.ATTACK_UNIT;
        this.targetPosition = target.getPosition();
	    this.targetUnit = target;
    }

    public void clearControlData() {
        this.actionGivenFrame = MyBotModule.Broodwar.getFrameCount();
        this.actionType  = ActionType.IDLE;
        this.targetPosition = null;
        this.targetUnit = null;
    }
    
}

class ActionType {

    public final static ActionType IDLE = new ActionType(0);
    public final static ActionType ATTACK_UNIT = new ActionType(10);
    public final static ActionType PATROL = new ActionType(20);
    public final static ActionType MOVE = new ActionType(30);
    public final static ActionType RUN = new ActionType(40);
    public final static ActionType HOLD = new ActionType(0);
    
    private int typeValue;
    public int getTypeValue() {return typeValue;}
    public ActionType(int tv) {
        this.typeValue = tv;
    }
}
