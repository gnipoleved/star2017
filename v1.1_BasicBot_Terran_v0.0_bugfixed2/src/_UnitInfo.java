
import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

/// 해당 Unit의 ID, UnitType, 소속 Player, HitPoint, lastPosition, completed(건물이 완성된 것인지) 등을 저장해두는 자료구조<br>
/// 적군 유닛의 경우 아군 시야 내에 있지 않아 invisible 상태가 되었을 때 정보를 조회할수도 없어지고 파악했던 정보도 유실되기 때문에 별도 자료구조가 필요합니다
public class _UnitInfo {
	
	public enum UnitJobs {
		Minerals, 		///< 미네랄 채취 
		Gas,			///< 가스 채취
		Build,			///< 건물 건설
		Combat, 		///< 전투
		Idle,			///< 하는 일 없음. 대기 상태. 
		Repair,			///< 수리. Terran_SCV 만 가능
		Move,			///< 이동
		Scout, 			///< 정찰. Move와 다름. Mineral / Gas / Build 등의 다른 임무로 차출되지 않게 됨. 
		Default 		///< 기본. 미설정 상태. 
	};

	private int unitID;
	private int lastHealth;
	private int lastShields;
	private Player player;
	private Unit unit;
	private Position lastPosition;
	private UnitType type;
	private boolean completed;
	
	private UnitJobs job;
	private int ssn;

	public _UnitInfo()
	{
		unitID = 0;
		lastHealth = 0;
		player = null;
		unit = null;
		lastPosition = Position.None;
		type = UnitType.None;
		completed = false;
		
		job = UnitJobs.Default;
	}

	
	public int getSsn() {
		return ssn;
	}


	public void setSsn(int ssn) {
		this.ssn = ssn;
	}


	public UnitJobs getJob() {
		return job;
	}

	public void setJob(UnitJobs job) {
		this.job = job;
	}

	public UnitType getType() {
		return type;
	}

	public boolean isCompleted() {
		return completed;
	}

	public Position getLastPosition() {
		return lastPosition;
	}

	public int getUnitID() {
		return unitID;
	}

	public void setUnitID(int unitID) {
		this.unitID = unitID;
	}

	public int getLastHealth() {
		return lastHealth;
	}

	public void setLastHealth(int lastHealth) {
		this.lastHealth = lastHealth;
	}

	public int getLastShields() {
		return lastShields;
	}

	public void setLastShields(int lastShields) {
		this.lastShields = lastShields;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public void setLastPosition(Position lastPosition) {
		this.lastPosition = lastPosition;
	}

	public void setType(UnitType type) {
		this.type = type;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof _UnitInfo)) return false;

        _UnitInfo that = (_UnitInfo) o;

        if (this.getUnitID() != that.getUnitID()) return false;

        return true;
    }

	
//		const bool operator == (BWAPI::Unit unit) const
//		{
//			return unitID == unit->getID();
//		}
//
//		const bool operator == (const UnitInfo & rhs) const
//		{
//			return (unitID == rhs.unitID);
//		}
//
//		const bool operator < (const UnitInfo & rhs) const
//		{
//			return (unitID < rhs.unitID);
//		}
};