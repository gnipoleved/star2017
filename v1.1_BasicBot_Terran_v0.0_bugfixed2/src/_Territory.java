

import bwta.BaseLocation;
import bwta.Region;

public class _Territory {
	
	public static int UNKNOWN = -1;
	public static int OWNED_BY_NO_ONE = 0;
	public static int OWNED_BY_SELF = 1;
	public static int OWNED_BY_ENEMY = 2;
	public static int OWNED_BY_BOTH = 3;
	
	public Region region;
	public BaseLocation base;
	public int ownership; // 0 == NONE, 1 == SELF, 2 == ENEMY, 3 == BOTH
	public boolean main;
	
	public _Territory() {
		region = null;
		base = null;
		ownership = UNKNOWN;
		main = false;
	}
	
	public void addOwner(int pidx)
	{
		//pidx = 0 또는 1
		if (pidx == 0) {	// self 소유가 추가 될때
//		if (player == Common.SELF) {
			if (ownership == UNKNOWN || ownership == OWNED_BY_NO_ONE || ownership == OWNED_BY_SELF) ownership = OWNED_BY_SELF;
			else ownership = OWNED_BY_BOTH;
		} else if (pidx == 1) {	//	enemy 소유가 추가 될때
//		} else if (player == Common.ENEMY) {	//	enemy 소유가 추가 될때
			if (ownership == UNKNOWN || ownership == OWNED_BY_NO_ONE || ownership == OWNED_BY_ENEMY) ownership = OWNED_BY_ENEMY;
			else ownership = OWNED_BY_BOTH;
		}
		
	}

}
