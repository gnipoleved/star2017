public class UnitActionData {

    public final static ActionType HOLD = new ActionType(0);
    public final static ActionType ATTACK = new ActionType(10);
    public final static ActionType PATROL = new ActionType(20);
    public final static ActionType MOVE = new ActionType(30);
    public final static ActionType RUN = new ActionType(30);


    public int actionGivenFrame;

}

class ActionType {
    private int typeValue;
    public int getTypeValue() {return typeValue;}
    public ActionType(int tv) {
        this.typeValue = tv;
    }
}
