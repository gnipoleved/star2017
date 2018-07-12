import bwapi.Position;

public class _VsTerranStrategy extends _TerranStrategy {
	
	private boolean 공격당한적있음; // true 일때,, 어느정도 북구 되면 닷 ㅣfalse로

	@Override
	public void start() {
		공격당한적있음 = false;
	}

	@Override
	public void execute() {
//		if (공격당한적있음) {
//			if (나피해좀입었음()) {
//				if (적피해좀입었음()) {
//					전략12드론();
//				}else {
//					전략9드론();
//				}
//			}else {
//				전략12드론();
//			}
			
			// TODO 공격당한적있음을 해제는 어떻게 할것인가?
			
//			if (InformationManager.Instance().self)
//		}
	}

//	private boolean 나피해좀입었음() {
//		// TODO Auto-generated method stub
//		return false;
//	}

	@Override
	public void handleNuclearAttack(Position target) {
		// TODO Auto-generated method stub

	}

}
