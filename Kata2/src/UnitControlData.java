import bwapi.Position;

public class UnitControlData {

	public int actionGivenFrame;
    public ActionType actionType;
    public Position targetPosition;
    
	public void setMoveControlData(Position targetPosition) {
		this.actionGivenFrame = MyBotModule.Broodwar.getFrameCount();
		this.actionType = ActionType.MOVE;
		this.targetPosition = targetPosition;
	}
    
}

class ActionType {
	
	public final static ActionType HOLD = new ActionType(0);
    public final static ActionType ATTACK = new ActionType(10);
    public final static ActionType PATROL = new ActionType(20);
    public final static ActionType MOVE = new ActionType(30);
    public final static ActionType RUN = new ActionType(30);
    
    
    private int typeValue;
    public int getTypeValue() {return typeValue;}
    public ActionType(int tv) {
        this.typeValue = tv;
    }
}
